package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSteerVehicle;

@CheckInfo(name = "Protocol", type = "E", description = "Invalid SteerVehicle packets.")
public class ProtocolE extends Check {

    public ProtocolE(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {
            final WrapperPlayClientSteerVehicle wrapper = new WrapperPlayClientSteerVehicle(event);

            final float forwardValue = Math.abs(wrapper.getForward());
            final float sideValue = Math.abs(wrapper.getSideways());

            final boolean invalid = forwardValue > .98F || sideValue > .98F;

            if (invalid) {
                fail("fV=" + forwardValue + " sV=" + sideValue + " (Disabler)");
                increaseVlBy(.45);
                staffAlert();
            }
        }
    }
}