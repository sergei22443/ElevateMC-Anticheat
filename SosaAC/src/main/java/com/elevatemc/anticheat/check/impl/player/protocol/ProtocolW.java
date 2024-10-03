package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Protocol", type = "W", description = "Respawning whilst not dead.")
public class ProtocolW extends Check {

    public ProtocolW(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS) {
            final WrapperPlayClientClientStatus wrapper = new WrapperPlayClientClientStatus(event);

            if (wrapper.getAction() == WrapperPlayClientClientStatus.Action.PERFORM_RESPAWN) {
                if (data.getPlayer().getHealth() > 0.0) {
                    if (increaseBuffer() > 2.0) {
                        fail("Player is trying to respawn while not dead");
                        staffAlert();
                    }
                }
            }
        } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            decreaseBufferBy(0.2);
        }
    }
}
