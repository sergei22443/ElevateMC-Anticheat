package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Motion", type = "O", experimental = true, description = "Gravity simulation")
public class MotionO extends Check {

    public MotionO(PlayerData data) {
        super(data);
    }

    private double nextMotionY = 0;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (!data.getPositionProcessor().isClientOnGround()) {
                boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.FLIGHT, ExemptType.JOINED, ExemptType.COLLIDING_VERTICALLY, ExemptType.VELOCITY, ExemptType.LIQUID);
                if (exempt) {
                    nextMotionY = 0;
                    return;
                }

                double deltaY = data.getPositionProcessor().getDeltaY();
                double accuracy = Math.abs(deltaY - nextMotionY);

                if (nextMotionY != 0 && accuracy > 1e-7) {
                    if (increaseBuffer() > 4.0) fail("accuracy=" + accuracy);
                }

                // We reset to the client value to make sure the accuracy is always on top
                nextMotionY = deltaY;
                nextMotionY -= 0.08D;
                nextMotionY *= 0.9800000190734863D;

                if (nextMotionY < 0.03) {
                    nextMotionY = 0;
                }
            } else {
                nextMotionY = 0;
            }
        }
    }
}
