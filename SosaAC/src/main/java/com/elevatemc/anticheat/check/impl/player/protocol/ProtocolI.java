package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckInfo(name = "Protocol", type = "I", description = "Sending action while attacking.")
public class ProtocolI extends Check {

    public ProtocolI(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            final WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);

            check: {
                if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) break check;

                final boolean invalid = data.getActionProcessor().isSendingAction();
                final boolean exempt = isExempt(ExemptType.FAST, ExemptType.LAG_SPIKE);

                if (invalid && !exempt) {
                    increaseVlBy(1.45);
                    fail("Player attacked in-correctly");
                    staffAlert();
                }
            }
        }
    }
}
