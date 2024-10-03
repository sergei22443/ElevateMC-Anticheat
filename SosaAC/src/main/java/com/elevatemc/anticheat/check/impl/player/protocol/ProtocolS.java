package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Protocol", type = "S", description = "Post ArmAnimation packets.")
public class ProtocolS extends Check {

    private long lastFlying;
    private boolean sent;

    public ProtocolS(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            if (data.getUser().getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) return;

            final long delay = now() - lastFlying;

            if (sent) {
                final boolean exempt = isExempt(ExemptType.CREATIVE);

                if (delay > 40L && delay < 100L && !exempt) {
                    if (increaseBuffer() > 6) {
                        increaseVlBy(.15);
                        fail("delay=" + delay);
                        multiplyBuffer(.25);
                    }
                } else {
                    decreaseBufferBy(.25);
                }
                sent = false;
            }

            lastFlying = now();
        } else if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            final long delay = now() - lastFlying;

            if (delay < 10) {
                sent = true;
            }
        }
    }
}
