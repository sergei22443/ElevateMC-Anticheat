package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Motion", type = "M", description = "Checks for invalid motion whilst jumping")
public class MotionM extends Check {

    public MotionM(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            double deltaY = data.getPositionProcessor().getDeltaY();

            boolean hasJumped = data.getPositionProcessor().isJumped();
            boolean currentlyTakingKB = data.getVelocityProcessor().getFlyingVelocityTicks() <= 5;

            if (hasJumped) {
                double expectedY = deltaY - 0.42;
                //debug(expectedY);

                if (Math.abs(expectedY) < 1.7E-14) {
                    fail("deltaY=" + deltaY + " kb=" + currentlyTakingKB);
                }
            }
        }
    }
}
