package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.util.server.block.BlockUtil;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Motion", type = "E", description = "Checks if a player is walking on water")
public class MotionE extends Check {

    public MotionE(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            double deltaX = data.getPositionProcessor().getDeltaX();
            double deltaY = data.getPositionProcessor().getDeltaY();
            double deltaZ = data.getPositionProcessor().getDeltaZ();

            boolean onGround = data.getPositionProcessor().isClientOnGround();
            boolean isInLiquid = BlockUtil.isLiquid(data);
            boolean stationary = deltaX % 1.0 == 0.0 && deltaZ % 1.0 == 0.0;

            boolean invalid = deltaY > 0.0 && !onGround && !isInLiquid && stationary;
            boolean exempt = isExempt(ExemptType.TELEPORT) || data.getActionProcessor().isTeleporting();
            if (invalid && !exempt) {
                final double deltaXZ = data.getPositionProcessor().getDeltaXZ();

                if (deltaXZ > 0.1) {
                    increaseVlBy(.45);
                    fail("deltaXZ=" + deltaXZ + " deltaY=" + deltaY + " deltaZ=" + deltaZ);
                    staffAlert();
                }
            }
        }
    }
}
