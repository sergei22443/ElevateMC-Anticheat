package com.elevatemc.anticheat.data.processor.entity;

import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.reach.data.PlayerReachEntity;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTracker {
    private final PlayerData data;
    public PlayerTracker(PlayerData player) {
        data = player;
    }
    public final Map<Integer, PlayerReachEntity> entityMap = new ConcurrentHashMap<>();
    private boolean hasSentPreWavePacket = true;
    public void handleSpawnPlayer(final WrapperPlayServerSpawnPlayer wrapper) {
        Vector3d position = wrapper.getPosition();

        int lastTransactionSent = data.getConnectionProcessor().getLastTransactionSent().get();
        data.getConnectionProcessor().addTransactionHandler(data.getConnectionProcessor().getLastTransactionSent().get(), () -> {
            PlayerReachEntity reachEntity = new PlayerReachEntity(data, position.getX(), position.getY(), position.getZ());
            reachEntity.setLastTransactionHung(lastTransactionSent);
            this.entityMap.put(wrapper.getEntityId(), reachEntity);
        });
    }

    public void handleDestroyEntity(final WrapperPlayServerDestroyEntities wrapper) {
        data.getConnectionProcessor().addTransactionHandler(data.getConnectionProcessor().getLastTransactionSent().get() + 1, () -> {
            for (int entityId : wrapper.getEntityIds()) {
                entityMap.remove(entityId);
            }
        });
    }

    public void handleRelEntityMove(final WrapperPlayServerEntityRelativeMove wrapper) {
        handleRelMove(wrapper.getEntityId(), wrapper.getDeltaX(), wrapper.getDeltaY(), wrapper.getDeltaZ());
    }

    public void handleEntityLook(final WrapperPlayServerEntityRotation wrapper) {
        final PlayerReachEntity trackedPlayer = this.entityMap.get(wrapper.getEntityId());
        if (trackedPlayer != null) {
            this.handleMoveEntity(trackedPlayer, 0, 0, 0, true, false);
        }
    }

    public void handleRelEntityMoveLook(final WrapperPlayServerEntityRelativeMoveAndRotation wrapper) {
        handleRelMove(wrapper.getEntityId(), wrapper.getDeltaX(), wrapper.getDeltaY(), wrapper.getDeltaZ());
    }

    private void handleRelMove(int entityId, double deltaX, double deltaY, double deltaZ) {
        final PlayerReachEntity trackedPlayer = this.entityMap.get(entityId);
        if (trackedPlayer != null) {
            this.handleMoveEntity(trackedPlayer, deltaX, deltaY, deltaZ, true, true);
        }
    }

    public void handleTeleportEntity(final WrapperPlayServerEntityTeleport wrapper) {
        final PlayerReachEntity trackedPlayer = this.entityMap.get(wrapper.getEntityId());
        if (trackedPlayer != null) {
            final Vector3d pos = wrapper.getPosition();
            this.handleMoveEntity(trackedPlayer, pos.getX(), pos.getY(), pos.getZ(), false, true);
        }
    }

    private void handleMoveEntity(PlayerReachEntity trackedPlayer, final double deltaX, final double deltaY, final double deltaZ, boolean isRelative, boolean hasPos) {
        if (!hasSentPreWavePacket) {
            hasSentPreWavePacket = true;
            data.getConnectionProcessor().sendTransaction();
        }

        if (trackedPlayer.getLastTransactionHung() == data.getConnectionProcessor().getLastTransactionSent().get()) {
            // System.out.println("double rel move which shouldnt happen since entity tracker shouldnt be dumb");
            // Commented out top line because our npc system is dumb and sends lots of dumb packets - Beanes
            data.getConnectionProcessor().sendTransaction();
        }
        trackedPlayer.setLastTransactionHung(data.getConnectionProcessor().getLastTransactionSent().get());

        int lastTrans = data.getConnectionProcessor().getLastTransactionSent().get(); // We send one at tick start

        data.getConnectionProcessor().addTransactionHandler(lastTrans, () -> {
            trackedPlayer.onFirstTransaction(isRelative, hasPos, deltaX, deltaY, deltaZ, data);
        });

        data.getConnectionProcessor().addTransactionHandler(lastTrans + 1, () -> {
            trackedPlayer.onSecondTransaction();
        });
    }

    public void handleFlying() {
        if (data.getPositionProcessor().isTeleporting()) {
            return;
        }

        for (final PlayerReachEntity entity : data.getPlayerTracker().entityMap.values()) {
            entity.onMovement(true);
        }
    }

    public void postEntityTracker() {
        data.getConnectionProcessor().sendTransaction(true);
    }

    public void onTickStart() {
        hasSentPreWavePacket = false;
    }
}
