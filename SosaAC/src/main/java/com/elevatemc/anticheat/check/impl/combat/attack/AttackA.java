package com.elevatemc.anticheat.check.impl.combat.attack;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.util.reach.data.PlayerReachEntity;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.reach.data.ReachUtils;
import com.elevatemc.anticheat.util.reach.box.SimpleCollisionBox;

import java.util.*;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.util.Vector;

// This code was from EdgeAC which is actually just skidded grim.ac lol, well it is what it is
@CheckInfo(name = "Attack", type = "A", description = "Player is attacking outside range or hitbox")
public class AttackA extends Check
{
    private final Map<Integer, Vector3d> playerAttackQueue = new LinkedHashMap<>();
    private int sinceNoMove = 0;

    private static final double[] eyeHeights = new double[] {(double) (1.62f - 0.08f), (double) (1.62f)};

    public AttackA(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {

        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            if (isExempt(ExemptType.CREATIVE, ExemptType.VEHICLE, ExemptType.TELEPORT)) {
                return;
            }

            final WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);

            if (wrapper.getAction().equals(WrapperPlayClientInteractEntity.InteractAction.ATTACK)) {
                int entityId = wrapper.getEntityId();
                if (data.getPlayerTracker().entityMap.containsKey(entityId)) {
                    playerAttackQueue.put(entityId, new Vector3d(data.getPositionProcessor().getX(), data.getPositionProcessor().getY(), data.getPositionProcessor().getZ())); // Queue for next tick for very precise check
                }
            }
        } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            boolean isNotMoving = event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() ==PacketType.Play.Client.PLAYER_FLYING;
            if (isNotMoving) {
                sinceNoMove = 0;
            } else {
                sinceNoMove++;
            }

            if (!data.getPositionProcessor().isTeleporting()) {
                this.tickFlying();
            } else {
                playerAttackQueue.clear();
            }
        }
    }

    public void tickFlying() {
        for (Map.Entry<Integer, Vector3d> attack : playerAttackQueue.entrySet()) {
            final PlayerReachEntity reachEntity = data.getPlayerTracker().entityMap.get(attack.getKey());
            final SimpleCollisionBox targetBox = reachEntity.getPossibleCollisionBoxes();
            // Bukkit.broadcastMessage("target: " + targetBox);
            double widthX = Math.abs(Math.abs(targetBox.minX) - Math.abs(targetBox.maxX));
            double widthZ = Math.abs(Math.abs(targetBox.minZ) - Math.abs(targetBox.maxZ));
            double height = Math.abs(Math.abs(targetBox.minY) - Math.abs(targetBox.maxY));
            // Bukkit.broadcastMessage("extra target info widthX=" + widthX + " widthY=" + height + " widthZ=" + widthZ);

            targetBox.expand(0.1f); // 1.8 / 1.7 hitbox is 0.1 bigger
            targetBox.expand(0.005); // hitbox treshold

            // Accounts for 0.03
            if (!data.getPositionProcessor().isLastMovementIncludedPosition()) {
                targetBox.expand(0.03);
            }

            // Bukkit.broadcastMessage("from=" + attack.getValue());
            final Vector3d from = attack.getValue();
            double minDistance = Double.MAX_VALUE;
            final List<Vector> possibleLookDirs = new ArrayList<>();

            // All possible 1.7 / 1.8 look directions (includes the look desync)
            possibleLookDirs.add(ReachUtils.getLook(data.getRotationProcessor().getYaw(), data.getRotationProcessor().getPitch(), false, data.getUser().getClientVersion()));
            possibleLookDirs.add(ReachUtils.getLook(data.getRotationProcessor().getYaw(), data.getRotationProcessor().getPitch(), true, data.getUser().getClientVersion()));
            possibleLookDirs.add(ReachUtils.getLook(data.getRotationProcessor().getLastYaw(), data.getRotationProcessor().getPitch(), false, data.getUser().getClientVersion()));
            possibleLookDirs.add(ReachUtils.getLook(data.getRotationProcessor().getLastYaw(), data.getRotationProcessor().getPitch(), true, data.getUser().getClientVersion()));

            for (final Vector lookVec : possibleLookDirs) {
                for (final double eye : eyeHeights) {
                    Vector eyePos = new Vector(from.getX(), from.getY() + eye, from.getZ());
                    Vector endReachPos = eyePos.clone().add(new Vector(lookVec.getX() * 6, lookVec.getY() * 6, lookVec.getZ() * 6));

                    Vector intercept = ReachUtils.calculateIntercept(targetBox, eyePos, endReachPos).getX();

                    if (ReachUtils.isVecInside(targetBox, eyePos)) {
                        minDistance = 0;
                        break;
                    }

                    if (intercept != null) {
                        minDistance = Math.min(eyePos.distance(intercept), minDistance);
                    }
                }
            }

            //debug("lol=" + minDistance);
            if (minDistance == Double.MAX_VALUE) {
                if (increaseBuffer() > 6) {
                    fail("Missed hitbox");
                    resetBuffer();
                }
            } else if (minDistance > 3.005) {
                fail("dist=" + minDistance + " sinceNoMove=" + sinceNoMove);
            }
        }

        playerAttackQueue.clear();
    }

}
