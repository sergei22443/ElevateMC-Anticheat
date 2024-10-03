package com.elevatemc.anticheat.check.impl.combat.killaura;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Kill Aura", type = "H", description = "GCD bypass flaw detected")
public class KillAuraH extends Check {

    public KillAuraH(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4) {
                boolean validSensitivity = data.getSensitivityHolder().hasValidSensitivity();

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

                    float combined = deltaYaw + deltaPitch;

                    if (MathUtil.isScientificNotation(yawDifference) && pitchDifference == 0.0f) {
                        if (increaseBuffer() > 6.0) {
                            multiplyBuffer(0.25);
                            fail("yawDifference=" + yawDifference + " pitchDifference=" + pitchDifference + " combined=" + combined);
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
