package com.elevatemc.anticheat.check.impl.player.entity;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.manager.PunishmentManager;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.server.BotUtils;
import com.elevatemc.anticheat.util.type.BotTypes;
import com.elevatemc.api.CheckInfo;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@CheckInfo(name = "Entity", type = "A", description = "Checks for lock aura")
public class InvalidAttack extends Check {
    PREDICTION prediction = PREDICTION.LEGIT;
    public InvalidAttack(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (WrapperPlayClientPlayerFlying.isFlying(packet.getPacketType()) && data.getEntityHelper().entityPlayer != null) {

            if ((System.currentTimeMillis() - data.getBotProcessor().lastEntityBotHit) > 500L) {
                if (data.getBotProcessor().getEntityHitTime() > 0) data.getBotProcessor().entityHitTime--;
            }

            if (data.getBotProcessor().getEntityHitTime() > 7 && (System.currentTimeMillis() - data.getBotProcessor().lastEntityBotHit) < 320L) {
                if (data.getBotProcessor().getForcedUser() == null) {
                    fail("t=" + data.getBotProcessor().getEntityHitTime());
                } else {
                    data.getBotProcessor().entityAReportedFlags++;
                }
            }

            long seconds = (System.currentTimeMillis() - data.getBotProcessor().lastEntitySpawn) / 1000;

            //boolean isFollowingBot = isFollowingBot(data.getPlayer(), data.getBotProcessor().botID);
            //debug(isFollowingBot);

            if (seconds > 5L) {
                if (data.getBotProcessor().getForcedUser() != null && data.getBotProcessor().getForcedUser().getPlayer().isOnline()) {
                    if (data.getBotProcessor().entityAReportedFlags > 3) {
                        prediction = PREDICTION.CHEATING;
                    } else if (data.getBotProcessor().getEntityAReportedFlags() > 5) {
                        prediction = PREDICTION.POSSIBLY;
                    } else {
                        prediction = PREDICTION.LEGIT;
                    }
                    data.getBotProcessor().getForcedUser().getPlayer().sendMessage(ChatColor.GRAY + " - " + (data.getBotProcessor().entityAReportedFlags > 3 ? ChatColor.RED + "Cheating" : (data.getBotProcessor().getEntityAReportedFlags() > 5 ? ChatColor.YELLOW + "Possibly Legit" : ChatColor.GREEN + "Legit")));
                }

                if (prediction == PREDICTION.CHEATING) {

                    fail("PREDICTION=" + prediction.name() + " ");
                }

                BotUtils.removeBotEntity(data);
                data.getBotProcessor().setForcedUser(null);
                data.getBotProcessor().entityATotalAttacks = 0;
                data.getBotProcessor().entityAReportedFlags = 0;
                data.getBotProcessor().setWaitingForBot(false);

                return;
            }
            Location playerLoc = new Location(data.getPlayer().getLocation().getWorld(), data.getPositionProcessor().getX(), data.getPositionProcessor().getY(), data.getPositionProcessor().getZ());
            Location loc = BotUtils.getBehind(data.getPlayer(), (data.getBotProcessor().moveBot ? (!(playerLoc.getPitch() < -21.00f) ? -0.10 : -2.90) : -2.90));

            if (data.getBotProcessor().getBotType() == BotTypes.WATCHDOG) loc = data.getPlayer().getLocation();

            boolean random = ThreadLocalRandom.current().nextBoolean();
            double offset = (random ? MathUtil.getRandomDouble(0.20, 0.55) : 0.0);
            boolean hit = ((System.currentTimeMillis() - data.getBotProcessor().lastEntityBotHit) < 122L);

            if (data.getBotProcessor().botTicks > 50) {
                if (!data.getBotProcessor().moveBot) {
                    data.getBotProcessor().moveBot = true;
                }
                data.getBotProcessor().botTicks = 0;
            }

            if (data.getBotProcessor().moveBot && data.getBotProcessor().movedBotTicks > 20) {
                data.getBotProcessor().movedBotTicks = 0;
                data.getBotProcessor().moveBot = false;
            }

            if (data.getBotProcessor().getBotType() == BotTypes.NORMAL) {
                Random r = new Random();
                int low = 0;
                int high = 20;
                int result = r.nextInt(high-low) + low;
                if(data.getRotationProcessor().getPitch() <= -80) {
                    data.getEntityHelper().entityPlayer.setLocation(loc.getX() + offset, loc.getY() - MathUtil.getRandomDouble(0.5f, 1f), loc.getZ() - offset, (float) (loc.getYaw() + MathUtil.getRandomDouble(0.10f, 0.50f)), (float) MathUtil.getRandomDouble(-90.0f, 90.0f));
                }
                else {
                    data.getEntityHelper().entityPlayer.setLocation(loc.getX() + offset, ((hit || data.getBotProcessor().moveBot) && !(playerLoc.getPitch() < -6.00f) ? loc.getY() + 3.42 : loc.getY() + (random && result < 15 ? MathUtil.getRandomDouble(0.10, 0.99) : 0.0)), loc.getZ() - offset, (float) (loc.getYaw() + MathUtil.getRandomDouble(0.10f, 0.50f)), (float) MathUtil.getRandomDouble(-90.0f, 90.0f));
                }
            } else if (data.getBotProcessor().getBotType() == BotTypes.WATCHDOG) {
                double increment = MathUtil.getRandomDouble(0.95, 1.40);
                data.getEntityHelper().entityPlayer.setLocation(loc.getX() + Math.sin(Math.toRadians(-(data.getBotProcessor().getEntityAStartYaw() + data.getBotProcessor().getEntityAMovementOffset()))) * increment, loc.getY() + 1 + (ThreadLocalRandom.current().nextBoolean() ? (ThreadLocalRandom.current().nextBoolean() ? MathUtil.getRandomDouble(0.35f, 0.42f) : 0.42f) : 0.0f), loc.getZ() + Math.cos(Math.toRadians(-(data.getBotProcessor().getEntityAStartYaw() + data.getBotProcessor().getEntityAMovementOffset()))) * increment, (float) (loc.getYaw() + MathUtil.getRandomDouble(0.10f, 0.50f)), (float) MathUtil.getRandomDouble(-90.0f, 90.0f));
            } else if (data.getBotProcessor().getBotType() == BotTypes.FOLLOW) {
                if (Math.abs(data.getBotProcessor().getEntityAFollowDistance()) > 1.20) {
                    data.getBotProcessor().setEntityAFollowDistance(data.getBotProcessor().getEntityAFollowDistance() + 0.05f);
                }

                double yaw = 0.0f, amount = -data.getBotProcessor().getEntityAFollowDistance();
                yaw = Math.toRadians(yaw);
                double dX = -Math.sin(yaw) * amount;
                double dZ = Math.cos(yaw) * amount;

                data.getEntityHelper().entityPlayer.setLocation(loc.getX() + dX, playerLoc.getY(), loc.getZ() + dZ, playerLoc.getYaw(), playerLoc.getPitch());
            }

            BotUtils.sendPacket(data, new PacketPlayOutEntityTeleport(data.getEntityHelper().entityPlayer), data.getBotProcessor().getForcedUser());

            if (!data.getBotProcessor().moveBot) data.getBotProcessor().botTicks++;
            else data.getBotProcessor().movedBotTicks++;
            data.getBotProcessor().randomBotSwingTicks++;
            data.getBotProcessor().randomBotDamageTicks++;
            data.getBotProcessor().setEntityAMovementOffset(data.getBotProcessor().getEntityAMovementOffset() + 20.0f);
        }

        if (packet.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrappedInUseEntityPacket = new WrapperPlayClientInteractEntity(packet);
            if (hitTicks() < 10 && wrappedInUseEntityPacket.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                if (SpigotReflectionUtil.getEntityById(wrappedInUseEntityPacket.getEntityId()) instanceof Player) {
                  // BotUtils.spawnBotEntity(data);
                }
            }

            if (data.getBotProcessor().hasBot && wrappedInUseEntityPacket.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK && wrappedInUseEntityPacket.getEntityId() == data.getBotProcessor().botID) {
                if (data.getBotProcessor().entityHitTime < 20) data.getBotProcessor().entityHitTime++;
                data.getBotProcessor().lastEntityBotHit = System.currentTimeMillis();
                data.getBotProcessor().entityATotalAttacks++;
            }
        }
    }

    private boolean isFollowingBot(Player suspect, int botId) {
        Entity entity = SpigotReflectionUtil.getEntityById(botId);

        if (entity != null && entity.getLocation() != null) {
            Location playerLocation = suspect.getLocation();
            Location targetLocation = entity.getLocation();

            // Calculate the direction vector from player to target
            Vector directionToTarget = targetLocation.toVector().subtract(playerLocation.toVector());

            // Calculate the angle between the player's direction and direction to target
            double angleToTarget = Math.toDegrees(Math.atan2(directionToTarget.getZ(), directionToTarget.getX()));

            // Normalize the angle to be between -180 and 180 degrees
            angleToTarget = MathUtil.clamp180(angleToTarget);

            // Get the player's yaw in degrees
            double playerYaw = playerLocation.getYaw();

            // Calculate the absolute difference between the two angles
            double angleDifference = Math.abs(MathUtil.clamp180(playerYaw - angleToTarget));

            // Check if the angle difference is within the acceptable range
            return angleDifference <= 30.0F;
        }
        return false;
    }

    enum PREDICTION {
        LEGIT,
        POSSIBLY,
        CHEATING
    }
}
