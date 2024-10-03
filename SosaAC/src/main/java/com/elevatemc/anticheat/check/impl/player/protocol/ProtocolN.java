package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckInfo(name = "Protocol", type = "N", description = "Invalid sword block packet order.")
public class ProtocolN extends Check {

    public ProtocolN(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            final WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);

            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                final boolean invalid = data.getActionProcessor().isBlocking();
                final boolean sword = PlayerUtil.isHoldingSword(data.getPlayer());

                if (invalid && sword) {
                    increaseVlBy(.75);
                    fail("Player blocked and attacked in the same tick (auto-block)");
                    staffAlert();
                }
            }
        }
    }
}