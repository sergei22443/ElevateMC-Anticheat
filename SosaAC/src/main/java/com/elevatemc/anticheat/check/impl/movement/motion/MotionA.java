package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Motion", type = "A", description = "Checks for repetitive vertical motions")
public class MotionA extends Check {

    public MotionA(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            boolean exempt = isExempt(ExemptType.SLIME, ExemptType.TELEPORT, ExemptType.FAST, ExemptType.LAG_SPIKE, ExemptType.WEB, ExemptType.ICE);

            double deltaY = data.getPositionProcessor().getDeltaY();
            double lastDeltaY = data.getPositionProcessor().getLastDeltaY();

            boolean invalid = deltaY == -lastDeltaY && deltaY != 0.0;

            if (invalid && !exempt) {
                if (increaseBuffer() > 4.0) {
                    fail("motion=" + deltaY);
                    increaseVlBy(.45);
                    staffAlert();
                }
            } else {
                resetBuffer();
            }
        }
    }
}
