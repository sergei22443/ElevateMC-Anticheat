package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.util.type.EvictingList;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "H", description = "Checks for lock aim")
public class PatternH extends Check {

    EvictingList<Float> yawSamples = new EvictingList<>(80), pitchSamples = new EvictingList<>(80);
    public PatternH(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4 && !data.getRotationProcessor().isZooming()) {

                float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                float deltaPitch = data.getRotationProcessor().getDeltaPitch();

                if (deltaPitch > 0.0 && deltaYaw > 0.0 && deltaPitch < 20.0f && deltaYaw < 20.0f) {
                    yawSamples.add(deltaYaw);
                    pitchSamples.add(deltaPitch);

                    if (yawSamples.isFull() && pitchSamples.isFull()) {
                        int duplicatesY = MathUtil.getDuplicates(yawSamples);
                        int duplicatesP = MathUtil.getDuplicates(pitchSamples);

                        if (duplicatesP + duplicatesY <= 1) {
                            if (increaseBuffer() > 3) fail("");
                        } else {
                            decreaseBuffer();
                        }

                        yawSamples.clear();
                        pitchSamples.clear();
                    }
                }

                if (hitTicks() > 100) {
                    yawSamples.clear();
                    pitchSamples.clear();
                }
            }
        }
    }
}
