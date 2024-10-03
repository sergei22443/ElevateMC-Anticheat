package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Motion", type = "J", description = "Checks if a players' acceleration is invalid")
public class MotionJ extends Check {

    public MotionJ(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            double deltaXZ = data.getPositionProcessor().getDeltaXZ();
            double lastDeltaXZ = data.getPositionProcessor().getLastDeltaXZ();

            double limit = PlayerUtil.getBaseSpeed(data.getPlayer()) + 0.1 + (isExempt(ExemptType.VELOCITY) ? (Math.hypot(Math.abs(data.getVelocityProcessor().getVelocityX()), data.getVelocityProcessor().getVelocityZ()) + 0.15) : 0.0);
            double acceleration = deltaXZ - lastDeltaXZ;

            boolean exempt = isExempt(ExemptType.FLIGHT,
                    ExemptType.VEHICLE, ExemptType.COLLIDING_VERTICALLY,
                    ExemptType.TELEPORT,ExemptType.CLIMBABLE, ExemptType.SLIME, ExemptType.JOINED);

            boolean teleport = data.getActionProcessor().getLastBukkitTeleport() < 10;

            boolean invalid = acceleration > limit;
            if (invalid && !exempt && !teleport) {
                if (increaseBuffer() > 6.0) {
                    fail("accel=" + acceleration + " deltaXZ=" + deltaXZ);
                    staffAlert();
                }
            } else {
                decreaseBufferBy(.15);
            }
        }
    }
}
