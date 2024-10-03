package com.elevatemc.anticheat.check.impl.movement.speed;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Speed", type = "F", description = "Checks if player is going faster than possible on air.")
public final class SpeedF extends Check
{
    public SpeedF(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            double deltaXZ = this.data.getPositionProcessor().getDeltaXZ();

            if (data.getVelocityProcessor().getVelocityY() == 0.175) return;

            int iceTicks = data.getPositionProcessor().getSinceNearIceTicks();
            int slimeTicks = data.getPositionProcessor().getSinceNearSlimeTicks();

            boolean collidedVTicks = isExempt(ExemptType.COLLIDING_VERTICALLY);
            boolean takingVelocity = data.getVelocityProcessor().getTicksSinceVelocity() <= 20;

            double velocityXZ = Math.hypot(Math.abs(data.getVelocityProcessor().getVelocityX()), data.getVelocityProcessor().getVelocityZ());
            double limit = PlayerUtil.getBaseSpeed(data.getPlayer(), 0.34f);

            if (iceTicks < 40 || slimeTicks < 40) {
                limit += 0.34;
            }
            if (collidedVTicks) {
                limit += 0.91;
            }
            if (takingVelocity) {
                limit += velocityXZ + 0.1;
            }
            boolean exempt = isExempt(ExemptType.VEHICLE,ExemptType.FLIGHT, ExemptType.TELEPORT);
            boolean invalid = deltaXZ > limit && data.getPositionProcessor().getClientAirTicks() > 2;
            if (invalid && !exempt) {
                if (increaseBuffer() > 14.0) {
                    fail("deltaXZ=" + deltaXZ + " ticks=" + data.getPositionProcessor().getClientAirTicks());
                    increaseVlBy(.43);
                    staffAlert();
                    multiplyBuffer(0.75);
                }
            }
            else {
                multiplyBuffer(0.75);
            }
        }
    }
}
