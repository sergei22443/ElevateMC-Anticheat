package com.elevatemc.anticheat.check.impl.player.inventory;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;


@CheckInfo(name = "Inventory", type = "D", description = "Sprinting while inventory is open")
public class InventoryD extends Check {

    public InventoryD(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            boolean exempt = isExempt(ExemptType.JOINED, ExemptType.FLIGHT, ExemptType.CREATIVE, ExemptType.VELOCITY, ExemptType.SLIME, ExemptType.TELEPORT, ExemptType.DEAD);
            boolean inventoryOpen = data.getActionProcessor().isInventoryOpen();

            double deltaXZ = data.getPositionProcessor().getDeltaXZ();
            double max = 0.28 + data.getPotionProcessor().getSpeedBoostAmplifier() * 0.06;

            if (inventoryOpen && deltaXZ > max && !exempt) {
                if (increaseBuffer() > 8.0) {
                    fail("dXZ=" + deltaXZ + " max=" + max);
                }
            } else {
                decreaseBufferBy(1.75);
            }
        }
    }
}

