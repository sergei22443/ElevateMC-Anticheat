package com.elevatemc.anticheat.check.impl.combat.killaura;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Kill Aura", type = "L", description = "GCD Bypass Flaw detected")
public class KillAuraL extends Check {

    public KillAuraL(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 5) {
                boolean validSensitivity = data.getSensitivityHolder().hasValidSensitivity();
                if (validSensitivity) {

                    boolean teleporting = isExempt(ExemptType.TELEPORT);
                    double mcpSensitivity = data.getSensitivityHolder().getMcpSensitivity();

                    if (mcpSensitivity < 0.001) {
                        return;
                    }

                    float f = (float)mcpSensitivity * 0.6f + 0.2f;
                    float gcd = f * f * f * 1.2f;

                    float yaw = data.getRotationProcessor().getYaw();
                    float pitch = data.getRotationProcessor().getPitch();

                    float adjustedYaw = yaw - yaw % gcd;
                    float adjustedPitch = pitch - pitch % gcd;

                    float yawDifference = Math.abs(yaw - adjustedYaw);
                    float pitchDifference = Math.abs(pitch - adjustedPitch);

                    float gcdYawDifference = Math.abs(gcd - yawDifference);
                    float gcdPitchDifference = Math.abs(gcd - pitchDifference);

                    double moduloX = gcdYawDifference % 0.001;
                    double moduloY = gcdPitchDifference % 0.001;

                    if (moduloX < 1.0E-6 && moduloY < 1.0E-6 && !teleporting) {
                        if (increaseBuffer() > 8.0) {
                            fail("moduloX=" + moduloX + " moduloY=" + moduloY);
                            multiplyBuffer(0.5);
                        }
                    } else {
                        decreaseBufferBy(0.175);
                    }
                }
            }
        }
    }
}
