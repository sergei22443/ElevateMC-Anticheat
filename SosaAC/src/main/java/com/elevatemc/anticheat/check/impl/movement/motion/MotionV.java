package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.server.block.BlockUtil;
import com.elevatemc.api.CheckInfo;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Motion", type = "V", experimental = true, description = "Invalid prediction motion")
public class MotionV extends Check {

    /*
        Idea was taken from beanes, Motion Type O.
     */
    public MotionV(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (WrapperPlayClientPlayerFlying.isFlying(packet.getPacketType())) {

            boolean exempt = isExempt(ExemptType.COLLIDING_VERTICALLY,ExemptType.SLIME);
            boolean inLiquid = BlockUtil.isLiquid(data.getPlayer().getLocation().getBlock().getType());

            int airTicks = data.getPositionProcessor().getClientAirTicks();
            double deltaY = data.getPositionProcessor().getDeltaY();
            double lastDeltaY = data.getPositionProcessor().getDeltaY();

            if (airTicks == 1 && !exempt && !inLiquid && deltaY > 0) {
                float limit = 0.42F;

                limit += (float) data.getPotionProcessor().getJumpBoostAmplifier() * 0.1F;
                //debug(data.getVelocityProcessor().getVelocityY());
                limit -= (float) data.getVelocityProcessor().getVelocityY();

                if (deltaY < limit && deltaY != lastDeltaY) {
                    if (increaseBuffer() > 20) fail("dY=" + deltaY);
                } else if (deltaY >= limit) {
                    multiplyBuffer(0.98);
                }
            }
        }
    }
}
