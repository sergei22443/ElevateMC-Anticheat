package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Motion", type = "I", description = "Checks for invalid packet order")
public class MotionI extends Check {

    private long lastFlyingPacketSent;

    public MotionI(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            lastFlyingPacketSent = now();
        } else if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            long diff = now() - lastFlyingPacketSent;
            boolean exempt = isExempt(ExemptType.LAG_SPIKE, ExemptType.FAST);
            if (diff < 5L && !exempt) {
                if (increaseBuffer() > 11.0) {
                    fail("timeDiff=" + diff);
                    increaseVlBy(.45);
                    staffAlert();
                }
            } else {
                resetBuffer();
            }
        }
    }
}
