package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.server.block.BlockUtil;
import com.elevatemc.api.CheckInfo;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Motion", type = "T", description = "Checks for liquidspeed", experimental = true)
public class MotionT extends Check {

    public MotionT(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (WrapperPlayClientPlayerFlying.isFlying(packet.getPacketType())) {

            boolean l = BlockUtil.isLiquid(data.getPlayer().getLocation().getBlock().getType());

            double multiplier = l ? 0.8 : 0.5;

            double deltaY = data.getPositionProcessor().getDeltaY();
            double lastDeltaY = data.getPositionProcessor().getLastDeltaY();

            double acceleration = deltaY - lastDeltaY;

            double predictedY = (lastDeltaY + 0.04D) * multiplier - 0.02D;
            double difference = Math.abs(deltaY - predictedY);

            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.VEHICLE, ExemptType.FLIGHT,
                    ExemptType.CLIMBABLE, ExemptType.VELOCITY, ExemptType.WEB,
                    ExemptType.SLIME, ExemptType.VEHICLE, ExemptType.CHUNK);
            boolean invalid = difference > 0.075 && deltaY > 0.075 && acceleration >= 0.0 && l;

            if (invalid && !exempt) {
                if (increaseBuffer() > 4) {
                    fail(difference);
                }
            } else {
                decreaseBufferBy(0.25);
            }
        }
    }
}