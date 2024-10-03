package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Protocol", type = "B", description = "Invalid pitch.")
public class ProtocolB extends Check {

    public ProtocolB(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            final float pitch = data.getRotationProcessor().getPitch();
            final boolean exempt = isExempt(ExemptType.TELEPORT);

            if (Math.abs(pitch) > 90F && !exempt) {
                fail("pitch=" + pitch);
                increaseVlBy(3.25);
                staffAlert();
            }
        }
    }
}
