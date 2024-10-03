package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Motion", type = "L", experimental = true, description = "Checks for invalid speed whilst in a cobweb")
public class MotionL extends Check {

    public MotionL(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            boolean inCobweb = isExempt(ExemptType.WEB);
            boolean takingVelocity = data.getVelocityProcessor().getTicksSinceVelocity() < 20 && inCobweb;

            double deltaXZ = data.getPositionProcessor().getDeltaXZ();
            double maxDelta = 0.22;

            if (takingVelocity) {
                maxDelta += 0.7;
            }

            if (inCobweb && !isExempt(ExemptType.FLIGHT)) {
                //debug("dXZ=" + data.getPositionProcessor().getDeltaXZ());
                if (deltaXZ > maxDelta) {
                    if (increaseBuffer() > 2) {
                        fail("dXZ=" + deltaXZ);
                    }
                }
            }
        }
    }
}
