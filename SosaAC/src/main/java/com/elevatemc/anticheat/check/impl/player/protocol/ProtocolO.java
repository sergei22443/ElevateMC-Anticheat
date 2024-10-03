package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckInfo(name = "Protocol", type = "O", description = "Invalid sword block packet order.")
public class ProtocolO extends Check {

    public ProtocolO(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            final WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);

            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                boolean invalid = data.getActionProcessor().isSendingDig();
                boolean sword = PlayerUtil.isHoldingSword(data.getPlayer());

                boolean exempt = Sosa.INSTANCE.getTickManager().getTicks() - data.getActionProcessor().getLastItemDrop() < 10 || hitTicks() > 4;

                if (exempt) return;

                if (invalid && sword) {
                    if (increaseBuffer() > 3) {
                        increaseVlBy(.15);
                        fail("Player attacked and dug in the same tick");
                        staffAlert();
                        multiplyBuffer(.25);
                    }
                } else {
                    decreaseBufferBy(.25);
                }
            }
        }
    }
}
