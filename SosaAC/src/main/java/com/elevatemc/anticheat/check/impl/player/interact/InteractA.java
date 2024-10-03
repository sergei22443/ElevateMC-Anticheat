package com.elevatemc.anticheat.check.impl.player.interact;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;

@CheckInfo(name = "Interact", type = "A", description = "Placed block from too far.")
public class InteractA extends Check {

    public InteractA(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            final WrapperPlayClientPlayerBlockPlacement wrapper = new WrapperPlayClientPlayerBlockPlacement(event);

            if (wrapper.getBlockPosition().getX() == -1 && wrapper.getBlockPosition().getZ() == -1) return;

            final double distanceX = wrapper.getBlockPosition().getX() - data.getPositionProcessor().getX();
            final double distanceZ = wrapper.getBlockPosition().getZ() - data.getPositionProcessor().getZ();
            final double distanceXZ = MathUtil.hypot(distanceX, distanceZ);

            final boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.TPS, ExemptType.FAST, ExemptType.LAG_SPIKE,
                    ExemptType.CREATIVE);

            if (distanceXZ > 6.5 && distanceXZ < 50 && !exempt) {
                if (increaseBuffer() > 2) {
                    increaseVlBy(.45);
                    fail("distance=" + distanceXZ);
                    multiplyBuffer(.25);
                }
            } else {
                decreaseBufferBy(.25);
            }
        }
    }
}
