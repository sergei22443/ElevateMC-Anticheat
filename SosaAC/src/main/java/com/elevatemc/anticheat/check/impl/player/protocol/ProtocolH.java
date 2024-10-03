package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Protocol", type = "H",  description = "Invalid attack packet order.")
public class ProtocolH extends Check {

    private boolean swung;

    public ProtocolH(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            final WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);

            if (data.getUser().getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) return;

            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (!swung) {
                    increaseVlBy(1.45);
                    fail("Player attacked without swinging (no-swing/ or similar)");
                    staffAlert();
                }
            }
        } else if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            swung = true;
        } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            swung = false;
        }
    }
}
