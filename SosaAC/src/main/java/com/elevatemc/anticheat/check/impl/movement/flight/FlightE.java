package com.elevatemc.anticheat.check.impl.movement.flight;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.server.block.BlockUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Flight", type = "E", description = "Checks for invalid vertical movement")
public class FlightE extends Check {

    public FlightE(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            boolean onGround = !data.getPositionProcessor().isInAir() || (data.getPositionProcessor().isClientOnGround() || data.getPositionProcessor().isMathematicallyOnGround());

            double deltaY = data.getPositionProcessor().getDeltaY();

            double lastDeltaY = data.getPositionProcessor().getLastDeltaY();
            double difference = Math.abs(deltaY - lastDeltaY);
            boolean exempt = isExempt(ExemptType.JOINED, ExemptType.VEHICLE, ExemptType.TELEPORT, ExemptType.LIQUID, ExemptType.FLIGHT, ExemptType.WEB,ExemptType.CLIMBABLE)
                    || Math.abs(deltaY) > 3.0
                    || Math.abs(lastDeltaY) > 3.0
                    || BlockUtil.isSlime(data)
                    || data.getPositionProcessor().getY() < 0.0
                    || data.getPositionProcessor().getY() < 4.0
                    || data.getPositionProcessor().getDeltaY() < 0.09;

            double limit = 0.01;

            if (data.getVelocityProcessor().getTicksSinceVelocity() <= 10) {
                limit += Math.abs(data.getVelocityProcessor().getVelocityXZ()) + 0.15;
            }

            boolean invalid = difference < limit && !onGround;

            if (invalid && !exempt) {
                if (increaseBuffer() > 4.0) {
                    increaseVlBy(.25);
                    fail("deltaY=" + deltaY + " difference=" + difference);
                    staffAlert();
                }
            }
            else {
                decreaseBufferBy(0.25);
            }
        }
    }
}

