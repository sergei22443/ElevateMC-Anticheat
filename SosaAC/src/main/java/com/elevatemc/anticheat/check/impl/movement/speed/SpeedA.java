package com.elevatemc.anticheat.check.impl.movement.speed;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Speed", type = "A", description = "Invalid friction.")
public class SpeedA extends Check {

    public SpeedA(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            double deltaXZ = data.getPositionProcessor().getDeltaXZ();
            double lastDeltaXZ = data.getPositionProcessor().getLastDeltaXZ();
            boolean sprinting = data.getActionProcessor().isSprinting();

            double prediction = lastDeltaXZ * 0.91f + (sprinting ? 0.026 : 0.02);
            double difference = deltaXZ - prediction;

            int airTicks = data.getPositionProcessor().getClientAirTicks();

            boolean velocity = data.getVelocityProcessor().getTicksSinceVelocity() < 5;
            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.JOINED, ExemptType.TPS, ExemptType.FAST,
                    ExemptType.FLIGHT, ExemptType.CREATIVE, ExemptType.DEAD, ExemptType.LAG_SPIKE,
                    ExemptType.DEPTH_STRIDER, ExemptType.LIQUID);

            if (airTicks > 2 && !velocity && !exempt) {
                if (difference > 1e-5) {
                    if (increaseBuffer() > 7) {
                        increaseVlBy(.45);
                        fail("difference=" + difference + " ticks=" + airTicks);
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
