package com.elevatemc.anticheat.check.impl.combat.killaura;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.util.math.MathHelper;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.util.Vector;


@CheckInfo(name = "Kill Aura", type = "J", description = "Checks for invalid motion whilst attacking")
public class KillAuraJ extends Check {

    float moveForward, moveStrafe;

    public KillAuraJ(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            if (hitTicks() < 4) {

                boolean validMotion = Math.abs(data.getPositionProcessor().getJumpMotion() - data.getPositionProcessor().getDeltaY()) < 1.0E-5;
                boolean exempt = isExempt(ExemptType.COLLIDING_HORIZONTALLY, ExemptType.TELEPORT);

                if (data.getActionProcessor().isSprinting() && validMotion && !exempt) {
                    float yaw = data.getRotationProcessor().getYaw();

                    float angleOne = (float) Math.sin(yaw * (float) Math.PI / 180.0F);
                    float angleTwo = (float) Math.cos(yaw * (float) Math.PI / 180.0F);

                    double deltaX = data.getPositionProcessor().getDeltaX(), deltaZ = data.getPositionProcessor().getDeltaZ();

                    boolean knockback = data.getPlayer().getItemInHand().containsEnchantment(Enchantment.KNOCKBACK);

                    if (!knockback && hitTicks() <= 1) {
                        deltaX *= 0.6;
                        deltaZ *= 0.6;
                    }

                    Vector motion = new Vector(deltaX, 0.0, deltaZ);

                    double acceleration = data.getPositionProcessor().getAcceleration();

                    moveHeading(angleOne, angleTwo, motion);
                    moveFlying(motion, (float) acceleration, angleOne, angleTwo, moveForward, moveStrafe);

                    double deltaXZ = MathUtil.hypot(deltaX, deltaZ);

                    if (deltaXZ > 0.001) {
                        if (Math.abs(deltaXZ - 0.2) < 0.001 * 2.0) {
                            fail("dXZ=" + deltaXZ);
                        }
                    }
                }
            }
        }
    }

    public void moveHeading(float motionX, float motionZ, Vector motion) {
        motion.setX(motion.getX() - (double)(motionX * 0.2F));
        motion.setZ(motion.getZ() + (double)(motionZ * 0.2F));
    }

    private void moveFlying(Vector motion, float friction, float moveX, float moveZ, float moveForward, float moveStrafe) {
        float f4 = moveStrafe * moveStrafe + moveForward * moveForward;
        if (f4 >= 1.0E-4F) {
            f4 =  MathHelper.sqrt_float(f4);
            f4 = friction / Math.max(1.0F, f4);
            moveStrafe *= f4;
            moveForward *= f4;
            motion.setX(motion.getX() + (double) (moveStrafe * moveZ - moveForward * moveX));
            motion.setZ(motion.getZ() + (double) (moveForward * moveZ + moveStrafe * moveX));
        }
    }
}
