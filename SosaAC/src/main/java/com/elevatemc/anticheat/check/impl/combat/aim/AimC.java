package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Aim", type = "C", description = "Subtle aim assist modifications")
public class AimC extends Check {

    public AimC(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4) {
                final boolean validSensitivity = this.data.getSensitivityHolder().hasValidSensitivity();
                if (validSensitivity) {

                    double mcpSensitivity = this.data.getSensitivityHolder().getMcpSensitivity();

                    float f = (float) mcpSensitivity * 0.6f + 0.2f;
                    float gcd = f * f * f * 1.2f;

                    float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                    float deltaPitch = data.getRotationProcessor().getDeltaPitch();

                    double deltaX = deltaYaw / gcd;
                    double deltaY = deltaPitch / gcd;

                    double floorDivisorX = Math.abs(Math.round(deltaX) - deltaX);
                    double floorDivisorY = Math.abs(Math.round(deltaY) - deltaY);

                    if (floorDivisorX > 0.03 && floorDivisorY < 1.0E-4) {
                        if (increaseBuffer() > 25.0) {
                            multiplyBuffer(0.25);
                            fail("divisorX=" + floorDivisorX + " divisorY=" + floorDivisorY);
                            staffAlert();
                        }
                    } else {
                        decreaseBufferBy(1.25);
                    }
                }
            }
        }
    }
}

