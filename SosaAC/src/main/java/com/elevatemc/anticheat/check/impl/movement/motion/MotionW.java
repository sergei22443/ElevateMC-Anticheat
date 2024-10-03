package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.api.CheckInfo;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Motion", type = "W", description = "Checks for too large movement")
public class MotionW extends Check {

    public MotionW(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {

            double deltaXZ = data.getPositionProcessor().getDeltaXZ();
            double acceleration = data.getPositionProcessor().getAcceleration();

            int clientGroundTicks = data.getPositionProcessor().getClientGroundTicks();

            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.CREATIVE, ExemptType.SPECTATOR, ExemptType.JOINED) || data.getPositionProcessor().getY() < 4.0;

            if (acceleration > 1.5D && deltaXZ > 2.5D && clientGroundTicks > 1 && !exempt) {
                if (increaseBuffer() > 1) fail("accel=" + acceleration + " dXZ=" + deltaXZ); else resetBuffer();
            }
        }
    }
}
