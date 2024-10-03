package com.elevatemc.anticheat.check.impl.movement.speed;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.elevatemc.anticheat.util.server.block.BlockUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Speed", type = "K", experimental = true, description = "Checks for invalid deltas")
public class SpeedK extends Check {

    public SpeedK(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            double maxDelta = 0.41;
            double deltaY = data.getPositionProcessor().getDeltaY(), lastDeltaY = data.getPositionProcessor().getLastDeltaY();

            if (deltaY == 0.009999990463242625 || deltaY == 0.41999998688697815 || deltaY == 0.35999999999999943
                    || deltaY == 0.07840000152587834 || deltaY == 0.019499999999993634 || deltaY == 0.0019999980926428407
                    || deltaY == 0.33319999363422337 || deltaY == 0.039851049416299134 || deltaY == 0.039345972337798685
                    || deltaY == lastDeltaY || deltaY == 0.33319999363422426) return;
            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.JOINED, ExemptType.CREATIVE, ExemptType.SPECTATOR, ExemptType.FLIGHT, ExemptType.LIQUID);

            //debug(data.getPositionProcessor().getDeltaXYZ());
            maxDelta += data.getPotionProcessor().getJumpBoostAmplifier() * 0.2;
            maxDelta += data.getPotionProcessor().getSpeedBoostAmplifier() * 0.0625;

            if (deltaY == 0.07840000152587923) {
                maxDelta += 0.125;
            }
            if (deltaY == 0.41999998688697815) {
                maxDelta += 0.1;
            }

            if (BlockUtil.isStair(data)) {
                maxDelta += 0.125;
            }
            if (deltaY == 0.5) {
                maxDelta += 0.1;
            }
            if (BlockUtil.isSlab(data.getPlayer().getLocation().getBlock().getType())) {
                maxDelta += 0.1;
            }
            if (PlayerUtil.getPing(data.getPlayer()) > 500 && data.getVelocityProcessor().getTicksSinceVelocity() < 15) {
                maxDelta += 10.0;
            }
            if (isExempt(ExemptType.SLIME)) {
                maxDelta += 0.05;
            }
            if (deltaY == 0.0) {
                maxDelta += 0.2;
            }
            if (data.getVelocityProcessor().getTicksSinceVelocity() < 15) {
                maxDelta += 0.8;
            }
            if (data.getPositionProcessor().getSinceNearIceTicks() < 50 && BlockUtil.isSlab(data.getPlayer().getLocation().getBlock().getType())) {
                maxDelta += 0.25;
            }
            if ((deltaY == 0.07840000152587834 && BlockUtil.isSlab(data.getPlayer().getLocation().getBlock().getType())) || (deltaY == 0.07840000152587923 && BlockUtil.isSlab(data.getPlayer().getLocation().getBlock().getType()))) {
                maxDelta += 0.03;
            }
            if (data.getPositionProcessor().getDeltaY() == 0.5 && BlockUtil.isSlab(data.getPlayer().getLocation().getBlock().getType())) {
                maxDelta += 0.5;
            }
            if (data.getPositionProcessor().getSinceNearIceTicks() < 50) {
                maxDelta += 0.25;
            }
            if (data.getPlayer().getWalkSpeed() > 0.2f) {
                maxDelta += data.getPlayer().getWalkSpeed() * 0.28634357f * 10.0f;
            }
            if (isExempt(ExemptType.COLLIDING_VERTICALLY)) {
                maxDelta += 0.55;
            }
            if (data.getPositionProcessor().getSinceNearIceTicks() < 20) {
                maxDelta += 0.55;
            }
            if (isExempt(ExemptType.COLLIDING_HORIZONTALLY) && data.getPositionProcessor().getDeltaY() < 0.2) {
                maxDelta += 0.35;
            }
            double deltaXZ = data.getPositionProcessor().getDeltaXZ();

            if (deltaXZ > maxDelta + 1.0 && !exempt) {
                if (increaseBuffer() > 1) fail("dXZ=" + data.getPositionProcessor().getDeltaXZ() + " max=" + maxDelta + " dY=" + data.getPositionProcessor().getDeltaY() + " fd=" + data.getConnectionProcessor().getFlyingDelay());
            } else {
                decreaseBuffer();
            }
        }
    }
}
