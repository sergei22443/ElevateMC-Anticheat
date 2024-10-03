package com.elevatemc.anticheat.check.impl.combat.killaura;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Kill Aura", type = "I", description = "Checks for GCD Bypass")
public class KillAuraI extends Check {


    private float lastRotationYaw, lastDeltaPitch, lastDeltaYaw;

    public KillAuraI(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            if (hitTicks() < 4) {
                final boolean validSensitivity = data.getSensitivityHolder().hasValidSensitivity();

                if (validSensitivity) {

                    float sensitivityX = (float)data.getSensitivityHolder().getSensitivityX();
                    float sensitivityY = (float)data.getSensitivityHolder().getSensitivityY();

                    float rotationYaw = data.getRotationProcessor().getYaw();
                    float rotationPitch = data.getRotationProcessor().getPitch();

                    float clampedYaw = modulo(sensitivityX, rotationYaw);
                    float clampedPitch = modulo(sensitivityY, rotationPitch);

                    float deltaYaw = Math.abs(clampedYaw - rotationYaw);
                    float deltaPitch = Math.abs(clampedPitch - rotationPitch);

                    float subtractedYaw = Math.abs(deltaYaw - this.lastDeltaYaw);
                    float subtractedPitch = Math.abs(deltaPitch - this.lastDeltaPitch);

                    if (deltaYaw < 1.0E-5 && deltaPitch < 1.0E-5 && subtractedYaw < 0.01 && subtractedPitch < 0.01) {
                        if (increaseBuffer() > 3.0) {
                            fail("subtracted=" + subtractedPitch);
                        }
                    } else {
                        decreaseBuffer();
                    }

                    this.lastDeltaPitch = deltaPitch;
                    this.lastDeltaYaw = deltaYaw;
                    this.lastRotationYaw = rotationYaw;
                }
            }
        }
    }
    private static float modulo(final float sensitivity, final float angle) {
        final float f = sensitivity * 0.6f + 0.2f;
        final float f2 = f * f * f * 1.2f;
        return angle - angle % f2;
    }
}