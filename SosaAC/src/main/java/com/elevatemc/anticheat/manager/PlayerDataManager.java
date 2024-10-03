package com.elevatemc.anticheat.manager;

import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerDataManager {

    @Getter
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public PlayerData getPlayerData(final Player player) {
        return playerDataMap.get(player.getUniqueId());
    }

    public PlayerData getPlayerData(final User user) {
        return playerDataMap.get(user.getUUID());
    }

    public PlayerData getPlayerData(final UUID uuid) {
        return playerDataMap.get(uuid);
    }


    public void add(final User user) {
        playerDataMap.put(user.getUUID(), new PlayerData(user));
        System.out.println("UUID: " + user.getUUID());
    }

    public void remove(final User user) {
        playerDataMap.remove(user.getUUID());
    }

    public Collection<PlayerData> getAllData() {
        return playerDataMap.values();
    }
}
