package com.elevatemc.anticheat.check.impl.movement.speed;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.*;

@CheckInfo(name = "Speed", type = "B", description = "Moving too quickly on the ground.")
public class SpeedB extends Check
{
    public SpeedB(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            double deltaXZ = this.data.getPositionProcessor().getDeltaXZ();
            double deltaY = this.data.getPositionProcessor().getDeltaY();
            int groundTicks = this.data.getPositionProcessor().getClientGroundTicks();
            double maxSpeed = PlayerUtil.getBaseGroundSpeed(data.getPlayer());
            List<String> tags = new ArrayList<>();

            switch (groundTicks) {
                case 0: {
                    maxSpeed += 0.333;
                    if (deltaY > 0.4) {
                        maxSpeed += 0.02;
                        break;
                    }
                    break;
                }
                case 1: {
                    maxSpeed += 0.0275;
                    break;
                }
                case 2: {
                    maxSpeed += 0.145;
                    break;
                }
                case 3: {
                    maxSpeed += 0.075;
                    break;
                }
                case 4: {
                    maxSpeed += 0.045;
                    break;
                }
                case 5: {
                    maxSpeed += 0.0305;
                    break;
                }
                case 6: {
                    maxSpeed += 0.015;
                    break;
                }
                case 7: {
                    maxSpeed += 0.01;
                    break;
                }
                case 8: {
                    maxSpeed += 0.007;
                    break;
                }
                case 9: {
                    maxSpeed += 0.005;
                    break;
                }
            }
            boolean velocity = data.getVelocityProcessor().getTicksSinceVelocity() < 15;
            if (velocity) {
                tags.add("Velocity");
                final double velocityXZ = Math.hypot(Math.abs(data.getVelocityProcessor().getVelocityX()), data.getVelocityProcessor().getVelocityZ());
                maxSpeed += velocityXZ + 0.15;
            }
            boolean ice = data.getPositionProcessor().getSinceNearIceTicks() < 50;
            if (ice) {
                tags.add("Ice");
                maxSpeed += 0.85;
            }
            boolean collidingVertically = data.getPositionProcessor().getSinceCollidingVerticallyTicks() < 50;
            if (collidingVertically) {
                tags.add("Vertical collision");
                ++maxSpeed;
            }
            boolean slime = data.getPositionProcessor().getSinceNearSlimeTicks() < 50;
            if (slime) {
                tags.add("Slime");
                ++maxSpeed;
            }
            boolean trapdoor = this.data.getPositionProcessor().getSinceNearTrapdoorTicks() < 50;
            if (trapdoor) {
                tags.add("Trapdoor");
                ++maxSpeed;
            }
            boolean placed = this.isExempt(ExemptType.BLOCK_PLACE);
            if (placed) {
                tags.add("Block Place");
                maxSpeed += 0.125;
            }
            if (isExempt(ExemptType.DEPTH_STRIDER)) {
                tags.add("Depth Strider");
                maxSpeed += 0.25;
            }

            boolean exempt = isExempt(ExemptType.FLIGHT, ExemptType.CREATIVE, ExemptType.TELEPORT, ExemptType.POTION_EXPIRE, ExemptType.LAG_SPIKE, ExemptType.FAST);
            double difference = deltaXZ - maxSpeed;
            boolean invalid = difference > 1.0E-5 && groundTicks >= 0;

            if (invalid && !exempt) {
                if (increaseBuffer() > 10.0) {
                    multiplyBuffer(0.25);
                    increaseVlBy(.45);
                    fail("diff=" + difference + " ticks=" + groundTicks + " max=" + maxSpeed + " tags=" + tags + " deltaY=" + deltaY);
                    staffAlert();
                }
            }
            else {
                decreaseBufferBy(0.075);
            }
        }
    }
}
