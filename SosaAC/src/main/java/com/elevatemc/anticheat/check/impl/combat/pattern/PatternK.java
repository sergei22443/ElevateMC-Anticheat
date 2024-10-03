package com.elevatemc.anticheat.check.impl.combat.pattern;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Pattern", type = "K", description = "Checks for head acceleration that doesn't match the rotation")
public class PatternK extends Check {

    public PatternK(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4) {
                float yAcceleration = data.getRotationProcessor().getYawAcceleration();
                float pAcceleration = data.getRotationProcessor().getPitchAcceleration();

                float deltaPitch = data.getRotationProcessor().getDeltaPitch();
                float deltaYaw = data.getRotationProcessor().getDeltaYaw();

                if ((MathUtil.isScientificNotation(yAcceleration) || MathUtil.isScientificNotation(pAcceleration)) && (deltaPitch > 0.5 && deltaYaw < 0.003 || deltaYaw > 0.5 && deltaPitch < 0.003)) {
                    if (increaseBuffer() > 5.0) {
                        fail("p=" + deltaPitch + " y=" + deltaYaw + " yAccel=" + yAcceleration + " pAccel=" + pAcceleration);
                    }
                } else {
                    decreaseBufferBy(.025);
                }
            }
        }
    }
}
