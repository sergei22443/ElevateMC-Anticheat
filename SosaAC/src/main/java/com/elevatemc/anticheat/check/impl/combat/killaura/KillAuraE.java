package com.elevatemc.anticheat.check.impl.combat.killaura;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Kill Aura", type = "E", description = "GCD Bypass and divisor flaw detected.")
public class KillAuraE extends Check {

    public KillAuraE(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (!data.getRotationProcessor().isZooming() && hitTicks() < 6) {
                boolean validSensitivity = data.getSensitivityHolder().hasValidSensitivity();

                if (validSensitivity) {

                    float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                    float deltaPitch = data.getRotationProcessor().getDeltaPitch();
                    float lastDeltaPitch = data.getRotationProcessor().getLastDeltaPitch();

                    double divisorPitch = MathUtil.getGcd((long) (deltaPitch * MathUtil.EXPANDER), (long) (lastDeltaPitch * MathUtil.EXPANDER));
                    double mcpSensitivity = data.getSensitivityHolder().getMcpSensitivity();

                    float f = (float) mcpSensitivity * 0.6f + 0.2f;
                    float gcd = f * f * f * 1.2f;

                    double deltaY = deltaPitch / gcd;
                    double divisor = Math.abs(gcd / divisorPitch);
                    double floorDivisorY = Math.abs(Math.round(deltaY) - deltaY);

                    if (deltaYaw > 0.1F && deltaPitch > 0.1F && deltaYaw < 20.f && deltaPitch < 20.f) {
                        if (divisor > 5E-7 && floorDivisorY > 0.3) {
                            if (increaseBuffer() > 7) {
                                fail("divisor=" + divisor + " floorDivisorY=" + floorDivisorY);
                                resetBuffer();
                            }
                        }
                    }
                }
            }
        }
    }
}
