package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Protocol", type = "J", description = "Placing while switching slots.")
public class ProtocolJ extends Check {

    public ProtocolJ(final PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            final boolean invalid = data.getActionProcessor().isPlacing();

            final boolean exempt = isExempt(ExemptType.FAST, ExemptType.LAG_SPIKE, ExemptType.CREATIVE);

            if (invalid && !exempt) {
                increaseVlBy(.85);
                fail("Player placed while switching slots");
                staffAlert();
            }
        }
    }
}
