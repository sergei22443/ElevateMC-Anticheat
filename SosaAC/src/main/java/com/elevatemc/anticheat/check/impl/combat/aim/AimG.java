package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Aim", type = "G", description = "Checks for a divisor in the delta rotations")
public class AimG extends Check {
    public AimG(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4 && !data.getRotationProcessor().isZooming()) {
                boolean validSensitivity = data.getSensitivityHolder().hasValidSensitivity();

                if (validSensitivity) {
                    float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                    float deltaPitch = data.getRotationProcessor().getDeltaPitch();
                    float lastDeltaPitch = data.getRotationProcessor().getLastDeltaPitch();

                    double divisorPitch = MathUtil.getGcd((long) (deltaPitch * MathUtil.EXPANDER), (long) (lastDeltaPitch * MathUtil.EXPANDER));
                    double mcpSensitivity = data.getSensitivityHolder().getMcpSensitivity();

                    float f = (float) mcpSensitivity * 0.6f + 0.2f;
                    float gcd = f * f * f * 1.2f;

                    double divisor = Math.abs(gcd / divisorPitch);

                    if (deltaYaw > 0.0 && deltaPitch > 0.0 && deltaYaw < 2 && deltaPitch < 2) {
                        boolean invalid = divisor > 5E-7;

                        if (invalid) {
                            if (increaseBuffer() > 15.0) {
                                fail("divisor=" + divisor);
                            }
                        } else {
                            decreaseBufferBy(0.5);
                        }
                    }
                }
            }
        }
    }
}
