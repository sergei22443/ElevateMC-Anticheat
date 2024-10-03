package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerAbilities;

@CheckInfo(name = "Protocol", type = "A", description = "Spoofed abilities packets.")
public class ProtocolA extends Check {

    public ProtocolA(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ABILITIES) {
            final WrapperPlayClientPlayerAbilities wrapper = new WrapperPlayClientPlayerAbilities(event);

            check: {
                if (data.getPlayer().getAllowFlight()) break check;

                if (wrapper.isFlightAllowed().orElse(false)) {
                    if (increaseBuffer() > 2.0) {
                        fail("Spoofed abilities packet");
                        increaseVlBy(.45);
                        staffAlert();
                    }
                } else {
                    resetBuffer();
                }
            }
        }
    }
}
