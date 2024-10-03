package com.elevatemc.anticheat.processor.player.listener;

import com.elevatemc.api.SosaFlagEvent;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CrackshotListener implements Listener {

    private final Map<UUID, Long> mrtallyman = new HashMap<>();

    @EventHandler
    public void onSosaFlag(final SosaFlagEvent event) {
        if ((event.getCheck().getName().equalsIgnoreCase("flight") && event.getType().equalsIgnoreCase("a") || event.getCheck().getName().equalsIgnoreCase("motion") && event.getType().equalsIgnoreCase("a") || event.getCheck().getName().equalsIgnoreCase("speed") && event.getType().equalsIgnoreCase("f")  ) && mrtallyman.containsKey(event.getPlayer().getUniqueId()) && System.currentTimeMillis() - mrtallyman.get(event.getPlayer().getUniqueId()) < 5000L) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(final WeaponDamageEntityEvent event) {
        if (event.getVictim() instanceof Player) {
            final Player player = (Player)event.getVictim();
            mrtallyman.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onLeave(final PlayerQuitEvent event) {
        mrtallyman.remove(event.getPlayer().getUniqueId());
    }
}
