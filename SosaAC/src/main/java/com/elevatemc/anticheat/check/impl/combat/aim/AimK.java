package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

//Big shout-out to NikV2 and Elevate for the help on this check!
@CheckInfo(name = "Aim", type = "K", description = "Checks for improper X and Y rotations")
public class AimK extends Check {

    public AimK(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4) {
                final boolean exempt = data.getRotationProcessor().getLastZoom() < 60L || data.getRotationProcessor().isZooming();
                if (!exempt) {

                    float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                    float deltaPitch = data.getRotationProcessor().getDeltaPitch();

                    double divYaw = (double) MathUtil.getGcd((long) (data.getRotationProcessor().getDeltaYaw() * MathUtil.EXPANDER), (long) (data.getRotationProcessor().getLastDeltaYaw() * MathUtil.EXPANDER));
                    double divPitch = (double) MathUtil.getGcd((long) (data.getRotationProcessor().getDeltaPitch() * MathUtil.EXPANDER), (long) (data.getRotationProcessor().getLastDeltaPitch() * MathUtil.EXPANDER));

                    double constantYaw = divYaw / MathUtil.EXPANDER;
                    double constantPitch = divPitch / MathUtil.EXPANDER;

                    double currentX = data.getRotationProcessor().getDeltaYaw() / constantYaw;
                    double currentY = data.getRotationProcessor().getDeltaPitch() / constantPitch;

                    double previousX = data.getRotationProcessor().getLastDeltaYaw() / constantYaw;
                    double previousY = data.getRotationProcessor().getLastDeltaPitch() / constantPitch;

                    if (deltaYaw > 0.1F && deltaPitch > 0.1F && deltaYaw < 20.f && deltaPitch < 20.f) {

                        double moduloX = currentX / previousX;
                        double moduloY = currentY / previousY;

                        double floorModuloX = Math.abs(Math.floor(moduloX) - moduloX);
                        double floorModuloY = Math.abs(Math.floor(moduloY) - moduloY);

                        boolean invalidX = moduloX > 90.0F && floorModuloX > 0.1F;
                        boolean invalidY = moduloY > 90.0F && floorModuloY > 0.1F;

                        if (invalidX || invalidY) {
                            if (increaseBuffer() > 24.0) {
                                fail("mX=" + moduloX + " mY=" + moduloY + " fMX=" + floorModuloX + " fmY=" + floorModuloY);
                                multiplyBuffer(.5);
                            } else {
                                decreaseBufferBy(.15);
                            }
                        }
                    }
                }
            }
        }
    }
}