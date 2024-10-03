package com.elevatemc.anticheat.processor.processor;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.*;

public class ReceivingPacketProcessor
{
    public void handle(final PlayerData data, final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction wrapper = new WrapperPlayClientEntityAction(event);
            data.getActionProcessor().handleEntityAction(wrapper);
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
            data.getActionProcessor().handleBlockDig(wrapper);
        }

        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            data.getActionProcessor().handleWindowClick();
        }

        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            data.getClickProcessor().handleArmAnimation();
            data.getActionProcessor().handleArmAnimation();
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            data.getActionProcessor().handleBlockPlace();
        }

        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper3 = new WrapperPlayClientInteractEntity(event);
            data.getCombatProcessor().handleEntityInteract(wrapper3);
        }

        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
            data.getPositionProcessor().handleFlying(wrapper);
            data.getCombatProcessor().handleFlying();
            data.getConnectionProcessor().handleFlying();
            data.getActionProcessor().handleFlying();
            data.getVelocityProcessor().handleFlying();
            if (wrapper.hasRotationChanged()) {
                data.getRotationProcessor().handle(wrapper.getLocation().getYaw(), wrapper.getLocation().getPitch());
                data.getSensitivityProcessor().handle(wrapper.getLocation().getYaw(), wrapper.getLocation().getPitch());
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS) {
            WrapperPlayClientClientStatus wrapper = new WrapperPlayClientClientStatus(event);
            data.getActionProcessor().handleClientCommand(wrapper);
        }

        if (event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            WrapperPlayClientWindowConfirmation wrapper = new WrapperPlayClientWindowConfirmation(event);
            if (data.getConnectionProcessor().handleTransaction(wrapper)) {
             event.setCancelled(true);
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
            WrapperPlayClientKeepAlive wrapper = new WrapperPlayClientKeepAlive(event);
            data.getConnectionProcessor().handleKeepAlive(wrapper);
        }

        for (Check check : data.getChecks()) {
            check.onPacketReceive(event);
        }

        // Processed post reach check & velocity
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            data.getPlayerTracker().handleFlying();
        }
    }
}
