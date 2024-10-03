package com.elevatemc.anticheat.check.impl.player.interact;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckInfo(name = "Interact", type = "B", description = "Dug block from too far.")
public class InteractB extends Check {

    public InteractB(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            final WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);

            switch (wrapper.getAction()) {
                case RELEASE_USE_ITEM:
                case DROP_ITEM:
                case DROP_ITEM_STACK:
                case SWAP_ITEM_WITH_OFFHAND:
                    return;
            }

            final double distanceX = wrapper.getBlockPosition().getX() - data.getPositionProcessor().getX();
            final double distanceZ = wrapper.getBlockPosition().getZ() - data.getPositionProcessor().getZ();
            final double distanceXZ = MathUtil.hypot(distanceX, distanceZ);

            final boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.TPS, ExemptType.FAST, ExemptType.LAG_SPIKE);

            if (distanceXZ > 8 && distanceXZ < 50 && !exempt) {
                if (increaseBuffer() > 5) {
                    increaseVlBy(.45);
                    fail("distance=" + distanceXZ);
                    staffAlert();
                    multiplyBuffer(.25);
                }
            } else {
                decreaseBufferBy(.75);
            }
        }
    }
}
