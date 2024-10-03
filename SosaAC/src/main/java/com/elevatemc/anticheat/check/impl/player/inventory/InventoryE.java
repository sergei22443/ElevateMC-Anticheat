package com.elevatemc.anticheat.check.impl.player.inventory;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckInfo(name = "Inventory", type = "E", description = "Checks if a player is attacking whilst their inventory is open")
public class InventoryE extends Check {

    public InventoryE(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(packet);

            boolean isAttacking = wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK;
            boolean isInventoryOpen = data.getActionProcessor().isInventoryOpen();

            if (isAttacking && isInventoryOpen) {
                fail("");
            }
        }
    }
}
