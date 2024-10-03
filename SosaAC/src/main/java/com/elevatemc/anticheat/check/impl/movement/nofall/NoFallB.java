package com.elevatemc.anticheat.check.impl.movement.nofall;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "No Fall", type = "B", description = "Checks for spoofed distance")
public class NoFallB extends Check {

    public NoFallB(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (packet.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION || packet.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {

            int airTicks = data.getPositionProcessor().getClientAirTicks();

            float distance = data.getPlayer().getFallDistance();

            double y = data.getPositionProcessor().getY();
            double yDifference = data.getPlayer().getLocation().getY() - y;

            boolean exempt = isExempt(ExemptType.CREATIVE, ExemptType.SPECTATOR, ExemptType.TELEPORT, ExemptType.LIQUID, ExemptType.SLIME, ExemptType.FLIGHT);

            boolean jumpBoostExempt = data.getPotionProcessor().getJumpBoostAmplifier() > 0;

            if (airTicks > 20 && distance == 0.0 && yDifference == 0.0 && !exempt && !jumpBoostExempt) {
                if (increaseBuffer() > 5) {
                    fail("");
                    resetBuffer();
                }
            }
        }
    }
}
