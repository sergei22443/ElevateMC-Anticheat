package com.elevatemc.anticheat.check.impl.combat.velocity;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.data.processor.player.VelocityProcessor;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;

@CheckInfo(name = "Velocity", type = "A", description = "Checks for vertical velocity")
public class VelocityA extends Check {

    public VelocityA(PlayerData data) {
        super(data);
    }

    private static final double THRESHOLD = 0.00000000001;
    private int tick = Integer.MIN_VALUE;
    private double bestAccuracy = Double.MAX_VALUE;
    private int bestRatio = Integer.MAX_VALUE;
    private double nextMotionY = 0;
    private double bufferOtherTicks = 0;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (tick > 0) {
                // We check if we can verify velocity
                if (!data.getVelocityProcessor().canVerifyVelocity()) {
                    reset();
                    return;
                }

                // We calculate the accuracy of the next y movement
                double deltaY = data.getPositionProcessor().getDeltaY();
                double accuracy = Math.abs(deltaY - nextMotionY);

                // If the simulation failed we will flag otherwise we will process the next tick
                if (accuracy > THRESHOLD) {
                    if (bufferOtherTicks++ > 7) {
                        int ratio = (int) Math.abs(Math.round((deltaY / nextMotionY) * 100));
                        fail("tick=" + tick + " ratio=" + ratio + "%");
                    }

                    reset();
                } else {
                    // We simulate the next tick motionY beforehand
                    boolean willSendMotion = simulateMotionY();

                    if (willSendMotion) {
                        tick++;
                    } else {
                        reset();
                        bufferOtherTicks = Math.max(0, bufferOtherTicks - 0.25D);
                    }
                }
            } else if (tick == 0) {
                // We first check if velocity processor assumes we can still process velocity
                if (data.getVelocityProcessor().getState() != VelocityProcessor.State.IDLE) {
                    // Basically we are uncertain which tick the velocity will be processed on, so we check every client tick until we have a correct motion
                    double deltaY = data.getPositionProcessor().getDeltaY();
                    double velocityY = data.getVelocityProcessor().getVelocityY();
                    double accuracy = Math.abs(deltaY - velocityY);

                    // We check the accuracy of the upwards motion to the velocity, if this was correct we stop processing the first tick and go on to process all the next ticks
                    if (accuracy < THRESHOLD) {
                        // We reset the best accuracy
                        bestAccuracy = Double.MAX_VALUE;

                        // We simulate the next tick motionY beforehand
                        boolean willSendMotion = simulateMotionY();

                        if (willSendMotion) {
                            tick = 1;
                        } else {
                            reset();
                        }

                        // Valid velocity
                        decreaseBufferBy(0.25);

                        return;
                    }

                    // We keep track of the best accuracy so if they flag we know their setting
                    if (accuracy < bestAccuracy) {
                        bestAccuracy = accuracy;
                        bestRatio = (int) Math.round((deltaY / velocityY) * 100);
                    }

                    // Our last movement has been processed but none of the movements were correct
                    if (data.getVelocityProcessor().getState() == VelocityProcessor.State.SANDWICHED) {
                        if (increaseBuffer() > 5) {
                            fail("tick=0 ratio=" + bestRatio + "%");
                        }
                        reset();
                    }
                }
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_VELOCITY) {
            final WrapperPlayServerEntityVelocity wrapper = new WrapperPlayServerEntityVelocity(event);
            if (wrapper.getEntityId() == data.getPlayer().getEntityId()) {
                if (data.getVelocityProcessor().canVerifyVelocity()) {
                    data.getConnectionProcessor().addTransactionHandler(data.getConnectionProcessor().getLastTransactionSent().get(), () -> {
                        reset();
                        tick = 0;
                    });
                } else {
                    reset();
                }
            }

        }
    }

    // This will return false if the next simulation is under 0.03
    private boolean simulateMotionY() {
        double deltaY = data.getPositionProcessor().getDeltaY();
        nextMotionY = deltaY;
        nextMotionY -= 0.08D;
        nextMotionY *= 0.9800000190734863D;

        // The client will not send a position if position change is under 0.03 (we could make this more accurate if we track horizontal and vertical in the same check)
        return nextMotionY > 0.03;
    }

    private void reset() {
        tick = Integer.MIN_VALUE;
        bestAccuracy = Double.MAX_VALUE;
    }
}