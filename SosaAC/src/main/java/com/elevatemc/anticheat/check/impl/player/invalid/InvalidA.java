package com.elevatemc.anticheat.check.impl.player.invalid;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.SosaPlugin;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import org.bukkit.Bukkit;

@CheckInfo(name = "Invalid", type = "A", description = "Custom payload detected")
public class InvalidA extends Check {

    private boolean pcebruh;

    public InvalidA(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
            final WrapperPlayClientPluginMessage wrapper = new WrapperPlayClientPluginMessage(event);

            final String channel = wrapper.getChannelName();
            pcebruh = true;

            switch (channel) {
                case "CRYSTAL|6LAKS0TRIES":
                case "1946203560":
                case "0SO1Lk2KASxzsd":
                case "LOLIMAHCKER":
                case "customGuiOpenBspkrs":
                case "mincraftpvphcker":
                case "cock":
                case "lmaohax":
                case "MCnetHandler":
                case "L0LIMAHCKER":
                case "218c69d8875f":
                    fail("Cracked Client: " + channel);
                    break;
                default:
                    pcebruh = false;
                    break;
            }
            if (pcebruh) {
                Sosa.INSTANCE.getPunishmentManager().punish(this, data);
            }
            if (getVl() > .1) {

                Bukkit.getScheduler().runTask(SosaPlugin.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "banwave add " + data.getPlayer().getName() + " Invalid Type A"));
            }
        }
    }
}
