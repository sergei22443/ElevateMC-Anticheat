package com.elevatemc.anticheat.check.impl.player.pingspoof;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Ping Spoof", type = "A", experimental = true, description = "Checks for large differences")
public class PingSpoofA extends Check {

    public PingSpoofA(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {

            int keepAlive = data.getConnectionProcessor().getKeepAliveTimes().size();
            int transaction = data.getConnectionProcessor().getTransactionsSent().size();

            if (keepAlive > transaction + 5) {
                fail("invalid game speed");
            }
        }
    }
}
