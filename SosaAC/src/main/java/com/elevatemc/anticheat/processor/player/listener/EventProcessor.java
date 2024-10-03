package com.elevatemc.anticheat.processor.player.listener;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.spigot.event.PostEntityTrackerEvent;
import net.minecraft.server.v1_8_R3.EntityHuman;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PotionEffectExpireEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EventProcessor implements Listener {

    @EventHandler
    public void onTeleport(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        final PlayerData data = Sosa.INSTANCE.getPlayerDataManager().getPlayerData(player);
        if (data == null) {
            return;
        }
        switch (event.getCause()) {
            case PLUGIN:
            case COMMAND: {
                data.getActionProcessor().handleBukkitTeleport();
                break;
            }
        }
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            final Player player = event.getPlayer();
            final PlayerData data = Sosa.INSTANCE.getPlayerDataManager().getPlayerData(player);
            if (data == null) {
                return;
            }
            switch (event.getAction()) {
                case LEFT_CLICK_BLOCK: {
                    data.getActionProcessor().handleBukkitDig();
                    break;
                }
                case RIGHT_CLICK_BLOCK: {
                    data.getActionProcessor().handleBukkitRightClickBlock();
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onExpire(final PotionEffectExpireEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player player = (Player)event.getEntity();
            final PlayerData data = Sosa.INSTANCE.getPlayerDataManager().getPlayerData(player);

            if (data == null) return;

            data.getActionProcessor().handlePotionExpire();
        }
    }

    @EventHandler
    public void onInventoryOpen(final InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            final PlayerData data = Sosa.INSTANCE.getPlayerDataManager().getPlayerData((Player) event.getPlayer());

            if (data == null) return;
            if (event.getPlayer().getEntityId() == data.getPlayer().getEntityId()) {
                data.getActionProcessor().handleInventoryOpen();

               //Bukkit.broadcastMessage("inventory status:" + data.getActionProcessor().isInventoryOpen());
               // Bukkit.broadcastMessage("opened inventory, entityid: " + data.getPlayer().getEntityId());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            final PlayerData data = Sosa.INSTANCE.getPlayerDataManager().getPlayerData((Player) event.getPlayer());

            if (data == null) return;

            if (event.getPlayer().getEntityId() == data.getPlayer().getEntityId()) {
                data.getActionProcessor().handleCloseWindow();

             //   Bukkit.broadcastMessage("inventory status:" + data.getActionProcessor().isInventoryOpen());
              //  Bukkit.broadcastMessage("closed inventory, entityid: " + data.getPlayer().getEntityId());
            }
        }
    }


    // Custom spigot event that runs post entity tracker
    @EventHandler
    public void onPostEntityTracker(final PostEntityTrackerEvent event) {
        for (EntityHuman human : event.getPlayers()) {
            PlayerData data = Sosa.INSTANCE.getPlayerDataManager().getPlayerData(human.getUniqueID());
            if (data == null) {
                return;
            }
            data.handlePostEntityTracker();
        }
    }

    @EventHandler
    public void onTickStart(final ServerTickStartEvent event) {
        Sosa.INSTANCE.getPlayerDataManager().getPlayerDataMap().forEach((uuid, data) -> data.handleTickStart());
    }
}
