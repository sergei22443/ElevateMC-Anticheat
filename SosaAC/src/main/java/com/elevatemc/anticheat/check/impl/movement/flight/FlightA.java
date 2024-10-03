package com.elevatemc.anticheat.check.impl.movement.flight;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Flight", type = "A", description = "Vertical axis prediction.")
public class FlightA extends Check {

    public FlightA(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            int airTicks = data.getPositionProcessor().getServerAirTicks();

            double deltaY = data.getPositionProcessor().getDeltaY();
            double lastDeltaY = data.getPositionProcessor().getLastDeltaY();

            double prediction = (lastDeltaY - 0.08) * 0.9800000190734863;
            double difference = Math.abs(deltaY - prediction);

            boolean invalid = airTicks > 5 && difference > 1e-10;
            boolean exempt = isExempt(ExemptType.FLIGHT, ExemptType.CREATIVE, ExemptType.LIQUID,
                    ExemptType.TRAPDOOR,
                    ExemptType.TELEPORT, ExemptType.LAG_SPIKE, ExemptType.BLOCK_PLACE, ExemptType.JOINED)
                    || data.getPositionProcessor().getY() < 0.0;

            if (invalid && !exempt) {
                if (increaseBuffer() > 8) {
                    increaseVlBy(.45);
                    fail("ticks=" + airTicks + " deltaY=" + deltaY + " diff=" + difference);
                    staffAlert();
                    multiplyBuffer(.5);
                }
            } else {
                decreaseBufferBy(.125);
            }
        }
    }
}