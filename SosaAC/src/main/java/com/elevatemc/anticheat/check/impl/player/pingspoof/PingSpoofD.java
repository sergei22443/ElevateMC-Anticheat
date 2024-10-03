
package com.elevatemc.anticheat.check.impl.player.pingspoof;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientResourcePackStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResourcePackSend;
import org.apache.commons.lang3.StringUtils;

import java.util.Deque;
import java.util.LinkedList;

@CheckInfo(name = "Ping Spoof", type = "D", description = "Fake Delaying", experimental = true)
public class PingSpoofD extends Check {
    private final Deque<Long> ids = new LinkedList<>();
    private Long next;
    

    public PingSpoofD(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        boolean exempt = isExempt(ExemptType.JOINED);
        if(!data.getUser().getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8)) {
            return;
        }
        if (event.getPacketType() == PacketType.Play.Client.RESOURCE_PACK_STATUS) {
            WrapperPlayClientResourcePackStatus sts = new WrapperPlayClientResourcePackStatus(event);
            String fixedHash = sts.getHash().replace("a", "");
            if (!StringUtils.isNumeric(sts.getHash())) {
                //debug("Invalid id: (ID: " + sts.getHash() + ")");
                return;
            }
            if (!sts.getResult().equals(WrapperPlayClientResourcePackStatus.Result.FAILED_DOWNLOAD) && !exempt) {
                this.fail("Successfully downloaded - Invalid");

                return;
            }
            if (this.ids.isEmpty() && !exempt) {
                this.fail("Packet coming from idk where");
                return;
            }
            long var = Long.decode(sts.getHash());
            if (!this.ids.contains(var) && !exempt) {
                this.fail("Keep alive does not exist bruh");
                return;
            }
            long id = this.ids.poll();
            if (id != var && this.ids.contains(var)) {
                while (id != var && !this.ids.isEmpty()) {
                    id = this.ids.poll();
                    if (exempt) continue;
                    this.fail("Invalid identifier, polling queue...");
                }
            } else {
                this.next = var;
                this.ids.remove(var);
                //debug("resource=" + var);
            }
        }
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if(!data.getUser().getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8)) {
            return;
        }
        if(event.getPacketType() == PacketType.Play.Server.KEEP_ALIVE) {
            WrapperPlayServerKeepAlive ka = new WrapperPlayServerKeepAlive(event);
            this.ids.add(ka.getId());
            String encoded = String.valueOf(ka.getId());

           // debug("Sending Resource Pack Send: ID: " + encoded);

            String fixedHash = encoded;
            while (fixedHash.length() < 40) {
                fixedHash += "a";
            }
            WrapperPlayServerResourcePackSend packet =  new WrapperPlayServerResourcePackSend(
                    "level://" + Math.random() + "/resources.zip", fixedHash,
                    true, null);

            data.getUser().sendPacket(packet);
        }
    }
}
