package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Motion", type = "H", description = "Checks for invalid deltaY")
public class MotionH extends Check {

    public MotionH(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            double deltaY = data.getPositionProcessor().getDeltaY();
            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.CREATIVE);

            if (data.getPlayer().getFallDistance() > 5.0f && data.getPlayer().getFallDistance() < 256.0f && deltaY > 4.5 && !exempt) {
                fail("deltaY=" + deltaY);
                increaseVlBy(.45);
                staffAlert();
            }
        }
    }
}
