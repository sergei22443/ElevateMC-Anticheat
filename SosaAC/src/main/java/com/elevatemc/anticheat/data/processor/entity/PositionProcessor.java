package com.elevatemc.anticheat.data.processor.entity;

import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.elevatemc.anticheat.util.server.block.BlockUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.teleport.RelativeFlag;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import org.bukkit.Location;
import org.bukkit.World;
import lombok.Getter;
import org.bukkit.Material;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Getter
public class PositionProcessor
{
    private final PlayerData data;
    private double x;
    private double y;
    private double z;
    private double lastX;
    private double lastY;
    private double lastZ;
    private double deltaX;
    private double deltaY;
    private double deltaZ;
    public double deltaXYZ;
    private double lastDeltaX;
    private double lastDeltaZ;
    private double lastDeltaY;
    public double lastDeltaXYZ;
    private double deltaXZ;
    private double lastDeltaXZ;
    private double tpX;
    private double tpY;
    private double tpZ;
    private double jumpMotion;
    private boolean teleporting;
    private boolean clientOnGround;
    private boolean mathematicallyOnGround;
    private boolean lastClientOnGround;
    private boolean lastMathematicallyOnGround;
    private boolean jumped;
    private boolean inAir;
    private boolean nearLiquid;
    private boolean nearTrapdoor;
    private boolean collidingVertically;
    private boolean collidingHorizontally;
    private boolean nearSlime;
    private boolean nearStair;
    private boolean nearSlab;
    private boolean nearIce;
    private boolean nearVehicle;
    private boolean inWeb, nearWeb;
    private boolean lastMovementIncludedPosition;
    private int clientGroundTicks;
    private int clientAirTicks;
    private int mathematicallyOnGroundTicks;
    private int sinceJumpTicks;
    private int sinceFlightTicks;
    private int sinceNearLiquidTicks;
    private int serverAirTicks;
    private int sinceNearSlimeTicks;
    private int sinceNearIceTicks;
    private int sinceCollidingVerticallyTicks;
    private int sinceNearTrapdoorTicks;
    private int ticksSinceWeb;
    private List<Material> nearbyBlocks;
    private List<Material> blocksAbove;
    private List<Material> blocksSide;
    private World world;
    private final Queue<Vector3d> teleports = new ArrayDeque<>();

    public PositionProcessor(final PlayerData data) {
        this.data = data;
    }

    public void handleFlying(final WrapperPlayClientPlayerFlying wrapper) {
        final boolean position = wrapper.hasPositionChanged();

        this.teleporting = false;
        this.tpX = (position ? wrapper.getLocation().getX() : this.tpX);
        this.tpY = (position ? wrapper.getLocation().getY() : this.tpY);
        this.tpZ = (position ? wrapper.getLocation().getZ() : this.tpZ);
        if (position && wrapper.hasRotationChanged()) {
            final Iterator<Vector3d> iterator = this.teleports.iterator();
            if (iterator.hasNext()) {
                do {
                    final Vector3d wantedLocation = iterator.next();
                    if (wantedLocation.getX() == this.tpX && Math.abs(wantedLocation.getY() - this.tpY) < 1.0E-7 && wantedLocation.getZ() == this.tpZ) {
                        this.teleporting = true;
                        this.teleports.remove(wantedLocation);
                        break;
                    }
                } while (iterator.hasNext());
            }
        }


        boolean onGround = wrapper.isOnGround();
        this.lastClientOnGround = this.clientOnGround;
        this.clientOnGround = onGround;

        if (position) {
            this.lastX = this.x;
            this.lastY = this.y;
            this.lastZ = this.z;

            this.x = wrapper.getLocation().getX();
            this.y = wrapper.getLocation().getY();
            this.z = wrapper.getLocation().getZ();

            this.lastDeltaX = this.deltaX;
            this.lastDeltaY = this.deltaY;
            this.lastDeltaZ = this.deltaZ;
            this.lastDeltaXZ = this.deltaXZ;

            this.deltaX = this.x - this.lastX;
            this.deltaY = this.y - this.lastY;
            this.deltaZ = this.z - this.lastZ;

            this.deltaXZ = MathUtil.hypot(this.deltaX, this.deltaZ);

            this.lastMathematicallyOnGround = this.mathematicallyOnGround;
            this.mathematicallyOnGround = (this.y % 0.015625 == 0.0);

            this.jumpMotion = 0.42F + data.getPotionProcessor().getJumpBoostAmplifier() * 0.1F;
            this.jumped = Math.abs(deltaY - jumpMotion) < 0.001D;

            this.handleCollisions();
            this.handlePositionTicks();
        }

        this.lastMovementIncludedPosition = wrapper.hasPositionChanged();

        this.handleFlyingTicks();
    }

