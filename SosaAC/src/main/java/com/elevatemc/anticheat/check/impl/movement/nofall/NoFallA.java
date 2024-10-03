package com.elevatemc.anticheat.check.impl.movement.nofall;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "No Fall", type = "A", experimental = true, description = "Spoofed distance")
public class NoFallA extends Check {

    public NoFallA(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (data.getPositionProcessor().isInAir()) {
                boolean exempt = isExempt(ExemptType.CLIMBABLE, ExemptType.SLIME, ExemptType.FLIGHT,
                        ExemptType.CREATIVE, ExemptType.LIQUID, ExemptType.VELOCITY, ExemptType.JOINED, ExemptType.LAG_SPIKE
                        , ExemptType.TPS, ExemptType.FAST, ExemptType.TELEPORT)
                        || data.getPositionProcessor().getLastX() == data.getPositionProcessor().getX()
                        || data.getPositionProcessor().getLastZ() == data.getPositionProcessor().getZ()
                        || data.getPositionProcessor().isNearSlime()
                        || data.getPositionProcessor().isNearStair();

                int airTicks = data.getPositionProcessor().getClientAirTicks();
                float fallDistance = data.getPlayer().getFallDistance();

                if (airTicks < 3 && fallDistance == 0.0f && airTicks > 1 && !exempt) {
                    if (increaseBuffer() > 6) {
                        fail("ticks=" + airTicks + " dist=" + fallDistance);
                    }
                } else {
                    decreaseBufferBy(.75);
                }
            }
        }
    }
}
