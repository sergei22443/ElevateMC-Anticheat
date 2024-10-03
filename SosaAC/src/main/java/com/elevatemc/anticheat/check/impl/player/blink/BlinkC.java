package com.elevatemc.anticheat.check.impl.player.blink;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;

@CheckInfo(name = "Blink", type = "C", experimental = true, description = "Invalid keep alive packet id.")
public class BlinkC extends Check {

    public BlinkC(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
            WrapperPlayClientKeepAlive wrapper = new WrapperPlayClientKeepAlive(event);

            long id = wrapper.getId();

            if (id == 10000L && increaseBuffer() > 1) {
                fail("id=" + id);
            }
        }
    }
}
