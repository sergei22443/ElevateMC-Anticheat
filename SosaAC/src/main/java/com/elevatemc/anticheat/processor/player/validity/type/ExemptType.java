package com.elevatemc.anticheat.processor.player.validity.type;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.elevatemc.anticheat.util.server.ServerUtil;
import com.elevatemc.anticheat.util.server.block.BlockUtil;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.util.NumberConversions;

import java.util.function.Function;

@Getter
public enum ExemptType {

    FAST(data -> data.getConnectionProcessor().isFast()),
    SLIME(BlockUtil::isSlime),
    CLIMBABLE (data -> BlockUtil.isClimbable(data.getPlayer().getLocation())),
    WEB(BlockUtil::isWeb),
    VEHICLE(data -> data.getPlayer().isInsideVehicle()),
    ONVEHICLE(data -> data.getPositionProcessor().isNearVehicle()),
    ICE(BlockUtil::isIce),
    TPS(data -> ServerUtil.getTPS() < 19.0),
    LIQUID(data -> data.getPositionProcessor().getSinceNearLiquidTicks() < 30),
    FLIGHT(data -> data.getPositionProcessor().getSinceFlightTicks() < 40),
    TRAPDOOR(data -> data.getPositionProcessor().isNearTrapdoor()),
    DEPTH_STRIDER(data -> data.getPositionProcessor().getSinceNearLiquidTicks() < 40 && PlayerUtil.isWearingDepthStrider(data.getPlayer())),
    JOINED(data -> System.currentTimeMillis() - data.getJoinTime() < 5000L),
    COLLIDING_VERTICALLY(data -> data.getPositionProcessor().isCollidingVertically()),
    COLLIDING_HORIZONTALLY(data -> data.getPositionProcessor().isCollidingHorizontally()),
    SERVER_POSITION(data -> data.getActionProcessor().getSinceTeleportTicks() < 20),
    CREATIVE(data -> data.getPlayer().getGameMode().equals(GameMode.CREATIVE)),

    VELOCITY(data -> data.getVelocityProcessor().getTicksSinceVelocity() < 30),
    BLOCK_BREAK(data -> Sosa.INSTANCE.getTickManager().getTicks() - data.getActionProcessor().getLastBlockBreak() < 20),
    BLOCK_PLACE(data -> Sosa.INSTANCE.getTickManager().getTicks() - data.getActionProcessor().getLastBukkitBlockPlace() < 50),

    AUTOCLICKER(data -> data.getActionProcessor().isDigging() || data.getConnectionProcessor().isFast()
            || Sosa.INSTANCE.getTickManager().getTicks() - data.getActionProcessor().getLastDig() < 10
            || Sosa.INSTANCE.getTickManager().getTicks() - data.getActionProcessor().getLastBukkitDig() < 10
            || Sosa.INSTANCE.getTickManager().getTicks() - data.getActionProcessor().getLastItemDrop() < 10
            || Sosa.INSTANCE.getTickManager().getTicks() - data.getActionProcessor().getLastWindowClick() < 15
            || data.getPlayer().getGameMode() == GameMode.CREATIVE),
    CHUNK(data -> !data.getPlayer().getWorld().isChunkLoaded(NumberConversions.floor(data.getPositionProcessor().getX()) >> 4, NumberConversions.floor(data.getPositionProcessor().getZ()) >> 4)),
    LAG_SPIKE(data -> Sosa.INSTANCE.getTickManager().getTicks() - data.getConnectionProcessor().getLastLagSpike() < 20),
    POTION_EXPIRE(data -> Sosa.INSTANCE.getTickManager().getTicks() - data.getActionProcessor().getLastPotionExpire() < 30),

    TELEPORT(data -> Sosa.INSTANCE.getTickManager().getTicks() - data.getActionProcessor().getLastBukkitTeleport() < 60
            || System.currentTimeMillis() - data.getJoinTime() < 5000L
            || data.getActionProcessor().getSinceTeleportTicks() < 15),

    SPECTATOR(data -> data.getPlayer().getGameMode().equals(GameMode.SPECTATOR)),

    STAIR (data -> data.getPositionProcessor().isNearStair()),

     DEAD(data -> data.getPlayer().isDead());
    private final Function<PlayerData, Boolean> exception;

    ExemptType(final Function<PlayerData, Boolean> exception) {
        this.exception = exception;
    }
}
