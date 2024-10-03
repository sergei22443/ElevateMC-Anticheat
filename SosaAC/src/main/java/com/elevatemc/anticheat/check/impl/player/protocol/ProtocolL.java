package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;

@CheckInfo(name = "Protocol", type = "L", description = "Duplicate KeepAlive packets.")
public class ProtocolL extends Check {

    private long lastId = -1;

    public ProtocolL(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
            final WrapperPlayClientKeepAlive wrapper = new WrapperPlayClientKeepAlive(event);

            final long id = wrapper.getId();

            if (id == lastId) {
                fail("Disabler, player is cheating.");
                staffAlert();
            }
            lastId = id;
        }
    }
}
