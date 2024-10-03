package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Motion", type = "G", description = "Vertical movement is faster than possible")
public class MotionG extends Check {

    public MotionG(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            double deltaY = Math.abs(data.getPositionProcessor().getDeltaY());
            boolean exempt = isExempt(ExemptType.JOINED, ExemptType.TELEPORT) || data.getActionProcessor().isTeleporting();
            boolean invalid = deltaY > 10.0;
            if (invalid && !exempt) {
                if (increaseBuffer() > 1.0) {
                    fail("deltaY=" + deltaY + " teleporting=" + data.getActionProcessor().isTeleporting());
                    staffAlert();
                }
            }
        }
    }
}