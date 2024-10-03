package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;

@CheckInfo(name = "Protocol", type = "G", description = "Duplicate HeldItemSlot packets.")
public class ProtocolG extends Check {

    private int lastSlot = -1;

    public ProtocolG(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            final WrapperPlayClientHeldItemChange wrapper = new WrapperPlayClientHeldItemChange(event);

            final int slot = wrapper.getSlot();

            if (slot == lastSlot) {

                fail("Duplicate slot packets");
                staffAlert();
            }

            lastSlot = slot;
        }
    }
}
