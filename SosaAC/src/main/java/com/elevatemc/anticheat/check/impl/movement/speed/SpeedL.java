package com.elevatemc.anticheat.check.impl.movement.speed;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Speed", type = "L", description = "Simulates most vanilla possibilities")
public class SpeedL extends Check {

    public SpeedL(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            if (data.getPlayer().getWalkSpeed() < 0.2f) return;

            boolean sprinting = data.getActionProcessor().isSprinting();

            double lastDeltaX = data.getPositionProcessor().getLastDeltaX();
            double lastDeltaZ = data.getPositionProcessor().getLastDeltaZ();

            double deltaXZ = data.getPositionProcessor().getDeltaXZ();
            double deltaY = data.getPositionProcessor().getDeltaY();

            int groundTicks = data.getPositionProcessor().getClientGroundTicks();
            int airTicks = data.getPositionProcessor().getClientAirTicks();

            float jumpMotion = 0.42f + data.getPotionProcessor().getJumpBoostAmplifier() * 0.1F;

            double groundLimit = PlayerUtil.getBaseGroundSpeed(data.getPlayer());
            double airLimit = PlayerUtil.getBaseSpeed(data.getPlayer());

            if (Math.abs(deltaY - jumpMotion) < 1.0E-4 && airTicks == 1 && sprinting) {
                float f = this.data.getRotationProcessor().getYaw() * 0.017453292f;

                double x = lastDeltaX - Math.sin(f) * 0.2D;
                double z = lastDeltaZ + Math.cos(f) * 0.2D;
                airLimit += Math.hypot(x, z);
            }
            if (isExempt(ExemptType.ICE, ExemptType.SLIME)) {
                airLimit += 0.34D;
                groundLimit += 0.34;
            }
            if (isExempt(ExemptType.COLLIDING_HORIZONTALLY)) {
                airLimit += 0.91F;
                groundLimit += 0.91;
            }
            if (groundTicks < 7) {
                groundLimit += 0.25f / groundTicks;
            }
            if (data.getPlayer().getWalkSpeed() > 0.3 && data.getPositionProcessor().isClientOnGround()) {
                return;
            }
            if (data.getVelocityProcessor().getFlyingVelocityTicks() <= 5) {
                groundLimit += data.getVelocityProcessor().getVelocityXZ() + 0.15;
                airLimit += data.getVelocityProcessor().getVelocityXZ() + 0.15;
            }
            boolean exempt = isExempt(ExemptType.ICE, ExemptType.FAST, ExemptType.VEHICLE, ExemptType.DEAD, ExemptType.ICE, ExemptType.FLIGHT, ExemptType.TELEPORT, ExemptType.CHUNK);

            if (!exempt) {
                if (airTicks > 10) {
                    if (deltaXZ > airLimit && airLimit != 0.34D) {
                        if (increaseBuffer() > 15.0) {
                            fail("limit=" + airLimit);
                        }
                    } else {
                        decreaseBufferBy(1.15);
                    }
                } else if (deltaXZ > groundLimit) {
                    if (increaseBuffer() > 15.0) {
                        fail("dXZ=" + deltaXZ + " limit=" + groundLimit);
                    }
                } else {
                    decreaseBufferBy(1.15);
                }
            }
        }
    }
}
