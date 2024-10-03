package com.elevatemc.anticheat.check.impl.player.invalid;

import com.elevatemc.anticheat.SosaPlugin;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.manager.PunishmentManager;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import org.bukkit.Bukkit;

@CheckInfo(name = "Invalid", type = "B", description = "Custom payload detected")
public class InvalidB extends Check {

    public InvalidB(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
            final WrapperPlayClientPluginMessage wrappedPacketInCustomPayload = new WrapperPlayClientPluginMessage(event);
            final String payload = wrappedPacketInCustomPayload.getChannelName();
            if (payload.equalsIgnoreCase("Remix") || payload.contains("CRYSTAL") || payload.contains("matrix") || payload.equalsIgnoreCase("CRYSTAL|6LAKS0TRIES") || payload.equalsIgnoreCase("CrystalWare") || payload.equalsIgnoreCase("CRYSTAL|KZ1LM9TO") || payload.equalsIgnoreCase("Misplace") || payload.equalsIgnoreCase("reach") || payload.equalsIgnoreCase("lmaohax") || payload.equalsIgnoreCase("Reach Mod") || payload.equalsIgnoreCase("cock") || payload.equalsIgnoreCase("Vape v3") || payload.equalsIgnoreCase("1946203560") || payload.equalsIgnoreCase("#unbanearwax") || payload.equalsIgnoreCase("EARWAXWASHERE") || payload.equalsIgnoreCase("Cracked Vape") || payload.equalsIgnoreCase("EROUAXWASHERE") || payload.equalsIgnoreCase("moon:exempt") || payload.equalsIgnoreCase("Vape") || payload.equalsIgnoreCase("WDL|INIT") || payload.equalsIgnoreCase("WDL|CONTROL") || payload.equalsIgnoreCase("Bspkrs Client")) {
                fail("Custom payload, t=" + payload);
                new PunishmentManager().punish(this, data);
            }
            if (getVl() > .1) {

                Bukkit.getScheduler().runTask(SosaPlugin.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "banwave add " + data.getPlayer().getName() + " Invalid Type B"));
            }
        }
    }
}
