package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Motion", type = "N", experimental = true, description = "Checks for invalid deltas")
public class MotionN extends Check {

    public MotionN(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || packet.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            boolean exempt = isExempt(ExemptType.JOINED, ExemptType.TELEPORT, ExemptType.LIQUID, ExemptType.COLLIDING_VERTICALLY, ExemptType.TRAPDOOR, ExemptType.LAG_SPIKE) || data.getPositionProcessor().getY() % 1.0 != 0.0 || data.getPositionProcessor().getLastY() % 1.0 != 0.0;
            boolean onGround = data.getPositionProcessor().isClientOnGround() || data.getPositionProcessor().isMathematicallyOnGround();

            double deltaXZ = data.getPositionProcessor().getDeltaXZ();
            double maxDelta = 0.33 + data.getPotionProcessor().getSpeedBoostAmplifier() * 0.06;

            if (data.getPlayer().getWalkSpeed() > 0.2f) {
                maxDelta += data.getPlayer().getWalkSpeed() * 0.28634357f * 3.0f;
            }

            if (deltaXZ > maxDelta && !onGround && !exempt) {
                if (increaseBuffer() > 4.0) {
                    fail("d=" + deltaXZ + " max=" + maxDelta);
                }
            }
        }
    }
}
