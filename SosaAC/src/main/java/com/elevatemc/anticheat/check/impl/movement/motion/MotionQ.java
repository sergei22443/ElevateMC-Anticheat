package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.util.Vector;


@CheckInfo(name = "Motion", type = "Q", description = "Checks for invalid sprint actions")
public class MotionQ extends Check {

    public MotionQ(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION || packet.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {

            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.JOINED, ExemptType.TPS, ExemptType.LAG_SPIKE, ExemptType.FLIGHT, ExemptType.VELOCITY);

            if (!exempt && !data.getActionProcessor().isSprinting() && (!data.getPositionProcessor().isClientOnGround() || !data.getPositionProcessor().isMathematicallyOnGround())) {

                float deltaYaw = data.getRotationProcessor().getDeltaYaw(), deltaPitch = data.getRotationProcessor().getDeltaPitch();

                if (deltaPitch > 30.0f || deltaYaw > 30.0f) resetBuffer();

                Vector movement = new Vector(data.getPositionProcessor().getDeltaX(), 0.0, data.getPositionProcessor().getDeltaZ());
                Vector direction = new Vector(-Math.sin(data.getPlayer().getEyeLocation().getYaw() * 3.1415927f / 180.0f) * 1.0 * 0.5, 0.0, Math.cos(data.getPlayer().getEyeLocation().getYaw() * 3.1415927f / 180.0f) * 1.0 * 0.5);

                //debug(movement.distanceSquared(direction));
                //debug(deltaYaw);
                //debug(deltaPitch);

                double delta = movement.distanceSquared(direction);
                //debug(delta);
                //debug(data.getPositionProcessor().getDeltaXZ());
                double maxDelta = 0.294;

                if (hitTicks() < 4) {
                    //debug(delta);
                    maxDelta += 0.12;
                }
                if (data.getPlayer().getWalkSpeed() > 0.2f) {
                    //debug(delta);
                    maxDelta += data.getPlayer().getWalkSpeed() * 0.28634357f * 4.0f;
                }
                if (data.getPositionProcessor().getSinceNearIceTicks() < 5) {
                    //debug(delta);
                    maxDelta += 0.42;
                }

                boolean invalid = delta > maxDelta + 0.7 && data.getPositionProcessor().getDeltaXZ() > 0.2095;

                if (invalid) {
                    if (increaseBuffer() > 15.0) {
                        fail("delta=" + delta + " max=" + maxDelta + " dXZ=" + data.getPositionProcessor().getDeltaXZ());
                    }
                } else {
                    decreaseBuffer();
                }
            }
        }
    }
}
