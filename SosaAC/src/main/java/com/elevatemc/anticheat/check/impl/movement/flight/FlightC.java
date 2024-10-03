package com.elevatemc.anticheat.check.impl.movement.flight;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Flight", type = "C", description = "Checks for hover flight")
public class FlightC extends Check {

    double lastLastY;
    public FlightC(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            double mcpLimit = 6.0 + data.getPotionProcessor().getSpeedBoostAmplifier();
            double deltaY = data.getPositionProcessor().getDeltaY();

            int airTicks = data.getPositionProcessor().getClientAirTicks();

            boolean velocity = data.getPlayer().getVelocity().getY() < -.075;
            boolean exempt = isExempt(ExemptType.LIQUID, ExemptType.FLIGHT,
                    ExemptType.TRAPDOOR,
                    ExemptType.JOINED, ExemptType.TELEPORT, ExemptType.FAST, ExemptType.LAG_SPIKE, ExemptType.CLIMBABLE, ExemptType.COLLIDING_VERTICALLY);

            if (data.getPositionProcessor().getDeltaXZ() == 0.0 && data.getPositionProcessor().getLastDeltaXZ() == 0.0) {
                return;
            }

            if (deltaY > 0.78 && airTicks > mcpLimit && velocity && !exempt) {
                if (increaseBuffer() > 8.0) {
                    fail("deltaY=" + deltaY + " ticks=" + airTicks);
                    staffAlert();
                }
            } else {
                decreaseBufferBy(.025);
            }
            lastLastY = data.getPositionProcessor().getLastY();
        }
    }
}
