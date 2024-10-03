package com.elevatemc.anticheat.check.impl.player.interact;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

@CheckInfo(name = "Interact", type = "F", description = "Checks for NoSlow")
public class InteractE extends Check {

    public InteractE(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            final WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);

            final boolean exempt = data.getUser().getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9);

            if (!exempt) {
                final boolean invalid = wrapper.getAction().equals(DiggingAction.RELEASE_USE_ITEM)
                        && data.getActionProcessor().isPlacing();

                if (invalid) {
                    if (increaseBuffer() > 3.0) {
                        increaseVlBy(1.25);
                        fail("deltaXZ=" + data.getPositionProcessor().getDeltaXZ());
                        staffAlert();
                    }
                } else {
                    decreaseBufferBy(.5);
                }
            }
        }
    }
}
