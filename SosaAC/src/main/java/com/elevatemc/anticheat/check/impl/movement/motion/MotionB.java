package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Motion", type = "B", description = "Checks if a player is accelerating faster than possible on Y axis")
public class MotionB extends Check {

    public MotionB(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            boolean exempt = isExempt(ExemptType.CREATIVE,
                    ExemptType.LIQUID,
                    ExemptType.FLIGHT,
                    ExemptType.TELEPORT) || data.getPositionProcessor().isClientOnGround()
                    || data.getActionProcessor().isTeleporting();

            if (!exempt) {
                double max = 0.7 + data.getPotionProcessor().getJumpBoostAmplifier() * 0.1;

                double deltaY = data.getPositionProcessor().getDeltaY();
                double velocityY = data.getPlayer().getVelocity().getY();


                if (deltaY != 1.1199999749660492) return;

                if (data.getVelocityProcessor().getFlyingVelocityTicks() <= 5) {
                    max += data.getVelocityProcessor().getVelocityXZ();
                }

                if (deltaY > max && velocityY < -0.075) {
                    fail("delta=" + deltaY);
                    increaseVlBy(.45);
                    staffAlert();
                }
            }
        }
    }
}
