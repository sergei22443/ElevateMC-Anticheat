package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Motion", type = "R", experimental = true, description = "")
public class MotionR extends Check {

    public MotionR(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION || packet.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {

            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.COLLIDING_VERTICALLY, ExemptType.VELOCITY, ExemptType.VEHICLE, ExemptType.DEAD, ExemptType.FLIGHT, ExemptType.CREATIVE);

            if (!exempt) {
                double deltaY = data.getPositionProcessor().getDeltaY(), lastDeltaY = data.getPositionProcessor().getLastDeltaY();

                double yDifference = Math.abs(deltaY - lastDeltaY);

                //debug("dY=" + deltaY);
                //debug("diff=" + yDifference);

                boolean invalid = deltaY == yDifference && deltaY > 1.0 && data.getPotionProcessor().getJumpBoostAmplifier() < 2;

                if (invalid) {
                    if (increaseBuffer() > 2.0) {
                        fail("dY=" + deltaY + " diff=" + yDifference);
                        resetBuffer();
                    }
                }
            }
        }
    }
}
