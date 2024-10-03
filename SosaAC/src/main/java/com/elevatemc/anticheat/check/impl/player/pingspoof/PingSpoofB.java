package com.elevatemc.anticheat.check.impl.player.pingspoof;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Ping Spoof", type = "B", experimental = true, description = "Checks for large difference between the trans packet and keep")
public class PingSpoofB extends Check {

    private int keepAlivePing;

    public PingSpoofB(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {

            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.JOINED) || Sosa.INSTANCE.isShittingItself();
            if (exempt) return;

            int keepAlivePing = (int) data.getConnectionProcessor().getKeepAlivePing();
            int transactionPing = (int) data.getConnectionProcessor().getTransactionPing();

            int delta = Math.abs(keepAlivePing - transactionPing);

            if (delta > 2500) {
                fail("");
            }
        }
    }
}

