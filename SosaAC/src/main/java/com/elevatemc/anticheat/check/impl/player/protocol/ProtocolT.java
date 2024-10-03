package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Protocol", type = "T", description = "Post EntityAction packets.")
public class ProtocolT extends Check {

    private long lastFlying;
    private boolean sent;

    public ProtocolT(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            final long delay = now() - lastFlying;

            if (sent) {
                final boolean exempt = isExempt(ExemptType.CREATIVE);

                if (delay > 47L && delay < 100L && !exempt) {
                    if (increaseBuffer() > 5) {
                        fail("delay=" + delay);
                        staffAlert();
                    }
                } else {
                    decreaseBufferBy(.25);
                }
                sent = false;
            }

            lastFlying = now();
        } else if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            final long delay = now() - lastFlying;

            if (delay < 10L) {
                sent = true;
            }
        }
    }
}

