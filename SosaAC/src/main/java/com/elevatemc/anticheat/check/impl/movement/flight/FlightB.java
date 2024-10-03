package com.elevatemc.anticheat.check.impl.movement.flight;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Flight", type = "B", experimental = true, description = "Checks for invalid deltaY")
public class FlightB extends Check {

    public FlightB(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            boolean exempt = isExempt(ExemptType.FAST,
                    ExemptType.TELEPORT,
                    ExemptType.JOINED,
                    ExemptType.CREATIVE,
                    ExemptType.COLLIDING_VERTICALLY,
                    ExemptType.TPS,
                    ExemptType.LIQUID,
                    ExemptType.CLIMBABLE,
                    ExemptType.TRAPDOOR,
                    ExemptType.FLIGHT)
                    || data.getPlayer().getMaximumNoDamageTicks() < 16
                    || data.getPositionProcessor().getDeltaY() == 0.0
                    || data.getPositionProcessor().getY() < 0.0
                    || data.getPositionProcessor().isNearSlime();

            if (!exempt && data.getPositionProcessor().getY() > 4.0) {

                int airTicks = data.getPositionProcessor().getClientAirTicks();
                double deltaY = data.getPositionProcessor().getDeltaY();

                if (airTicks > 20 && deltaY < 0.3 && deltaY > 0.0 && data.getPlayer().getFallDistance() > 3.0f) {
                    if (increaseBuffer() > 6.0) {
                        fail("deltaY=" + deltaY + " ticks=" + airTicks);
                        staffAlert();
                        increaseVlBy(.35);
                        multiplyBuffer(.5);
                    }
                } else {
                    decreaseBufferBy(0.5);
                }
            }
        }
    }
}
