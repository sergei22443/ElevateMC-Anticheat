package com.elevatemc.anticheat.check.impl.combat.criticals;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Criticals", type = "B", description = "Checks if a player is critting while spoofing the ground")
public class CriticalsB extends Check {

    public CriticalsB(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            final boolean clientOnGround = data.getPositionProcessor().isClientOnGround();
            final boolean playerOnGround = data.getPositionProcessor().isMathematicallyOnGround();

            final int clientAirTicks = data.getPositionProcessor().getClientAirTicks();

            final double y = data.getPositionProcessor().getY();

            final boolean exempt = isExempt(ExemptType.COLLIDING_VERTICALLY, ExemptType.JOINED, ExemptType.TPS, ExemptType.LAG_SPIKE, ExemptType.FAST, ExemptType.SPECTATOR);
            final boolean invalid = !clientOnGround && !playerOnGround && clientAirTicks > 0 && data.getPlayer().getFallDistance() == 0.0f && y % 1.0 == 0.0;

            if (invalid && !exempt) {
                if (increaseBuffer() > 5.0) {
                    fail("ticks=" + clientAirTicks);
                    staffAlert();
                }
            } else {
                decreaseBufferBy(.75);
            }
        }
    }
}
