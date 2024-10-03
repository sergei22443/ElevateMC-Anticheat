package com.elevatemc.anticheat.check.impl.player.inventory;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Inventory", type = "B", description = "Checks if player is moving while inventory is open")
public class InventoryB extends Check {

    public InventoryB(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            final int sprintTicks = data.getActionProcessor().getSprintingTicks();

            final double deltaXZ = data.getPositionProcessor().getDeltaXZ();

            final boolean invalid = sprintTicks > 2 && deltaXZ > 0.2;
            final boolean exempt = isExempt(ExemptType.CREATIVE,ExemptType.SPECTATOR) || data.getVelocityProcessor().getTicksSinceVelocity() < 10;

            if (invalid && !exempt) {
                increaseVlBy(.55);
                fail("ticks=" + sprintTicks + " dXZ=" + deltaXZ);
                staffAlert();
            }
        }
    }
}