    public void handleServerPosition(final WrapperPlayServerPlayerPositionAndLook wrapper) {
        Vector3d pos = new Vector3d(wrapper.getX(), wrapper.getY(), wrapper.getZ());
        if (wrapper.isRelativeFlag(RelativeFlag.X)) {
            pos = pos.add(new Vector3d(this.getX(), 0, 0));
        }

        if (wrapper.isRelativeFlag(RelativeFlag.Y)) {
            pos = pos.add(new Vector3d(0, this.getY(), 0));
        }

        if (wrapper.isRelativeFlag(RelativeFlag.Z)) {
            pos = pos.add(new Vector3d(0, 0, this.getZ()));
        }

        wrapper.setX(pos.getX());
        wrapper.setY(pos.getY());
        wrapper.setZ(pos.getZ());
        wrapper.setRelativeMask((byte) (wrapper.getRelativeFlags().getMask() & 0b11000));
        // For some reason teleports on 1.7 servers are offset by 1.62?
        if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_8)) {
            pos = pos.withY(pos.getY() - 1.62);
        }
        this.teleports.add(pos);
    }

    public synchronized void handleCollisions() {
        this.world = this.data.getPlayer().getWorld();
        this.nearbyBlocks = BlockUtil.getNearbyBlocksAsync(new Location(this.world, this.x, this.y, this.z), 1);
        this.blocksAbove = PlayerUtil.getBlocksAbove(this.data);
        this.blocksSide = PlayerUtil.getBlocksSide(this.data);
        this.collidingVertically = !this.blocksAbove.stream().allMatch(BlockUtil::isAir);
        this.collidingHorizontally = !this.blocksSide.stream().allMatch(BlockUtil::isAir);
        if (this.nearbyBlocks != null) {
            final Supplier<Stream<Material>> supplier = (() -> this.nearbyBlocks.stream());
            this.inAir = supplier.get().allMatch(BlockUtil::isAir);
            this.nearLiquid = supplier.get().anyMatch(BlockUtil::isLiquid);
            this.nearTrapdoor = supplier.get().anyMatch(BlockUtil::isTrapdoor);
            this.nearSlime = supplier.get().anyMatch(BlockUtil::isSlime);
            this.nearStair = supplier.get().anyMatch(BlockUtil::isStair);
            this.nearSlab = supplier.get().anyMatch(BlockUtil::isSlab);
            this.nearIce = supplier.get().anyMatch(BlockUtil::isIce);
            this.nearWeb = supplier.get().anyMatch(BlockUtil::isWeb);
            this.nearVehicle = supplier.get().anyMatch(BlockUtil::isVehicle);
            this.inWeb = BlockUtil.isWeb(data);
        }
    }

    public boolean isTeleporting() {
        return this.teleporting;
    }

    public double getAcceleration() {
        return Math.abs(this.deltaXZ - this.lastDeltaXZ);
    }

    private void handleFlyingTicks() {
        if (this.clientOnGround) {
            ++this.clientGroundTicks;
        }
        else {
            this.clientGroundTicks = 0;
        }
        if (!this.clientOnGround) {
            ++this.clientAirTicks;
        }
        else {
            this.clientAirTicks = 0;
        }
        if (!this.jumped) {
            ++this.sinceJumpTicks;
        } else {
            sinceJumpTicks = 0;
        }

    }

    private void handlePositionTicks() {
        if (this.mathematicallyOnGround) {
            ++this.mathematicallyOnGroundTicks;
        }
        else {
            this.mathematicallyOnGroundTicks = 0;
        }
        if (this.data.getPlayer().getAllowFlight()) {
            this.sinceFlightTicks = 0;
        }
        else {
            ++this.sinceFlightTicks;
        }
        if (this.nearLiquid) {
            this.sinceNearLiquidTicks = 0;
        }
        else {
            ++this.sinceNearLiquidTicks;
        }
        if (this.nearSlime) {
            this.sinceNearSlimeTicks = 0;
        }
        else {
            ++this.sinceNearSlimeTicks;
        }
        if (this.collidingVertically) {
            this.sinceCollidingVerticallyTicks = 0;
        }
        else {
            ++this.sinceCollidingVerticallyTicks;
        }
        if (this.nearTrapdoor) {
            this.sinceNearTrapdoorTicks = 0;
        }
        else {
            ++this.sinceNearTrapdoorTicks;
        }
        if (this.nearIce) {
            this.sinceNearIceTicks = 0;
        }
        else {
            ++this.sinceNearIceTicks;
        }
        if (this.inAir) {
            ++this.serverAirTicks;
        }
        else {
            this.serverAirTicks = 0;
        }
        if (this.inWeb) {
            this.ticksSinceWeb = 0;
        } else {
            ++this.ticksSinceWeb;
        }
    }

    public PlayerData getData() {
        return this.data;
    }

}
