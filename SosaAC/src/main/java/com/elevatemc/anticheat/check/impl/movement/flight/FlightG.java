package com.elevatemc.anticheat.check.impl.movement.flight;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Flight", type = "G", experimental = true, description = "Checks for VClipping")
public class FlightG extends Check {

    double lastLastDeltaY;
    public FlightG(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.JOINED, ExemptType.SLIME, ExemptType.ICE, ExemptType.SPECTATOR, ExemptType.CREATIVE);

            if (!exempt) {
                double deltaY = data.getPositionProcessor().getDeltaY(), lastDeltaY = data.getPositionProcessor().getLastDeltaY();

                int airTicks = data.getPositionProcessor().getClientAirTicks();
                int sTicks = data.getPositionProcessor().getServerAirTicks();

                if ((airTicks >= 20 || sTicks >= 20) && (!data.getPositionProcessor().isClientOnGround() || !data.getPositionProcessor().isMathematicallyOnGround())) {
                    if (increaseBuffer() > 15.0) {
                        if (deltaY > 0.7f && lastDeltaY < 0.42f && lastLastDeltaY != lastDeltaY) {
                            fail("dY=" + deltaY + " lDY=" + lastDeltaY);
                            resetBuffer();
                        }
                    }
                } else {
                    decreaseBuffer();
                }

                lastLastDeltaY = lastDeltaY;
            }
        }
    }
}
