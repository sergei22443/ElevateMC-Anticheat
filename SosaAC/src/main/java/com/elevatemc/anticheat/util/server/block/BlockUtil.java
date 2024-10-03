package com.elevatemc.anticheat.util.server.block;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public final class BlockUtil
{
    public static List<Material> getNearbyBlocksAsync(final Location location, final int radius) {
        final List<Material> nearby = new ArrayList<>();
        final int blockX = location.getBlockX();
        final int blockY = location.getBlockY();
        final int blockZ = location.getBlockZ();
        for (int x = blockX - radius; x <= blockX + radius; ++x) {
            for (int y = blockY - radius; y <= blockY + radius + 1; ++y) {
                for (int z = blockZ - radius; z <= blockZ + radius; ++z) {
                    nearby.add(getBlockTypeASync(location.getWorld(), x, y, z));
                }
            }
        }
        return nearby;
    }

    public static Material getBlockTypeASync(final World world, final int x, final int y, final int z) {
        if (world.isChunkLoaded(x >> 4, z >> 4)) {
            return world.getBlockAt(x, y, z).getType();
        }
        return Material.AIR;
    }

    public static boolean isClimbable(final Location loc) {
        final double y = loc.getY();
        for (double x = -0.5; x <= 0.5; x += 0.2) {
            for (double z = -0.5; z <= 0.5; z += 0.2) {
                final Location check = new Location(loc.getWorld(), loc.getX() + x, y, loc.getZ() + z);
                if (check.getBlock().getType().equals(Material.VINE) || check.getBlock().getType().equals(Material.LADDER) || check.getBlock().getType().equals(Material.SNOW) || check.getBlock().getType().equals(Material.WEB)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isWeb(PlayerData data) {
        Player player = data.getPlayer();
        return player.getLocation().getBlock().getType() == Material.WEB || player.getLocation().clone().add(0, 1, 0).getBlock().getType() == Material.WEB;
    }

    public static boolean isIce(PlayerData data) {
        Player player = data.getPlayer();
        return player.getLocation().getBlock().getType() == Material.ICE || player.getLocation().clone().add(0, 1, 0).getBlock().getType() == Material.PACKED_ICE;
    }

    public static boolean isStair(PlayerData data) {
        PlayerData p = Sosa.INSTANCE.getPlayerDataManager().getPlayerData(data.getPlayer());
        Player player = p.getPlayer();
        double expand = 0.3;
        if (!p.getExemptProcessor().isExempt(ExemptType.JOINED)) {
            for (double x = -expand; x <= expand; x += expand) {
                for (double z = -expand; z <= expand; z += expand) {
                    if (player.getLocation().clone().add(z, 0, x).getBlock().getType().toString().toLowerCase().contains("stairs")) {
                        return true;
                    }
                    if (player.getLocation().clone().add(z, player.getEyeLocation().getY(), x).getBlock().getType().toString().contains("STAIRS")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isTrapdoor(final Material material) {
        return material == Material.TRAP_DOOR || material == Material.IRON_TRAPDOOR;
    }

    public static boolean isStair(final Material material) {
        return material.toString().contains("STAIR");
    }

    public static boolean isSlab(final Material material) {
        return material.toString().contains("STEP");
    }

    public static boolean isIce(final Material material) {
        return material == Material.ICE || material == Material.PACKED_ICE;
    }

    public static boolean isSlime(final Material material) {
        return material == Material.SLIME_BLOCK;
    }

    public static boolean isSlime(PlayerData data) {
            Player player = data.getPlayer();
            double expand = 0.3;

        if (!data.getExemptProcessor().isExempt(ExemptType.JOINED)) {
            for (double x = -expand; x <= expand; x += expand) {
                for (double z = -expand; z <= expand; z += expand) {
                    if (player.getLocation().clone().add(z, 0, x).getBlock().getType().toString().toLowerCase().contains("slime")) {
                        return true;
                    }
                    if (player.getLocation().clone().add(z, player.getEyeLocation().getY(), x).getBlock().getType().toString().toLowerCase().contains("slime")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isAir(final Material material) {
        return material == Material.AIR;
    }

    public static boolean isWeb(final Material material) {
        return material == Material.WEB;
    }

    public static boolean isVehicle(final Material material) {
        return material == Material.BOAT || material == Material.MINECART;
    }

    public static boolean isLiquid(final Material material) {
        return material == Material.WATER || material == Material.LAVA || material == Material.STATIONARY_LAVA || material == Material.STATIONARY_WATER;
    }

    public static boolean isLiquid(PlayerData data) {
        Player player = data.getPlayer();
        double expand = 0.3;
        for (double x = -expand; x <= expand; x += expand) {
            for (double z = -expand; z <= expand; z += expand) {
                if (player.getLocation().clone().add(z, 0, x).getBlock().isLiquid()) {
                    return true;
                }
                if (player.getLocation().clone().add(z, player.getEyeLocation().getY(), x).getBlock().isLiquid()) {
                    return true;
                }
            }
        }
        return false;
    }
    private BlockUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}