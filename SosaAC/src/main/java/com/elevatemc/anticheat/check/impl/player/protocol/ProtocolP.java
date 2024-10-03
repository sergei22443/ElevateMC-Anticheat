package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckInfo(name = "Protocol", type = "P", description = "Digging and placing at the same time.")
public class ProtocolP extends Check {

    public ProtocolP(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            final WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);

            check: {
                if (wrapper.getAction() != DiggingAction.RELEASE_USE_ITEM) break check;

                final boolean invalid = data.getActionProcessor().isPlacing();

                if (invalid) {
                    if (increaseBuffer() > 3) {
                        increaseVlBy(.75);
                        fail("Dug and placed in the same tick");
                        staffAlert();
                        multiplyBuffer(.5);
                    }
                } else {
                    decreaseBufferBy(.25);
                }
            }
        }
    }
}
