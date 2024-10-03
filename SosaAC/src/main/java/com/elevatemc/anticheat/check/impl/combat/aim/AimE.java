package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;


@CheckInfo(name = "Aim", type = "E", description = "GCD bypass flaw detected.")
public class AimE extends Check {

    public AimE(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            boolean valid = hitTicks() < 5 && data.getSensitivityHolder().hasValidSensitivity() && !data.getRotationProcessor().isZooming();

            if (valid) {

                float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                float lastDeltaYaw = data.getRotationProcessor().getLastDeltaYaw();

                float deltaPitch = data.getRotationProcessor().getDeltaPitch();

                double divisorYaw = (double) MathUtil.getGcd((long)(deltaYaw * MathUtil.EXPANDER), (long)(lastDeltaYaw * MathUtil.EXPANDER));

                int integerSensitivity = data.getSensitivityHolder().getIntegerSensitivity();

                float f = integerSensitivity / 200.0f * 0.6f + 0.2f;
                float gcdTable = f * f * f * 1.2f;

                double divisor = gcdTable / divisorYaw;

                if (deltaYaw > 0.0 && deltaPitch > 0.0 && deltaYaw < 1.0f && deltaPitch < 1.0f) {
                    if (divisor > 9.9E-7) {
                        if (increaseBuffer() > 6.0) {
                            fail("divisor=" + divisor + " y=" + divisorYaw);
                        }
                    } else {
                        decreaseBufferBy(2.0);
                    }
                }
            }
        }
    }
}
