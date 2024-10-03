package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Protocol", type = "F", description = "Speed bypass flaw detected")
public class ProtocolF extends Check {

    public ProtocolF(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            double deltaY = data.getPositionProcessor().getDeltaY();

            int groundTicks = data.getPositionProcessor().getClientGroundTicks();
            int airTicks = data.getPositionProcessor().getClientAirTicks();

            boolean exempt = isExempt(ExemptType.JOINED, ExemptType.DEAD, ExemptType.SLIME, ExemptType.TELEPORT);

            final boolean invalid = deltaY == 0.0 && groundTicks == 1 && airTicks == 0;

            if (invalid && !exempt) {
                if (increaseBuffer() > 12.0) {
                    increaseVlBy(.25);
                    fail("deltaY=" + deltaY + " ,gTicks=" + groundTicks + " ,ticks=" + airTicks);
                    staffAlert();
                }
            } else {
                decreaseBufferBy(.25);
            }
        }
    }
}

