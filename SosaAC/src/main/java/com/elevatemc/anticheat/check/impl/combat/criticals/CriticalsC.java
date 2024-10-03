package com.elevatemc.anticheat.check.impl.combat.criticals;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Criticals", type = "C", description = "Checks for notations in players' attacks")
public class CriticalsC extends Check {

    public CriticalsC(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            boolean clientOnGround = data.getPositionProcessor().isClientOnGround();
            boolean playerOnGround = data.getPositionProcessor().isMathematicallyOnGround();

            final int clientAirTicks = data.getPositionProcessor().getClientAirTicks();

            boolean notation = MathUtil.isScientificNotation(data.getPlayer().getFallDistance());
            boolean exempt = isExempt(ExemptType.COLLIDING_VERTICALLY, ExemptType.LIQUID);
            boolean invalid = (!clientOnGround && playerOnGround || !playerOnGround && clientOnGround) && clientAirTicks > 20 && notation;

            if (invalid && !exempt) {
                if (increaseBuffer() > 1.0) {
                    fail("ticks=" + clientAirTicks);
                    staffAlert();
                }
            }
        }
    }
}
