package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;

@CheckInfo(name = "Protocol", type = "X", description = "Custom Payload detected")
public class ProtocolX extends Check {

    public ProtocolX(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
            final WrapperPlayClientPluginMessage wrapper = new WrapperPlayClientPluginMessage(event);

            String channel = wrapper.getChannelName();

            final boolean exempt = channel.equals("CrystalClient");

            if (!exempt) {
                if (channel.equals("MC|Brand") || channel.equals("REGISTER")) {
                    if (increaseBuffer() > 7.0) {
                        multiplyBuffer(.35);
                        increaseVlBy(1.45);
                        fail("c= " + channel);
                        staffAlert();
                    }
                } else {
                    decreaseBufferBy(.75);
                }
            }
        }
    }
}

