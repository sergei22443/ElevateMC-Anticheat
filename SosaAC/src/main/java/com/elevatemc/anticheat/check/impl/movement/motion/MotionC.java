package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Motion", type = "C", description = "Checks if a player is going up a ladder faster than possible")
public class MotionC extends Check {

    public MotionC(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            boolean exempt = isExempt(ExemptType.FLIGHT, ExemptType.CREATIVE, ExemptType.LAG_SPIKE, ExemptType.FAST)
                    || data.getPositionProcessor().getDeltaY() != data.getPositionProcessor().getLastDeltaY()
                    || data.getPlayer().isInsideVehicle();

            boolean valid = isExempt(ExemptType.CLIMBABLE);

            if (!exempt && valid) {
                double deltaY = data.getPositionProcessor().getDeltaY();

                if ((float) deltaY > 0.1178) {
                    if (increaseBuffer() > 5.0) {
                        fail("deltaY=" + deltaY);
                        staffAlert();
                        multiplyBuffer(.5);
                    }
                } else {
                    decreaseBufferBy(.25);
                }
            }
        }
    }
}
