package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Protocol", type = "Y", description = "Spoofed game movement.")
public class    ProtocolY extends Check {

    public ProtocolY(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {

            if (!isExempt(ExemptType.JOINED, ExemptType.FLIGHT, ExemptType.CREATIVE, ExemptType.COLLIDING_VERTICALLY, ExemptType.FAST)) {

                int clientAirTicks = data.getPositionProcessor().getClientAirTicks(), server = data.getPositionProcessor().getServerAirTicks();

                double deltaY = data.getPositionProcessor().getDeltaY();

                if (deltaY > 0.5 && (clientAirTicks == 0 && server >= 50 || server == 0 && clientAirTicks >= 50)) {
                    if (increaseBuffer() > 3.0) {
                        fail("dY=" + deltaY + " s=" + server);
                    }
                } else {
                    decreaseBufferBy(.25);
                }
            }
        }
    }
}

