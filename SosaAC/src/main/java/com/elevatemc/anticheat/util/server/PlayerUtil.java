package com.elevatemc.anticheat.util.server;

import com.elevatemc.anticheat.Sosa;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import lombok.experimental.UtilityClass;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import com.elevatemc.anticheat.data.PlayerData;
import java.util.*;
@UtilityClass
public class PlayerUtil {
    public static List<Material> getBlocksAbove(final PlayerData data) {
        final List<Material> blocksAbove = new ArrayList<>();
        final List<Material> nearbyBlocks = data.getPositionProcessor().getNearbyBlocks();
        if (nearbyBlocks == null) {
            return blocksAbove;
        }
        blocksAbove.add(nearbyBlocks.get(9));
        blocksAbove.add(nearbyBlocks.get(10));
        blocksAbove.add(nearbyBlocks.get(11));
        blocksAbove.add(nearbyBlocks.get(21));
        blocksAbove.add(nearbyBlocks.get(22));
        blocksAbove.add(nearbyBlocks.get(23));
        blocksAbove.add(nearbyBlocks.get(33));
        blocksAbove.add(nearbyBlocks.get(34));
        blocksAbove.add(nearbyBlocks.get(35));
        return blocksAbove;
    }
    public static List<Material> getBlocksSide(final PlayerData data) {
        final List<Material> blocksAbove = new ArrayList<>();
        final List<Material> nearbyBlocks = data.getPositionProcessor().getNearbyBlocks();
        if (nearbyBlocks == null) {
            return blocksAbove;
        }
        blocksAbove.add(nearbyBlocks.get(3));
        blocksAbove.add(nearbyBlocks.get(4));
        blocksAbove.add(nearbyBlocks.get(5));
        blocksAbove.add(nearbyBlocks.get(6));
        blocksAbove.add(nearbyBlocks.get(7));
        blocksAbove.add(nearbyBlocks.get(8));
        blocksAbove.add(nearbyBlocks.get(9));
        blocksAbove.add(nearbyBlocks.get(10));
        blocksAbove.add(nearbyBlocks.get(11));
        blocksAbove.add(nearbyBlocks.get(15));
        blocksAbove.add(nearbyBlocks.get(17));
        blocksAbove.add(nearbyBlocks.get(18));
        blocksAbove.add(nearbyBlocks.get(20));
        blocksAbove.add(nearbyBlocks.get(21));
        blocksAbove.add(nearbyBlocks.get(22));
        blocksAbove.add(nearbyBlocks.get(23));
        blocksAbove.add(nearbyBlocks.get(27));
        blocksAbove.add(nearbyBlocks.get(28));
        blocksAbove.add(nearbyBlocks.get(29));
        blocksAbove.add(nearbyBlocks.get(30));
        blocksAbove.add(nearbyBlocks.get(31));
        blocksAbove.add(nearbyBlocks.get(32));
        blocksAbove.add(nearbyBlocks.get(33));
        blocksAbove.add(nearbyBlocks.get(34));
        blocksAbove.add(nearbyBlocks.get(35));

        return blocksAbove;
    }


    public int getPing(final Player player) {
        return PacketEvents.getAPI().getPlayerManager().getPing(player);
    }

    public String getClientVersionToString(final PlayerData data) {
        if (data.getUser().getClientVersion() == null) {
            return "Unresolved";
        }
        return data.getUser().getClientVersion().toString().replaceAll("V_", "").replaceAll("_", ".");
    }

    public static double getBaseGroundSpeed(final Player player) {
        PlayerData data = Sosa.INSTANCE.getPlayerDataManager().getPlayerData(player.getPlayer());
        double speed = 0.288 + getSpeed(player) * 0.062f + (player.getWalkSpeed() - 0.2f) * 3.5;
        if (speed < 0.288) {
            speed = 0.288;
        }
        return speed;
    }

    public static int getSpeed(final Player p) {
        PlayerData data = Sosa.INSTANCE.getPlayerDataManager().getPlayerData(p.getPlayer());

        return data.getPotionProcessor().getSpeedBoostAmplifier();
    }

    public static int getJumpBoost(final Player p) {
        PlayerData data = Sosa.INSTANCE.getPlayerDataManager().getPlayerData(p.getPlayer());

        return data.getPotionProcessor().getJumpBoostAmplifier();
    }


    public float getBaseSpeed(final Player player, final float base) {
        return base + (getSpeed(player.getPlayer()) * 0.062f) + ((player.getWalkSpeed() - 0.2f) * 1.6f);
    }

    public static float getBaseSpeed(final Player player) {
        return 0.34f + (getSpeed(player.getPlayer()) * 0.062f) + (player.getWalkSpeed() - 0.2f) * 1.6f;
    }

    public boolean isHoldingSword(final Player player) {
        return player.getItemInHand().getType().toString().contains("SWORD");
    }

    public boolean isWearingDepthStrider(final Player player) {
        return player.getInventory().getBoots() != null && player.getInventory().getBoots().getEnchantmentLevel(Enchantment.DEPTH_STRIDER) > 0;
    }

}
