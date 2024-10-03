package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.type.EvictingList;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "N", description = "Checks for rotation speed")
public class PatternN extends Check {

    EvictingList<Float> input = new EvictingList<>(25);

    public PatternN(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            float pitchAcceleration = data.getRotationProcessor().getPitchAcceleration();
            float deltaPitch = data.getRotationProcessor().getDeltaPitch();
            input.add(pitchAcceleration);

            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.JOINED);

            if (input.isFull()) {
                double average = MathUtil.getAverage(input);
                // Real small acceleration with a what's considered a large rotation in this instance.
                if (average < 0.001D && deltaPitch > 0.3F && !exempt) {
                    if (increaseBuffer() > 3) fail("avg=" + average);
                    input.clear();
                }
            }
        }
    }
}