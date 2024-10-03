package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Protocol", type = "C", description = "More than 20 flying packets in a row.")
public class ProtocolC extends Check {

    private int streak;

    public ProtocolC(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            final WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);

            if (data.getUser().getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) return;

            if (wrapper.hasPositionChanged() || data.getPlayer().isInsideVehicle()) {
                streak = 0;
                return;
            }

            if (++streak > 23) {
                fail("streak=" + streak);
                increaseVlBy(1.45);
                staffAlert();
            }
        } else if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {
            streak = 0;
        }
    }
}
