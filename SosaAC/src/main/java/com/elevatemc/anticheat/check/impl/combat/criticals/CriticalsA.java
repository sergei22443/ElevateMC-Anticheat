package com.elevatemc.anticheat.check.impl.combat.criticals;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Criticals", type = "A", description = "Checks if a player is critting without jumping")
public class CriticalsA extends Check {

    public CriticalsA(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            boolean clientOnGround = data.getPositionProcessor().isClientOnGround();
            boolean serverOnGround = data.getPositionProcessor().isMathematicallyOnGround();

            double y = data.getPositionProcessor().getY();
            double lastY = data.getPositionProcessor().getLastY();

            int clientAirTicks = data.getPositionProcessor().getClientAirTicks();

            if (isExempt(ExemptType.CREATIVE, ExemptType.SPECTATOR)) {
                return;
            }

            boolean exempt = isExempt(ExemptType.COLLIDING_VERTICALLY, ExemptType.JOINED, ExemptType.TPS, ExemptType.LAG_SPIKE, ExemptType.FAST, ExemptType.VEHICLE);

            boolean invalid = (!clientOnGround && serverOnGround || !serverOnGround && clientOnGround) && data.getPlayer().getFallDistance() == 0.0f && clientAirTicks > 0 && lastY % 1.0 == 0.0 && y % 1.0 == 0.0;

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
