package com.elevatemc.anticheat.check.impl.movement.flight;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Flight", type = "D", description = "Checks for acceleration mid-air.")
public class FlightD extends Check
{
    public FlightD(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {

            if (isExempt(ExemptType.JOINED)) return;

            int serverAirTicks = data.getPositionProcessor().getServerAirTicks();
            int clientAirTicks = data.getPositionProcessor().getClientAirTicks();

            double deltaY = data.getPositionProcessor().getDeltaY();
            double lastDeltaY = data.getPositionProcessor().getLastDeltaY();

            double acceleration = deltaY - lastDeltaY;

            boolean exempt = isExempt(ExemptType.VEHICLE, ExemptType.TELEPORT, ExemptType.LIQUID,
                    ExemptType.SLIME,
                    ExemptType.WEB,
                    ExemptType.TRAPDOOR,
                    ExemptType.FLIGHT,
                    ExemptType.CLIMBABLE, ExemptType.CHUNK);

            double limit = 0.0;

            if (deltaY == lastDeltaY || clientAirTicks > 30 && serverAirTicks == 0 || isExempt(ExemptType.WEB)) {
                return;
            }

            if (data.getVelocityProcessor().getFlyingVelocityTicks() <= 10) {
                limit += Math.abs(data.getVelocityProcessor().getVelocityXZ());
            } else {
                limit = 0.0;
            }

            boolean invalid = acceleration > limit && (serverAirTicks > 15 || clientAirTicks > 15);

            if (invalid && !exempt) {
                if (increaseBuffer() > 15.0) {
                    fail("ticks=" + clientAirTicks + " sTicks=" + serverAirTicks + " deltaY=" + deltaY);
                    staffAlert();
                }
            }
            else {
                decreaseBufferBy(0.1);
            }
        }
    }
}
