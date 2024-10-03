package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Protocol", type = "R",description = "Post BlockDig packets.")
public class ProtocolR extends Check {

    private long lastFlying;
    private boolean sent;

    public ProtocolR(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            final long delay = now() - lastFlying;

            if (sent) {
                if (delay > 40L && delay < 100L) {
                    if (increaseBuffer() > 10) {
                        increaseVlBy(.25);
                        fail("delay=" + delay);
                        staffAlert();
                        multiplyBuffer(.25);
                    }
                } else {
                    decreaseBufferBy(.75);
                }
                sent = false;
            }

            lastFlying = now();
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            final long delay = now() - lastFlying;

            if (delay < 10) {
                sent = true;
            }
        }
    }
}
