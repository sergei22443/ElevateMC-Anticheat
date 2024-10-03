package com.elevatemc.anticheat.check.impl.player.blink;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.Location;

@CheckInfo(name = "Blink", type = "A", description = "Checks for packet choking / low packet delay")
public class BlinkA extends Check {

    private Long lastAccept = 0L, lastFlying = 0L;

    public BlinkA(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        boolean exempt = isExempt(ExemptType.VEHICLE, ExemptType.CREATIVE, ExemptType.DEAD, ExemptType.SPECTATOR, ExemptType.JOINED);
        boolean valid = data.getConnectionProcessor().getTransactionPing() != -1 && data.getConnectionProcessor().getKeepAlivePing() != -1;

        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            lastFlying = now();
        } else if (event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION && !exempt && valid) {

            long sinceFlyingPacket = now() - lastFlying;
            long sinceAcceptedTransaction = now() - lastAccept;

            long transactionPing = data.getConnectionProcessor().getTransactionPing();

            boolean invalid = sinceFlyingPacket - transactionPing > 100 && sinceAcceptedTransaction > 10;

            if (invalid) {
                if (increaseBuffer() > 6.0) {
                    fail("flagged=" + getBuffer());
                    decreaseBufferBy(2.0);
                }
            } else {
                decreaseBufferBy(2);
            }
            lastAccept = now();
        }
    }
}