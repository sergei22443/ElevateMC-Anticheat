package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Motion", type = "P", description = "Checks for invalid acceleration over deltaY")
public class MotionP extends Check {

    public MotionP(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            double deltaY = data.getPositionProcessor().getDeltaY();

            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.COLLIDING_VERTICALLY,
                    ExemptType.VELOCITY, ExemptType.DEAD, ExemptType.FLIGHT,
                    ExemptType.CREATIVE, ExemptType.SPECTATOR, ExemptType.VEHICLE)
                    || data.getPlayer().getWalkSpeed() > 0.21f || deltaY == 0.5;

            if (!exempt) {
                boolean invalid = deltaY % 0.25 == 0.0 && deltaY > 0.0;

                if (invalid) {
                    fail("deltaY=" + deltaY);
                }
            }
        }
    }
}
