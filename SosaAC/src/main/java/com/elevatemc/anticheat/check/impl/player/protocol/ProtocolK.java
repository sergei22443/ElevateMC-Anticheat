package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;

@CheckInfo(name = "Protocol", type = "K", description = "Spoofed KeepAlive packet.")
public class ProtocolK extends Check {

    public ProtocolK(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
            final WrapperPlayClientKeepAlive wrapper = new WrapperPlayClientKeepAlive(event);

            if (wrapper.getId() == 0L) {
                increaseVlBy(1.25);
                fail("Spoofed KeepAlive packets, cheating (disabler)");
                staffAlert();
            }
        }
    }
}
