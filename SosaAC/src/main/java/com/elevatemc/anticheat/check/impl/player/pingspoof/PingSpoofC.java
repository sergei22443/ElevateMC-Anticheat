package com.elevatemc.anticheat.check.impl.player.pingspoof;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;

@CheckInfo(name = "Ping Spoof", type = "C", description = "Invalid KeepAlive packets")
public class PingSpoofC extends Check {

    private Integer lastKeepAlive;

    public PingSpoofC(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
            final WrapperPlayClientKeepAlive wrapper = new WrapperPlayClientKeepAlive(event);

             long keepAliveId = wrapper.getId();

             final boolean exempt = isExempt(ExemptType.TPS, ExemptType.LAG_SPIKE);
             if (lastKeepAlive != null && lastKeepAlive > keepAliveId && !exempt) {
                 fail("lastKA" + lastKeepAlive + " currentKA=" + keepAliveId);
                 staffAlert();
             }
             lastKeepAlive = Math.toIntExact(keepAliveId);
        }
    }
}
