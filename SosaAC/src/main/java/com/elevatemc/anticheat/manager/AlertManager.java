package com.elevatemc.anticheat.manager;

import com.elevatemc.anticheat.SosaPlugin;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.database.log.Log;
import com.elevatemc.anticheat.config.Config;
import com.elevatemc.anticheat.util.server.ColorUtil;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.elevatemc.anticheat.util.server.ServerUtil;
import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.data.PlayerData;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.*;

public class AlertManager {

    @Getter
    private final Set<Player> alertsEnabled = new HashSet<>();
    private String checkId;

    public void toggleAlerts(final Player player) {
        if (alertsEnabled.contains(player)) {
            alertsEnabled.remove(player);
            player.sendMessage(ColorUtil.translate(Config.ALERTS_DISABLED));
        } else {
            alertsEnabled.add(player);
            player.sendMessage(ColorUtil.translate(Config.ALERTS_ENABLED));
        }
    }
    public void handleAlert(final Check check, final PlayerData data, final String info) {
        if (data.getPlayer().hasMetadata("frozen")) return;
        if (data.getExemptProcessor().isAnticheatExempt(data.getPlayer())) return;
        if (Sosa.INSTANCE.isShittingItself()) return;

        APIManager.flagEvent(check);

        final int maxViolations = Config.MAX_VIOLATIONS.get(check.getClass().getSimpleName());
        check.setVl(check.getVl() + .75);

        final double vl = check.getVl();

        final TextComponent alertMessage = new TextComponent(ColorUtil.translate(Config.ALERTS_FORMAT)
                .replaceAll("%player%", data.getPlayer().getName())
                .replaceAll("%ping%", Integer.toString(PlayerUtil.getPing(data.getPlayer())))
                .replaceAll("%tps%", new DecimalFormat("##.##").format(ServerUtil.getTPS()))
                .replaceAll("%check%", check.getCheckInfo().name())
                .replaceAll("%description%", check.getCheckInfo().description())
                .replaceAll("%version%", PlayerUtil.getClientVersionToString(data))
                .replaceAll("%dev%", check.getCheckInfo().experimental() ? ColorUtil.translate("*") : "")
                .replaceAll("%vl%", new DecimalFormat("##.##").format(vl))
                .replaceAll("%maxvl%", new DecimalFormat("##.##").format(maxViolations))
                .replaceAll("%percentage%", ColorUtil.translate(getBar(vl, maxViolations)))
                .replaceAll("%type%", check.getCheckInfo().type()));

        for (final String clickCommands : Config.ALERTS_CLICK_COMMANDS) {
            alertMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    clickCommands.replaceAll("%player%", data.getPlayer().getName())));
        }

            final StringBuilder builder = new StringBuilder();
            int listSize = Config.ALERTS_HOVER_MESSAGES.size();
            int i = 1;

            for (final String hoverMessages : Config.ALERTS_HOVER_MESSAGES) {
                if (hoverMessages.contains("%info%") && info == null) {
                    i++;
                    continue;
                }
                if (i == listSize) {
                    builder.append(hoverMessages);
                } else {
                    builder.append(hoverMessages).append("\n");
                }
                i++;
            }

            final String hoverMessage = ColorUtil.translate(builder.toString()
                    .replaceAll("%player%", data.getPlayer().getName())
                    .replaceAll("%check%", check.getCheckInfo().name())
                    .replaceAll("%vl%", new DecimalFormat(".##").format(vl))
                    .replaceAll("%version%", PlayerUtil.getClientVersionToString(data))
                    .replaceAll("%ping%", Integer.toString(PlayerUtil.getPing(data.getPlayer())))
                    .replaceAll("%description%", check.getCheckInfo().description())
                    .replaceAll("%dev%", check.getCheckInfo().experimental() ? ColorUtil.translate("EXP") : "")
                    .replaceAll("%type%", check.getCheckInfo().type())
                    .replaceAll("%tps%", new DecimalFormat("##.##").format(ServerUtil.getTPS()))
                    .replaceAll("%info%", info)
                    .replaceAll("%check%", check.getCheckInfo().name()));

            Log log = new Log(data.getPlayer().getUniqueId(), check.getName(), check.getType(), info, vl, PlayerUtil.getPing(data.getPlayer()), PlayerUtil.getClientVersionToString(data) + " " + data.getClientBrand());

            SosaPlugin.getInstance().getLogManager().getQueuedLogs().add(log);

            alertMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverMessage).create()));

        Bukkit.getOnlinePlayers().stream().filter(player -> player.isOp() || player.hasPermission("sosa.alerts.advanced")).filter(alertsEnabled::contains).forEach(player -> player.spigot().sendMessage(alertMessage));

            final boolean punishable = Config.PUNISHABLE.get(check.getClass().getSimpleName());
            if (vl >= maxViolations && punishable) {
                Sosa.INSTANCE.getPunishmentManager().punish(check, data);
            }
    }

    public void staffAlert(final Check check, final PlayerData data) {
        if (data.getPlayer().hasMetadata("frozen")) return;
        if (data.getExemptProcessor().isAnticheatExempt(data.getPlayer())) return;
        disguisedCheck(check);

        final String alertMessage = ColorUtil.translate("&c%player% &7is using &c%check%")
               .replaceAll("%player%", data.getPlayer().getName())
                .replaceAll("%check%", this.checkId);

       Bukkit.getOnlinePlayers().stream().filter(player -> !player.isOp() && player.hasPermission("sosa.alerts.basic") && !player.hasPermission("sosa.alerts.advanced")).filter(alertsEnabled::contains).forEach(player -> player.sendMessage(alertMessage));
    }

    public void sendMessage(final String message) {
        if (alertsEnabled.isEmpty()) return;
        alertsEnabled.forEach(player -> player.sendMessage(ColorUtil.translate(message)));
    }


    public void disguisedCheck(final Check check) {
        String name = check.getName();

        /*
        Eh, Will continue later I hate the yandere dev code below LOOOOOL.
        switch (check.getName()) {
            case "Kill Aura": {
                checkId = "Combat-AURA";
                break;
            }
            case "Improbable": {
                checkId = "Combat-IMPROBABILITY";
                break;
            }
        }
         */

        if (name.equalsIgnoreCase("kill aura")) {
            checkId = "Kill Aura";
        }
        else if (name.equalsIgnoreCase("improbable")) {
             this.checkId = "Impossible Actions";
         }
        else if (name.equalsIgnoreCase("speed")) {
            this.checkId = "Speed";
        }
        else if (name.equalsIgnoreCase("velocity")) {
            this.checkId = "Velocity";
        } else if (name.equalsIgnoreCase("aim")) {
            this.checkId = "Combat Modifications";
        }
        else if (name.equalsIgnoreCase("flight")) {
            this.checkId = "Flight";
        }
        else if (name.equalsIgnoreCase("motion")) {
            this.checkId = "Motion";
        }
         else if (name.equalsIgnoreCase("nofall")) {
             this.checkId = "NoFall";
         }
        else  if (name.equalsIgnoreCase("interact")) {
            this.checkId = "Interact";
        }
        else if (name.equalsIgnoreCase("invalid")) {
            this.checkId = "Hacked Client";
        }
        else if (name.equalsIgnoreCase("ping spoof")) {
            this.checkId = "Spoof";
        } else if (name.equalsIgnoreCase("protocol")) {
            this.checkId = "Game Logic";
        }
        else if (name.equalsIgnoreCase("timer")) {
            this.checkId = "Game Speed";
        } else if (name.equalsIgnoreCase("Inventory")) {
            this.checkId = "Inventory";
        } else if (name.equalsIgnoreCase("Refill")) {
             this.checkId = "PR-12231";
        } else if (name.equalsIgnoreCase("Blink")) {
             this.checkId = "PR-11521";
        } else if (name.equalsIgnoreCase("Hitbox")) {
            this.checkId = "CR-11522";
        } else if (name.equalsIgnoreCase("Criticals")) {
            this.checkId = "CR-11576";
        } else if (name.equalsIgnoreCase("Attack")) {
            this.checkId = "FUN-242";
        } else if (name.equalsIgnoreCase("Auto Clicker")) {
            this.checkId = "CR-11523";
        } else if (name.equalsIgnoreCase("Pattern")) {
            this.checkId = "CR-11524";
        }
    }

    public String getBar(double vl, double maxVl) {
        double percent =  vl / maxVl * 10.0;
        StringBuilder result = new StringBuilder("&8[");

        for (int i = 0; i < 10; ++i) {
            if (i < percent) {
                result.append("&f:");
            }
            else {
                result.append("&7:");
            }
        }
        result.append("&8]");
        return result.toString();
    }
}