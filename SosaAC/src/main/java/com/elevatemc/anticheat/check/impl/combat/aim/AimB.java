package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.api.CheckInfo;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Aim", type = "B", description = "Not constant rotations.")
public class AimB extends Check {

    public AimB(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (hitTicks() < 4 && !data.getRotationProcessor().isZooming()) {
                float deltaYaw = data.getRotationProcessor().getDeltaYaw();
                float deltaPitch = data.getRotationProcessor().getDeltaPitch();

                float lastDeltaYaw = data.getRotationProcessor().getLastDeltaYaw();
                float lastDeltaPitch = data.getRotationProcessor().getLastDeltaPitch();

                double divisorYaw = MathUtil.getGcd((long) (deltaYaw * MathUtil.EXPANDER), (long) (lastDeltaYaw * MathUtil.EXPANDER));
                double divisorPitch = MathUtil.getGcd((long) (deltaPitch * MathUtil.EXPANDER), (long) (lastDeltaPitch * MathUtil.EXPANDER));

                double constantYaw = divisorYaw / MathUtil.EXPANDER;
                double constantPitch = divisorPitch / MathUtil.EXPANDER;

                double currentX = deltaYaw / constantYaw;
                double currentY = deltaPitch / constantPitch;

                double previousX = lastDeltaYaw / constantYaw;
                double previousY = lastDeltaPitch / constantPitch;

                if (deltaYaw > 0.1F && deltaPitch > 0.1F && deltaYaw < 20.f && deltaPitch < 20.f) {
                    double moduloX = currentX % previousX;
                    double moduloY = currentY % previousY;

                    double floorModuloX = Math.abs(Math.floor(moduloX) - moduloX);
                    double floorModuloY = Math.abs(Math.floor(moduloY) - moduloY);

                    boolean invalidX = moduloX > 60.d && floorModuloX > 0.1D;
                    boolean invalidY = moduloY > 60.d && floorModuloY > 0.1D;

                    if (invalidX && invalidY) {
                        if (increaseBuffer() > 12.0) {
                            fail("deltaYaw=" + deltaYaw + " deltaPitch=" + deltaPitch);
                            multiplyBuffer(.25);
                        }
                    } else {
                        decreaseBufferBy(.35);
                    }
                }
            }
        }
    }
}
