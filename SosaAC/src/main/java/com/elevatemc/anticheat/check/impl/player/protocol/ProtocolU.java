package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Protocol", type = "U", description = "Post HeldItemSlot packets.")
public class ProtocolU extends Check {

    private long lastFlying;
    private boolean sent;

    public ProtocolU(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            final long delay = now() - lastFlying;

            if (sent) {
                final boolean exempt = isExempt(ExemptType.CREATIVE);

                if (delay > 55L && delay < 100L && !exempt) {
                    if (increaseBuffer() > 7) {
                        fail("delay=" + delay);
                        staffAlert();
                        multiplyBuffer(.25);
                    }
                } else {
                    decreaseBufferBy(.25);
                }
                sent = false;
            }

            lastFlying = now();
        } else if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            final long delay = now() - lastFlying;

            if (delay < 10) {
                sent = true;
            }
        }
    }
}
