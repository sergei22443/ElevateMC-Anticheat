package com.elevatemc.anticheat.check.impl.movement.speed;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.elevatemc.anticheat.util.server.block.BlockUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.Material;

@CheckInfo(name = "Speed", type = "J", description = "Checks for invalid speed appliance")
public class SpeedJ extends Check {

    public SpeedJ(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            boolean exempt = isExempt(ExemptType.JOINED, ExemptType.TELEPORT, ExemptType.POTION_EXPIRE, ExemptType.DEPTH_STRIDER, ExemptType.FLIGHT
                    , ExemptType.FAST);

            if (exempt) return;

            double maxDelta = 0.432;
            if (data.getPlayer().getWalkSpeed() > 0.2f) {
                maxDelta += data.getPlayer().getWalkSpeed() * 0.28634357f * 4.0f;
            }
            maxDelta += data.getPotionProcessor().getSpeedBoostAmplifier() * 0.0625;
            if (data.getPositionProcessor().isNearStair()) {
                maxDelta += 0.5;
            }
            if (data.getPositionProcessor().isCollidingVertically()) {
                maxDelta += 0.35;
            }
            if (hitTicks() <= 1) {
                maxDelta += 0.6;
            }
            if (PlayerUtil.getPing(data.getPlayer()) > 500 && data.getVelocityProcessor().getFlyingVelocityTicks() <= 3) {
                maxDelta += 10.5 * 1;
            }
            if (!data.getPositionProcessor().getBlocksAbove().contains(Material.AIR)) {
                maxDelta += 0.06;
            }
            if (data.getPositionProcessor().isNearTrapdoor() && data.getPositionProcessor().getDeltaY() >= 0.41999998688697815) {
                maxDelta += 0.25;
            }
            if (data.getPositionProcessor().isNearTrapdoor()) {
                maxDelta += 0.03;
            }
            if (data.getPositionProcessor().isNearIce() || data.getPositionProcessor().getSinceNearIceTicks() < 15) {
                maxDelta += 0.25;
            }
            if (!BlockUtil.isIce(data.getPlayer().getLocation().getBlock().getType())) {
                maxDelta += 0.15;
            }
            if (data.getPositionProcessor().getDeltaXZ() > maxDelta && data.getPositionProcessor().getClientGroundTicks() > 2) {
                fail( data.getPositionProcessor().getDeltaXZ() + " > " + maxDelta + " gt=" + data.getPositionProcessor().getClientGroundTicks() + " fd=" + data.getConnectionProcessor().getFlyingDelay());
            }
        }
    }
}
