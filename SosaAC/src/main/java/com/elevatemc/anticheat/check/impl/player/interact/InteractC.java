package com.elevatemc.anticheat.check.impl.player.interact;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;

@CheckInfo(name = "Interact", type = "C", description = "Invalid Inventory Interactions")
public class InteractC extends Check {


    public InteractC(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            final WrapperPlayClientClickWindow wrapper = new WrapperPlayClientClickWindow(event);

            if (wrapper.getActionNumber().isPresent() && wrapper.getActionNumber().get() == 0 && !data.getActionProcessor().isInventoryOpen()) {
                fail("Invalid inventory actions");
                increaseVlBy(.45);
                staffAlert();
            }
        }
    }
}
