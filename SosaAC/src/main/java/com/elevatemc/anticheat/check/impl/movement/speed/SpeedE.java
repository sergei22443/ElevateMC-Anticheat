package com.elevatemc.anticheat.check.impl.movement.speed;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Speed", type = "E", description = "Invalid air movement")
public final class SpeedE extends Check
{
    public SpeedE(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            boolean sprinting = data.getActionProcessor().isSprinting();

            double deltaX = data.getPositionProcessor().getDeltaX();
            double deltaZ = data.getPositionProcessor().getDeltaZ();

            double deltaXZ = data.getPositionProcessor().getDeltaXZ();
            double lastDeltaX = data.getPositionProcessor().getLastDeltaX();
            double lastDeltaZ = data.getPositionProcessor().getLastDeltaZ();

            int airTicks = data.getPositionProcessor().getClientAirTicks();

            double blockSlipperiness = 0.91F;
            double attributeSpeed = sprinting ? 0.026 : 0.02;

            double predictedDeltaX = lastDeltaX * blockSlipperiness + attributeSpeed;
            double predictedDeltaZ = lastDeltaZ * blockSlipperiness + attributeSpeed;
            double diffX = deltaX - predictedDeltaX;
            double diffZ = deltaZ - predictedDeltaZ;

            boolean exempt = isExempt(
                    ExemptType.TPS,
                    ExemptType.TELEPORT,
                    ExemptType.FLIGHT,
                    ExemptType.COLLIDING_VERTICALLY,
                    ExemptType.CLIMBABLE,
                    ExemptType.LAG_SPIKE,
                    ExemptType.FAST,
                    ExemptType.LIQUID);
            boolean invalid = (diffX > 0.01 || diffZ > 0.01) && deltaXZ > 0.175 && airTicks > 2;

            if (invalid && !exempt) {
                if (increaseBuffer() > 5.0) {
                    fail("diffX=" + diffX + " ticks=" + airTicks);
                    increaseVlBy(.49);
                    staffAlert();
                }
            }
            else {
                decreaseBufferBy(0.1);
            }
        }
    }
}
