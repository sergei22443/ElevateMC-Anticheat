package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.server.block.BlockUtil;
import com.elevatemc.api.CheckInfo;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Motion", type = "S", description = "Checks for liquidspeed", experimental = true)
public class MotionS extends Check {

    double lastDifference = 0;
    public MotionS(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (WrapperPlayClientPlayerFlying.isFlying(packet.getPacketType())) {

            boolean inLiquid = BlockUtil.isLiquid(data);

            double deltaY = data.getPositionProcessor().getDeltaY();
            double lastDeltaY = data.getPositionProcessor().getLastDeltaY();
            double limit = inLiquid ? 0.8 : 0.5;

            double acceleration = deltaY - lastDeltaY;

            double predicted = lastDeltaY * limit - 0.02F;
            double difference = Math.abs(deltaY - predicted);
            boolean sameDifference = difference == lastDifference;
            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.VEHICLE, ExemptType.FLIGHT, ExemptType.TRAPDOOR,
                    ExemptType.CLIMBABLE, ExemptType.WEB, ExemptType.SLIME, ExemptType.VEHICLE, ExemptType.CHUNK) || data.getVelocityProcessor().getTicksSinceVelocity() < 10;
            boolean invalid = difference > 0.125 && deltaY < -0.075 && acceleration <= 0.0 && inLiquid;

            if (invalid && !exempt && !sameDifference) {
                if (increaseBuffer() > 4) {
                    fail("diff=" + difference);
                }
            } else {
                decreaseBufferBy(0.25);
            }

            lastDifference = difference;
        }
    }
}