package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.type.EvictingList;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "B", description = "Checks for duplicate rotations.")
public class PatternB extends Check {

    EvictingList<Float> samples = new EvictingList<>(120);

    public PatternB(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            float deltaYaw = this.data.getRotationProcessor().getDeltaYaw();
            float deltaPitch = this.data.getRotationProcessor().getDeltaPitch();

            boolean valid = hitTicks() < 4 && !data.getRotationProcessor().isZooming() && data.getSensitivityHolder().hasValidSensitivity();

            if (deltaYaw > 0.0 && deltaPitch > 0.0 && deltaYaw < 30.0f && deltaPitch < 30.0f && valid) {
                samples.add(deltaPitch);
            }
            if (samples.isFull()) {
                int distinct = MathUtil.getDistinct(samples);
                int duplicates = samples.size() - distinct;

                double average = MathUtil.getAverage(samples);

                if (duplicates <= 9 && average < 30.0 && distinct > 130) {
                    if (increaseBuffer() > 4.0) {
                        fail("dup=" + duplicates + ", avg=" + average);
                    }
                }
                else {
                    decreaseBufferBy(3);
                }
               samples.clear();
            }
        }
    }
}