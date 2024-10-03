package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Aim", type = "D", description = "GCD bypass flaw")
public class AimD extends Check {

    public AimD(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4) {
                final boolean validSensitivity = data.getSensitivityHolder().hasValidSensitivity();
                if (validSensitivity) {

                    double mcpSensitivity = data.getSensitivityHolder().getMcpSensitivity();

                    float f = (float) mcpSensitivity * 0.6f + 0.2f;

                    float gcd = f * f * f * 1.2f;
                    float yaw = data.getRotationProcessor().getYaw();
                    float pitch = data.getRotationProcessor().getPitch();

                    float adjustedYaw = yaw - yaw % gcd;
                    float adjustedPitch = pitch - pitch % gcd;

                    float yawDifference = Math.abs(yaw - adjustedYaw);
                    float pitchDifference = Math.abs(pitch - adjustedPitch);

                    float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                    float deltaPitch = data.getRotationProcessor().getDeltaPitch();

                    boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.JOINED);

                    if (yawDifference == 0.0f || (pitchDifference == 0.0f && deltaPitch > 1.0f && deltaYaw > 1.0f) && !exempt) {
                        if (increaseBuffer() > 12.0) {
                            multiplyBuffer(0.25);
                            fail("yawDifference=" + yawDifference + " pitchDifference=" + pitchDifference);
                            staffAlert();
                            increaseVlBy(1.2);
                        }
                    } else {
                        decreaseBufferBy(.2);
                    }
                }
            }
        }
    }
}
