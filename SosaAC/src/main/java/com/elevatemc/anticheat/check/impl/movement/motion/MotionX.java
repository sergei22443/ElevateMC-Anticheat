package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.api.CheckInfo;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Motion", type = "X", description = "Improbable tick difference in air.")
public class MotionX extends Check {

    public MotionX(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (WrapperPlayClientPlayerFlying.isFlying(packet.getPacketType())) {

            int clientAirTicks = data.getPositionProcessor().getClientAirTicks();
            int serverAirTicks = data.getPositionProcessor().getServerAirTicks();

            boolean exempt = isExempt(ExemptType.FLIGHT, ExemptType.CREATIVE,
                    ExemptType.JOINED, ExemptType.LIQUID, ExemptType.VEHICLE, ExemptType.ONVEHICLE,
                    ExemptType.WEB, ExemptType.TELEPORT);

            if (isExempt(ExemptType.ONVEHICLE, ExemptType.VEHICLE)) {
                clientAirTicks = 0;
                serverAirTicks = 0;
            }

            if (clientAirTicks > 60 && serverAirTicks == 0 && !exempt && data.getPositionProcessor().getY() > 4.0) {
                fail("c=" + clientAirTicks + " s=" +serverAirTicks);
            }
        }
    }
}

