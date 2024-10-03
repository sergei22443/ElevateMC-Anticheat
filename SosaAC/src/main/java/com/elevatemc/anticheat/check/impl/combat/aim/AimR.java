package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.api.CheckInfo;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Aim", type = "R", experimental = true, description = "Not constant rotations")
public class AimR extends Check {

    public AimR(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || packet.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() <= 10 && !data.getRotationProcessor().isZooming()) {

                float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                float deltaPitch = data.getRotationProcessor().getDeltaPitch();

                float currentYaw = data.getRotationProcessor().getYaw();
                float currentPitch = data.getRotationProcessor().getPitch();

                float f = data.getSensitivityHolder().getIntegerSensitivity() / 142.0F;

                double adjustedYaw = currentYaw - (currentYaw % f);
                double adjustedPitch = currentPitch - (currentPitch % f);

                double deltaX = Math.abs(adjustedYaw - currentYaw);
                double deltaY = Math.abs(adjustedPitch - currentPitch);

                if (deltaYaw > 0.1F && deltaPitch > 0.1F && deltaYaw < 30.0F && deltaPitch < 30.0F) {
                    if (deltaX > 0.01 && deltaY < 1.0E-4) {
                        fail("deltaX=" + deltaX + " deltaY=" + deltaY);
                    }
                }
            }
        }
    }
}
