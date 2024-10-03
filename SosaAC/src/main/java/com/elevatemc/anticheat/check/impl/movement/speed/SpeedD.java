package com.elevatemc.anticheat.check.impl.movement.speed;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Speed", type = "D", description = "Checks for slow-hop")

public class SpeedD extends Check {

    public SpeedD(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            // lazy fixing jumping backwards false.
            if (data.getPlayer().getWalkSpeed() < 0.2f) return;

            double deltaXZ = data.getPositionProcessor().getDeltaXZ();
            double lastDeltaXZ = data.getPositionProcessor().getLastDeltaXZ();
            boolean sprinting = data.getActionProcessor().isSprinting();

            double prediction = lastDeltaXZ * 0.91f + (sprinting ? 0.026 : 0.02);
            double difference = deltaXZ - prediction;

            int airTicks = data.getPositionProcessor().getClientAirTicks();

            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.JOINED, ExemptType.TPS, ExemptType.FAST,
                    ExemptType.FLIGHT, ExemptType.CREATIVE, ExemptType.DEAD, ExemptType.LAG_SPIKE,
                    ExemptType.DEPTH_STRIDER, ExemptType.LIQUID);

            boolean hasJump = data.getPotionProcessor().getJumpBoostAmplifier() > 0;

            if (airTicks > 3 && !exempt && !hasJump) {
                if (difference > 1.0E-12 && deltaXZ > 0.2) {
                    if (increaseBuffer() > 6.0) {
                        fail("difference=" + difference + " ticks=" + airTicks);
                        increaseVlBy(.45);
                        staffAlert();
                        multiplyBuffer(.5);
                    }
                } else {
                    decreaseBufferBy(.35);
                }
            }
        }
    }
}
