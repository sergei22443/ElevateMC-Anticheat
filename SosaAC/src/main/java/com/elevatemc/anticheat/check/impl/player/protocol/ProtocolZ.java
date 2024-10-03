package com.elevatemc.anticheat.check.impl.player.protocol;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Protocol", type = "Z", description = "Checks for client spoofing")
public class ProtocolZ extends Check {

    public ProtocolZ(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.WINDOW_CONFIRMATION) {
           if (!isExempt(ExemptType.JOINED)) {

               String clientBrand = data.getClientBrand();

               if (clientBrand == null) {
                   fail("Spoofed client");
               }
           }
       }
    }
}
