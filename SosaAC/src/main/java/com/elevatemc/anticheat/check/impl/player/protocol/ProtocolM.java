package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckInfo(name = "Protocol", type = "M", description = "Invalid sword block packet order.")
public class ProtocolM extends Check {

    public ProtocolM(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            final WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);

            if (data.getUser().getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9)) return;

            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                final boolean invalid = data.getActionProcessor().isPlacing();
                final boolean sword = PlayerUtil.isHoldingSword(data.getPlayer());

                if (invalid && sword) {
                    increaseVlBy(.75);
                    fail("Player placed and attacked in the same tick");
                    staffAlert();
                }
            }
        }
    }
}