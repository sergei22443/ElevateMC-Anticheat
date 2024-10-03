package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Motion", type = "F", description = "Horizontal movement is faster than possible")
public class MotionF extends Check {

    public MotionF(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            double deltaXZ = data.getPositionProcessor().getDeltaXZ();

            boolean exempt = isExempt(ExemptType.JOINED, ExemptType.TELEPORT);
            boolean teleport = data.getActionProcessor().getLastBukkitTeleport() < 30 || data.getActionProcessor().isTeleporting();
            boolean invalid = deltaXZ > 10.0;

            if (invalid && !exempt && !teleport) {
                increaseVlBy(.55);
                fail("deltaXZ=" + deltaXZ);
                staffAlert();
            }
        }
    }
}
