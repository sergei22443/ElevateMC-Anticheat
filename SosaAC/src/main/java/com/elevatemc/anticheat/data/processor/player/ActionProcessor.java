package com.elevatemc.anticheat.data.processor.player;

import com.elevatemc.anticheat.Sosa;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerRespawn;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ActionProcessor
{
    private final PlayerData data;
    private boolean sprinting,inventoryOpen, sneaking, sendingAction, placing, digging, blocking, sendingDig, teleporting, eating;
    private int lastItemDrop, sprintingTicks, lastBukkitBlockPlace, lastBukkitDig, lastBukkitRightClickBlock, lastDig, lastBlockBreak,
            lastWindowClick, lastBukkitTeleport, sinceTeleportTicks, lastServerPosition, lastPotionExpire, lastInventoryOpen, startConsume, lastWorldChange;

    private double lastAbortX, lastAbortZ, distanceFromLastAbort;

    public ActionProcessor(final PlayerData data) {
        this.data = data;
    }

    public void handleEntityAction(final WrapperPlayClientEntityAction wrapper) {
        this.sendingAction = true;
        switch (wrapper.getAction()) {
            case START_SNEAKING: {
                this.sneaking = true;
                break;
            }
            case STOP_SNEAKING: {
                this.sneaking = false;
                break;
            }
            case START_SPRINTING: {
                this.sprinting = true;
                break;
            }
            case STOP_SPRINTING: {
                this.sprinting = false;
                break;
            }
        }
    }

    public void handleClientCommand(final WrapperPlayClientClientStatus wrapper) {
        if (wrapper.getAction() == WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT) {
            this.inventoryOpen = true;
           // Bukkit.broadcastMessage("opened inv | ClientStatus");
        }
    }

    public void handleCloseWindow() {
        this.inventoryOpen = false;
    }

    public void handleBlockDig(final WrapperPlayClientPlayerDigging wrapper) {
        this.sendingDig = true;
        switch (wrapper.getAction()) {
            case START_DIGGING: {
                this.digging = true;
                break;
            }
            case FINISHED_DIGGING: {
                this.digging = false;
                break;
            }
            case CANCELLED_DIGGING: {
                this.lastBlockBreak = Sosa.INSTANCE.getTickManager().getTicks();
                this.lastAbortX = wrapper.getBlockPosition().getX();
                this.lastAbortZ = wrapper.getBlockPosition().getZ();
                this.digging = false;
                break;
            }
            case RELEASE_USE_ITEM: {
                this.blocking = false;
                this.eating = false;
                break;
            }
            case DROP_ITEM:
            case DROP_ITEM_STACK: {
                this.lastItemDrop = Sosa.INSTANCE.getTickManager().getTicks();
                break;
            }
        }
    }

    public void handleBlockPlace() {
        placing = true;
        eating = true;
        blocking = true;
        startConsume = Sosa.INSTANCE.getTickManager().getTicks();
    }

    public void handleFlying() {
        this.blocking = false;
        this.sendingDig = false;
        this.sendingAction = false;
        this.placing = false;
        ++this.sinceTeleportTicks;
        if (this.sprinting) {
            ++this.sprintingTicks;
        }
        else {
            this.sprintingTicks = 0;
        }
        if (inventoryOpen) {
            lastInventoryOpen = 0;
        } else {
            ++lastInventoryOpen;
        }
    }

    public void handleBukkitTeleport() {
        this.lastBukkitTeleport = Sosa.INSTANCE.getTickManager().getTicks();
    }

    public void handleArmAnimation() {
        if (this.digging) {
            this.lastDig = Sosa.INSTANCE.getTickManager().getTicks();
        }
        else if (this.lastAbortX != 0.0 && this.lastAbortZ != 0.0) {
            final double locationX = data.getPositionProcessor().getX();
            final double locationZ = data.getPositionProcessor().getZ();
            this.distanceFromLastAbort = MathUtil.hypot(Math.abs(locationX - this.lastAbortX), Math.abs(locationZ - this.lastAbortZ));
            if (this.distanceFromLastAbort > 10.0) {
                final double lastAbortX = 0.0;
                this.distanceFromLastAbort = lastAbortX;
                this.lastAbortZ = lastAbortX;
                this.lastAbortX = lastAbortX;
            }
        }
    }

    public void handleBukkitBlockPlace() {
        this.lastBukkitBlockPlace = Sosa.INSTANCE.getTickManager().getTicks();
    }

    public void handleBukkitDig() {
        this.lastBukkitDig = Sosa.INSTANCE.getTickManager().getTicks();
    }

    public void handleBukkitRightClickBlock() {
        this.lastBukkitRightClickBlock = Sosa.INSTANCE.getTickManager().getTicks();
    }

    public void handleWindowClick() {
        this.lastWindowClick = Sosa.INSTANCE.getTickManager().getTicks();
    }

    public void handleTeleport(final WrapperPlayServerPlayerPositionAndLook wrapper) {
        final Vector teleport = new Vector(wrapper.getX(), wrapper.getY(), wrapper.getZ());
        this.lastServerPosition = Sosa.INSTANCE.getTickManager().getTicks();
        this.sinceTeleportTicks = 0;
    }

    public void handlePotionExpire() {
        this.lastPotionExpire = Sosa.INSTANCE.getTickManager().getTicks();
    }

    public void handleInventoryOpen() {
        this.inventoryOpen = true;
        //Bukkit.broadcastMessage("Opened inventory");
    }

    public PlayerData getData() {
        return this.data;
    }

    public boolean isSprinting() {
        return this.sprinting;
    }

    public boolean isSneaking() {
        return this.sneaking;
    }

    public boolean isSendingAction() {
        return this.sendingAction;
    }

    public boolean isPlacing() {
        return this.placing;
    }

    public boolean isDigging() {
        return this.digging;
    }

    public boolean isBlocking() {
        return this.blocking;
    }

    public boolean isSendingDig() {
        return this.sendingDig;
    }

    public boolean isTeleporting() {
        return this.teleporting;
    }

    public int getLastItemDrop() {
        return this.lastItemDrop;
    }

    public int getSprintingTicks() {
        return this.sprintingTicks;
    }

    public int getLastBukkitBlockPlace() {
        return this.lastBukkitBlockPlace;
    }

    public int getLastBukkitDig() {
        return this.lastBukkitDig;
    }

    public int getLastBukkitRightClickBlock() {
        return this.lastBukkitRightClickBlock;
    }

    public int getLastDig() {
        return this.lastDig;
    }

    public int getLastBlockBreak() {
        return this.lastBlockBreak;
    }

    public int getLastWindowClick() {
        return this.lastWindowClick;
    }

    public int getLastBukkitTeleport() {
        return this.lastBukkitTeleport;
    }

    public int getSinceTeleportTicks() {
        return this.sinceTeleportTicks;
    }

    public int getLastServerPosition() {
        return this.lastServerPosition;
    }


    public int getLastPotionExpire() {
        return this.lastPotionExpire;
    }

    public double getLastAbortX() {
        return this.lastAbortX;
    }

    public double getLastAbortZ() {
        return this.lastAbortZ;
    }

    public double getDistanceFromLastAbort() {
        return this.distanceFromLastAbort;
    }

}
