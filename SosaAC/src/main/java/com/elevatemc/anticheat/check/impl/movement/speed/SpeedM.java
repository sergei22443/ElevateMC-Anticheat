package com.elevatemc.anticheat.check.impl.movement.speed;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.server.block.BlockUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Speed", type = "M", experimental = true, description = "Checks for invalid movement")
public class SpeedM extends Check {

    int delay;

    public SpeedM(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            boolean exempt = isExempt(ExemptType.CREATIVE, ExemptType.VELOCITY, ExemptType.FLIGHT, ExemptType.JOINED, ExemptType.TELEPORT, ExemptType.COLLIDING_VERTICALLY);

            if (exempt) resetBuffer();

            boolean onGround = data.getPositionProcessor().isClientOnGround() || data.getPositionProcessor().isMathematicallyOnGround();

            double deltaX = data.getPositionProcessor().getDeltaX(), deltaZ = data.getPositionProcessor().getDeltaZ();
            double horizontalOffset = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

            double maxDelta = onGround ? 0.34 : 0.36;

            int speedLevel = data.getPotionProcessor().getSpeedBoostAmplifier();

            if (speedLevel > 0) {
                delay = 0;
            } else if (++delay > data.getConnectionProcessor().getPingTicks() * 5) {
                speedLevel = 0;
            }

            if (BlockUtil.isStair(data)) {
                maxDelta = 0.45;
            } else if (data.getPositionProcessor().getSinceNearIceTicks() < 40) {
                maxDelta = isExempt(ExemptType.COLLIDING_VERTICALLY) ? 1.3 : 0.8;
            } else if (isExempt(ExemptType.COLLIDING_VERTICALLY)) {
                maxDelta = 0.7;
            }

            maxDelta += (onGround ? 0.06 : 0.02) * speedLevel;

            float walkSpeed = data.getPlayer().getWalkSpeed();

            if (walkSpeed > 0.2F) maxDelta += walkSpeed * 10F * 0.33F;

            if (horizontalOffset > maxDelta && !exempt) {
                if (increaseBuffer() > 18) {
                    fail("d=" + horizontalOffset + " max=" + maxDelta);
                    decreaseBufferBy(2.0);
                }
            } else {
               decreaseBufferBy(.25);
            }
        }
    }
}
