package com.elevatemc.anticheat.check.impl.movement.speed;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.server.block.BlockUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Speed", type = "H", description = "Checks for invalid speed appliance")
public class SpeedH extends Check {

    public SpeedH(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            final boolean exempt = isExempt(ExemptType.JOINED, ExemptType.TELEPORT, ExemptType.POTION_EXPIRE, ExemptType.DEPTH_STRIDER, ExemptType.FLIGHT)
                    || data.getPositionProcessor().getDeltaY() == 0.039851049416299134
                    || data.getConnectionProcessor().getFlyingDelay() < 2L;

            if (isExempt(ExemptType.SLIME)) {
                return;
            }

            if (!exempt) {
                double deltaY = data.getPositionProcessor().getDeltaY();
                double limit = 0.63;

                if (BlockUtil.isStair(data.getPlayer().getLocation().getBlock().getType())) {
                    limit += .05;
                }
                if (data.getPlayer().getWalkSpeed() > 0.2f) {
                    limit += data.getPlayer().getWalkSpeed() * 0.28634357f * 4.0f;
                }
                if (data.getPositionProcessor().getSinceNearIceTicks() < 15) {
                    limit += 0.15;
                }
                if (data.getPotionProcessor().getSpeedBoostAmplifier() > 0 && deltaY == 0.5 && isExempt(ExemptType.TRAPDOOR)) {
                    limit += 0.25;
                }
                if (isExempt(ExemptType.COLLIDING_VERTICALLY)) {
                    limit += 0.75;
                }
                if (data.getVelocityProcessor().getFlyingVelocityTicks() <= 2) {
                    limit += 0.65;
                }
                limit += data.getPotionProcessor().getSpeedBoostAmplifier() * 0.06;

                double deltaXZ = data.getPositionProcessor().getDeltaXZ();

                if (deltaXZ > limit ) {
                    if (increaseBuffer() > 14.0) {
                        fail("deltaXZ=" + deltaXZ);
                        staffAlert();
                    }
                } else {
                    decreaseBufferBy(.25);
                }
            }
        }
    }
}
