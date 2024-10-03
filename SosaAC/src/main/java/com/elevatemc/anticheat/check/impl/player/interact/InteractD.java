package com.elevatemc.anticheat.check.impl.player.interact;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Interact", type = "D", description = "Checks for FastUse")
public class InteractD extends Check {

    public InteractD(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            boolean exempt = data.getActionProcessor().getLastItemDrop() < 10;

            if (!exempt) {
                long startConsume = data.getActionProcessor().getStartConsume();
                long delay = now() - startConsume;

                if (delay < 700L && delay > 100L) {
                    fail("delay=" + delay);
                }
            }
        }
    }
}