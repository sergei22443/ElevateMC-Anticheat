package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.util.server.block.BlockUtil;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Motion", type = "D", description = "Checks if a player is moving faster than possible horizontally on a climbable")
public class MotionD extends Check {

    public MotionD(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            boolean exempt = isExempt(ExemptType.FLIGHT, ExemptType.CREATIVE, ExemptType.WEB, ExemptType.CLIMBABLE)
                    || data.getPlayer().isInsideVehicle()
                    || data.getVelocityProcessor().getTicksSinceVelocity() < 30;

            if (BlockUtil.isClimbable(data.getPlayer().getLocation()) && !exempt) {
                double horizontalLimit = 0.24;

                if (data.getPotionProcessor().getSpeedBoostAmplifier() > 0) {
                    horizontalLimit *= 1 + data.getPotionProcessor().getSpeedBoostAmplifier() * 0.42;
                }
                if (data.getPlayer().getWalkSpeed() > 0.2f) {
                    horizontalLimit *= 1 + ((data.getPlayer().getWalkSpeed() / 0.2f) * 0.39);
                }
                double deltaXZ = data.getPositionProcessor().getDeltaXZ();

                if (deltaXZ > horizontalLimit) {
                    if (increaseBuffer() > 12.0) {
                        increaseVlBy(.45);
                        fail("deltaXZ=" + deltaXZ + " limit=" + horizontalLimit);
                        staffAlert();
                    }
                } else {
                    resetBuffer();
                }
            }

        }
    }
}
