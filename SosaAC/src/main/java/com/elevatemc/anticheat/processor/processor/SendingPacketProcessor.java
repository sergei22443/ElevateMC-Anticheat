package com.elevatemc.anticheat.processor.processor;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.*;


public class SendingPacketProcessor
{
    public void handle(final PlayerData data, final PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            data.getVelocityProcessor().handle(event);
        }

        if (event.getPacketType() == PacketType.Play.Server.PLAYER_POSITION_AND_LOOK) {
            WrapperPlayServerPlayerPositionAndLook wrapper = new WrapperPlayServerPlayerPositionAndLook(event);
            data.getPositionProcessor().handleServerPosition(wrapper);
            data.getActionProcessor().handleTeleport(wrapper);
        }

        if (event.getPacketType() == PacketType.Play.Server.KEEP_ALIVE) {
            WrapperPlayServerKeepAlive wrapper = new WrapperPlayServerKeepAlive(event);
            data.getConnectionProcessor().handleOutboundKeepAlive(wrapper);
        }

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_EFFECT) {
            WrapperPlayServerEntityEffect wrapper = new WrapperPlayServerEntityEffect(event);
            data.getPotionProcessor().handleEffect(wrapper);
        }

        if (event.getPacketType() == PacketType.Play.Server.REMOVE_ENTITY_EFFECT) {
            WrapperPlayServerRemoveEntityEffect wrapper = new WrapperPlayServerRemoveEntityEffect(event);
            data.getPotionProcessor().handleRemoveEffect(wrapper);
        }

        if (event.getPacketType() == PacketType.Play.Server.RESPAWN) {
            data.getPotionProcessor().handleRespawn();
        }

        if (event.getPacketType() == PacketType.Play.Server.SPAWN_PLAYER) {
            WrapperPlayServerSpawnPlayer wrapper = new WrapperPlayServerSpawnPlayer(event);
            data.getPlayerTracker().handleSpawnPlayer(wrapper);
        }

        if (event.getPacketType() == PacketType.Play.Server.DESTROY_ENTITIES) {
            WrapperPlayServerDestroyEntities wrapper = new WrapperPlayServerDestroyEntities(event);
            data.getPlayerTracker().handleDestroyEntity(wrapper);
        }

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
            WrapperPlayServerEntityRelativeMove wrapper = new WrapperPlayServerEntityRelativeMove(event);
            data.getPlayerTracker().handleRelEntityMove(wrapper);
        }

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_ROTATION) {
            WrapperPlayServerEntityRotation wrapper = new WrapperPlayServerEntityRotation(event);
            data.getPlayerTracker().handleEntityLook(wrapper);
        }

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
            WrapperPlayServerEntityRelativeMoveAndRotation wrapper = new WrapperPlayServerEntityRelativeMoveAndRotation(event);
            data.getPlayerTracker().handleRelEntityMoveLook(wrapper);
        }

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {
            WrapperPlayServerEntityTeleport wrapper = new WrapperPlayServerEntityTeleport(event);
            data.getPlayerTracker().handleTeleportEntity(wrapper);
        }

        if (event.getPacketType() == PacketType.Play.Server.WINDOW_CONFIRMATION) {
            WrapperPlayServerWindowConfirmation wrapper = new WrapperPlayServerWindowConfirmation(event);
            data.getConnectionProcessor().handleOutboundTransaction(wrapper);
        }

        for (Check check : data.getChecks()) {
            check.onPacketSend(event);
        }
    }
}