package com.elevatemc.anticheat.command.impl;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.config.Config;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.cache.CacheUtil;
import com.elevatemc.anticheat.util.server.BotUtils;
import com.elevatemc.anticheat.util.server.ColorUtil;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.elevatemc.anticheat.util.type.BotTypes;
import com.elevatemc.anticheat.util.type.Probability;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import java.util.Collections;

public class SosaCommand implements CommandExecutor {

    public void sendMessage(final CommandSender player, final String message) {
        player.sendMessage(ColorUtil.translate(message));
    }
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!sender.hasPermission("sosa.developer") || !sender.hasPermission("sosa.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou don't have permission to use this command."));
            return true;
        } else if (args.length == 0) {
            sendMessage(sender, ("&8&l------»&r  " + "&c&lSosa" + "  §8§l«------"));
            sendMessage(sender, "&cSosa Info: ");
            sendMessage(sender, "&cTotal checks: &7" + Sosa.INSTANCE.getPlayerDataManager().getPlayerData((Player) sender).getChecks().size());
            sendMessage(sender,"&7/&calerts &7- &eEnables/Disables alerts");
            sendMessage(sender,"&7/&clogs &c<&eplayer&c> &7- &eView a players logs");
            sendMessage(sender,"&7/&cforceban &c<&eplayer&c> &7- &eBan a player through the AC");
            sendMessage(sender, ("&7/&cbanwave  &7- &eInformation about the Banwave command"));
            sendMessage(sender, ("&7/&csosa info <&eplayer&c> &7- &eShows information about the player"));
            sendMessage(sender, "&7/&csosa &cdisablecheck&7/&cenablecheck &7- &eEnable&7/&eDisable checks");
            sendMessage(sender, ("&7/&csosa reset &7- &eReset violations"));
            sendMessage(sender, ("&7/&csosa reload &7- &eReload the config file"));
            sendMessage(sender, ("&7/&csosa crash &7- <&eplayer&c> &7- &eCrash a players MC"));
            sendMessage(sender,"&8&m---------------------------");
            return true;
        } else if (args[0].equalsIgnoreCase("disablecheck")) {
            if (sender.hasPermission("sosa.disablecheck")) {
                if (args.length == 1) {
                    sendMessage(sender, "&cUsage: /sosa disablecheck (checkname)");
                } else {
                    final String checkName = args[1];

                    if (Config.ENABLED_CHECKS.get(checkName)) {
                        Config.ENABLED_CHECKS.remove(checkName);
                        Config.ENABLED_CHECKS.put(checkName, false);
                        CacheUtil.updateCheckValues();
                        sendMessage(sender, "&cDisabled check: " + checkName);
                    } else {
                        sendMessage(sender, "&cInvalid check!");
                        return true;
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("enablecheck")) {
            if (sender.hasPermission("sosa.enablecheck")) {
                if (args.length == 1) {
                    sendMessage(sender, "&cUsage: /sosa enablecheck (checkname)");
                } else {
                    String checkName = args[1];
                    if (!Config.ENABLED_CHECKS.get(checkName)) {
                        Config.ENABLED_CHECKS.remove(checkName);
                        Config.ENABLED_CHECKS.put(checkName, true);
                        CacheUtil.updateCheckValues();
                        sendMessage(sender, "&enabled check: " + checkName);
                    } else {
                        sendMessage(sender, "&cInvalid check!");
                        return true;
                    }
                }
            }
        } else if (args[0].equalsIgnoreCase("reset")) {
            if (sender.hasPermission("sosa.reset")) {
                Sosa.INSTANCE.getResetManager().reset();
                sendMessage(sender, "&aSuccessfully reset all violations!");
            } else {
                sendMessage(sender, "&cNo permission!");
            }
        } else if (args[0].equalsIgnoreCase("crash")) {
            if (sender.hasPermission("sosa.crash")) {
                if (args.length == 1) {
                    sendMessage(sender, "&cUsage: /sosa crash (player)");
                } else {
                    String name = args[1];

                    Player player = Bukkit.getPlayer(name);

                    if (player != null) {
                        new Thread(() -> {
                            if(!player.isOnline()) {
                                return;
                            }
                            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(10, 0.0F));

                            for (int i = 0; i < 10; ++i) {
                                player.playSound(player.getLocation(), Sound.BAT_DEATH, 2.14748365E9F, 0.2F);
                                player.playSound(player.getLocation(), Sound.ANVIL_LAND, 2.14748365E9F, 0.2F);
                                player.playSound(player.getLocation(), Sound.ARROW_HIT, 2.14748365E9F, 0.2F);
                                player.playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 1.0F, 0.2F);
                            }
                            try {
                                Thread.sleep(2500);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
                                    new PacketPlayOutExplosion(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                                            Float.POSITIVE_INFINITY, Collections.EMPTY_LIST,
                                            new Vec3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)));
                            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
                                    new PacketPlayOutWorldParticles(EnumParticle.PORTAL, true,
                                            Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Integer.MAX_VALUE));
                        }).start();
                        sendMessage(sender, "&aSuccessfully crashed " + player.getName() + "!");
                    } else {
                        sendMessage(sender, "&cInvalid player!");
                        return true;
                    }
                }
            } else {
                sendMessage(sender, "&cNo permission!");
                return true;
            }
        }  else if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("sosa.developer")) {
                Sosa.INSTANCE.reload();
                sendMessage(sender, "&aSuccessfully reloaded! (Run this command twice).");
            } else {
                sendMessage(sender, "&cNo permission!");
            }
        } else if (args[0].equalsIgnoreCase("exempt")) {
            if (sender.getName().equalsIgnoreCase("DawnX01") || sender.getName().equalsIgnoreCase("Dodged") || sender.getName().equalsIgnoreCase("Moose1301")) {
                if (args.length == 1) {
                    sendMessage(sender, "&cUsage: /sosa exempt (player)");
                } else {
                    String name = args[1];

                    Player player = Bukkit.getPlayer(name);

                    if (player != null) {
                        PlayerData data = Sosa.INSTANCE.getPlayerDataManager().getPlayerData(player.getPlayer());
                        data.getExemptProcessor().setExempt(player);
                        sendMessage(sender,"&a " + player.getName() + " is " + (data.getExemptProcessor().isAnticheatExempt(player) ? "no longer" : "now") + " getting flagged by Sosa.");
                    } else {
                        sendMessage(sender, "&cInvalid player!");
                        return true;
                    }
                }
            } else {
                sendMessage(sender, "&cNo permission!");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("info")) {
            if (sender.hasPermission("sosa.info")) {
                if (args.length == 1) {
                    sendMessage(sender, "&cUsage: /sosa info (player)");
                } else {
                    String name = args[1];
                    Player player = Bukkit.getPlayer(name);

                    PlayerData data = Sosa.INSTANCE.getPlayerDataManager().getPlayerData(player.getPlayer());

                    String clientBrand = data.getClientBrand();
                    String clientVersion = PlayerUtil.getClientVersionToString(data);

                    int playerPing = PlayerUtil.getPing(player.getPlayer());
                    int sens = data.getSensitivityHolder().getIntegerSensitivity();

                    double cps = data.getClickProcessor().getCps();
                    double totalSessionVl = 0.0;

                    long lastTeleport = data.getActionProcessor().getSinceTeleportTicks() / 20;
                    long fd = data.getConnectionProcessor().getFlyingDelay();

                    boolean lagging = playerPing > 350;
                    boolean sLag = data.getConnectionProcessor().getLastTransactionReceived().get() > 300;
                    boolean lag = fd > 220L;

                    Probability probability = Probability.UNSURE;
                    String yes = "";

                    for (Check check : data.getChecks()) {
                        if (player.getUniqueId() == data.getPlayer().getUniqueId()) {
                            if (check.getVl() > 0.0) totalSessionVl += check.getVl();
                        }
                    }

                    if (totalSessionVl > 10 && totalSessionVl < 15) {
                        probability = Probability.UNSURE;
                    } else if (totalSessionVl > 15 && totalSessionVl < 25) {
                        probability = Probability.PROBABLY;
                    } else if (totalSessionVl > 25 && totalSessionVl < 35) {
                        probability = Probability.IMPROBABLE;
                    } else if (totalSessionVl > 35) {
                        probability = Probability.ILLEGITIMATE;
                    }

                    switch (probability) {
                        case UNSURE: {
                            yes = ColorUtil.translate("&aLegitimate");
                            break;
                        }
                        case PROBABLY: {
                            yes = ColorUtil.translate("&2Probably");
                            break;
                        }
                        case IMPROBABLE: {
                            yes = ColorUtil.translate("&cImprobable");
                            break;
                        }
                        case ILLEGITIMATE: {
                            yes = ColorUtil.translate("&4Illegitimate");
                            break;
                        }
                    }

                    sendMessage(sender, "&c&m-------------------");
                    sendMessage(sender, "&c" + player.getName() + " &e's AC Info");
                    sendMessage(sender, "&eBrand: " + clientBrand);
                    sendMessage(sender, "&eVersion: " + clientVersion);
                    sendMessage(sender, "");
                    sendMessage(sender, "&eProbability: " + yes);
                    sendMessage(sender, "&eSession: " + totalSessionVl);
                    sendMessage(sender, "&ePing: " + playerPing);
                    sendMessage(sender, "&eSensitivity: " + sens);
                    sendMessage(sender, "&eCPS: " + cps);
                    sendMessage(sender, "&eLagging (P | S): " + (lagging || lag) + " " + sLag);
                    sendMessage(sender, "&eLast TP: " + lastTeleport);
                    sendMessage(sender, "&c&m-------------------");
                }
            } else {
                sendMessage(sender, "&cNo permission!");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("velocity")) {
            if (sender.hasPermission("sosa.velocity")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.setVelocity(new Vector(0.5, 0.5, 0.5));
                } else {
                    sendMessage(sender, "bad console >:(");
                }
            }
        } else if (args[0].equalsIgnoreCase("forcebot")) {
            if (sender.hasPermission("sosa.velocity")) {
                if (args.length >= 3) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sendMessage(sender, "&cThis player does not exist!");

                    }
                    BotTypes botType = BotTypes.NORMAL;
                    try {
                        botType = BotTypes.valueOf(args[2]);
                    } catch (IllegalArgumentException Exception) {
                        sendMessage(sender, "&cThis mode does not exist!");
                    }
                    PlayerData attackerData = Sosa.INSTANCE.getPlayerDataManager().getPlayerData((Player) sender);
                    PlayerData victimData = Sosa.INSTANCE.getPlayerDataManager().getPlayerData(target);
                    if (victimData.getCombatProcessor().getHitTicks() > 40) {
                        sendMessage(sender, "&cThis player doesnt have any targets!");
                    }
                    if (victimData.getBotProcessor().hasBot) {
                        sendMessage(sender, "&cThis player is already being checked by a bot!");
                    }
                    BotUtils.spawnBotEntity(victimData, attackerData, botType);
                    sendMessage(sender, "&aSending this bot to the player!");
                }
            }
        }
        return true;
    }
}
