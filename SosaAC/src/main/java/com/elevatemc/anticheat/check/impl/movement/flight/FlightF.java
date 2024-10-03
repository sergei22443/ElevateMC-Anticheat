package com.elevatemc.anticheat.check.impl.movement.flight;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Flight", type = "F", experimental = true, description = "Spoofed ground")
public class FlightF extends Check {

    public FlightF(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            boolean serverOnGround = data.getPositionProcessor().isMathematicallyOnGround();
            boolean clientOnGround = data.getPositionProcessor().isClientOnGround();

            boolean exempt = isExempt(ExemptType.JOINED, ExemptType.VEHICLE, ExemptType.ONVEHICLE, ExemptType.FLIGHT, ExemptType.TPS, ExemptType.LAG_SPIKE, ExemptType.FAST, ExemptType.TELEPORT, ExemptType.WEB, ExemptType.CREATIVE, ExemptType.SPECTATOR) || data.getPositionProcessor().getTicksSinceWeb() < 5;

            if (!exempt) {
                if (clientOnGround && !serverOnGround || serverOnGround && !clientOnGround) {
                    if (increaseBuffer() > 20.0) {
                        fail("c=" + clientOnGround + " s=" + serverOnGround);
                    }
                } else {
                    decreaseBufferBy(.75);
                }
            }
        }
    }
}
