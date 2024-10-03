package com.elevatemc.anticheat.check.impl.player.invalid;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;

import java.util.Objects;

@CheckInfo(name = "Invalid", type = "C", description = "Checks for Lite Client by Rhys")
public class InvalidC extends Check {

    public InvalidC(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {

            WrapperPlayClientPluginMessage wrappedPacketInCustomPayload = new WrapperPlayClientPluginMessage(packet);
            String payload = wrappedPacketInCustomPayload.getChannelName();

            if (Objects.equals(payload, "218c69d8875f")) {
                fail("Lite client detected.");
            }
        }
    }
}
