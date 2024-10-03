package com.elevatemc.anticheat.check.impl.movement.speed;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.util.math.MathHelper;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import org.bukkit.enchantments.Enchantment;

import java.math.BigDecimal;
import java.math.RoundingMode;

@CheckInfo(name = "Speed", type = "I", description = "Checks for movement prediction")
public class SpeedI extends Check {

    boolean lastLast;
    float playerF;

    public SpeedI(PlayerData data) {
        super(data);
    }


    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            boolean exempt = isExempt(ExemptType.JOINED, ExemptType.TELEPORT, ExemptType.DEPTH_STRIDER, ExemptType.FLIGHT, ExemptType.CLIMBABLE, ExemptType.DEAD, ExemptType.VEHICLE) || data.getPlayer().getWalkSpeed() != 0.2F;

            if (!exempt) {

                if (lastLast) {
                    playerF = 0.91F * 0.6F;

                    if (data.getPositionProcessor().getSinceNearIceTicks() <= 20) {
                        //debug(friction);
                        playerF =  0.91F * 0.98F;
                    }
                    if (isExempt(ExemptType.SLIME)) {
                        //debug(friction);
                        playerF = 0.91F * 0.8F;
                    }
                    if (data.getPositionProcessor().getSinceNearIceTicks() <= 20 && isExempt(ExemptType.SLIME)) {
                        //debug(friction);
                        playerF = (0.91F * 0.8F) * 0.98F;
                    }
                } else {
                    playerF = 0.91F;
                }

                double deltaXZ = data.getPositionProcessor().getDeltaXZ(), lastDeltaXZ = data.getPositionProcessor().getLastDeltaXZ();

                double predictedXZ = lastDeltaXZ * playerF;
                double deltaY = data.getPositionProcessor().getDeltaY();
                //debug(predictedXZ);

                boolean onGround = data.getPositionProcessor().isClientOnGround(), lastOnGround = data.getPositionProcessor().isLastClientOnGround();

                if (!onGround && lastOnGround && deltaY > 0.0) {
                    //debug("dY=" + deltaY);
                    //debug("p=" + predictedXZ);
                    predictedXZ += 0.2F;
                }

                if (hitTicks() < 4) {
                    //debug(predictedXZ);
                    predictedXZ += 0.0101f;
                }

                if (data.getVelocityProcessor().getTicksSinceVelocity() < 10) {
                    //debug(predictedXZ);
                    predictedXZ += 0.0101f;
                }

                predictedXZ += movingFlyingV3(data);

                double totalSpeed = deltaXZ - predictedXZ;

                if (onGround || lastOnGround) {
                    //debug("dXZ=" + deltaXZ);
                   // debug("S=" + totalSpeed);

                    if (deltaXZ > 0.37 && totalSpeed > 0.8) {
                        if (increaseBuffer() > 1) fail("deltaXZ=" + deltaXZ + " speed=" + totalSpeed);
                    } else {
                        resetBuffer();
                    }
                }
            }
        }
        lastLast = data.getPositionProcessor().isLastClientOnGround();
    }

    // Credits to FlyCode (Rhys)
    public double movingFlyingV3(PlayerData user) {

        double preD = 0.01D;

        double mx = user.getPositionProcessor().getDeltaX();
        double mz = user.getPositionProcessor().getDeltaZ();

        float motionYaw = (float) (Math.atan2(mz, mx) * 180.0D / Math.PI) - 90.0F;

        int direction;

        motionYaw -= user.getRotationProcessor().getYaw();

        while (motionYaw > 360.0F)
            motionYaw -= 360.0F;
        while (motionYaw < 0.0F)
            motionYaw += 360.0F;

        motionYaw /= 45.0F;

        float moveS = 0.0F;
        float moveF = 0.0F;

        if (Math.abs(Math.abs(mx) + Math.abs(mz)) > preD) {
            direction = (int) new BigDecimal(motionYaw).setScale(1, RoundingMode.HALF_UP).doubleValue();

            if (direction == 1) {
                moveF = 1F;
                moveS = -1F;


            } else if (direction == 2) {
                moveS = -1F;


            } else if (direction == 3) {
                moveF = -1F;
                moveS = -1F;


            } else if (direction == 4) {
                moveF = -1F;

            } else if (direction == 5) {
                moveF = -1F;
                moveS = 1F;

            } else if (direction == 6) {
                moveS = 1F;

            } else if (direction == 7) {
                moveF = 1F;
                moveS = 1F;

            } else if (direction == 8) {
                moveF = 1F;

            } else if (direction == 0) {
                moveF = 1F;
            }
        }

        moveS *= 0.98F;
        moveF *= 0.98F;

        float strafe = 1F, forward = 1F;
        float f = strafe * strafe + forward * forward;

        if (hitTicks() <= 4) {
            strafe *= 0.2F;
            forward *= 0.2F;
        }


        if (user.getActionProcessor().isSneaking() && !user.getActionProcessor().isSprinting()) {
            strafe *= 0.3F;
            forward *= 0.3F;
        }


        float friction;

        float var3 = (0.6F * 0.91F);
        float getAIMoveSpeed = 0.13000001F;


        if (user.getPotionProcessor().getSpeedBoostAmplifier()> 0) {
            switch (data.getPotionProcessor().getSpeedBoostAmplifier()) {
                case 0: {
                    getAIMoveSpeed = 0.23400002F;
                    break;
                }

                case 1: {
                    getAIMoveSpeed = 0.156F;
                    break;
                }

                case 2: {
                    getAIMoveSpeed = 0.18200001F;
                    break;
                }

                case 3: {
                    getAIMoveSpeed = 0.208F;
                    break;
                }

                case 4: {
                    getAIMoveSpeed = 0.23400001F;
                    break;
                }

            }
        }

        float var4 = 0.16277136F / (var3 * var3 * var3);

        if (data.getPositionProcessor().isClientOnGround()) {
            friction = getAIMoveSpeed * var4;
        } else {
            friction = 0.026F;
        }


        float f4 = 0.026F;
        float f5 = 0.8F;

        if (isExempt(ExemptType.LIQUID)) {


            if (user.getPlayer().getInventory().getBoots() != null
                    && user.getPlayer().getInventory().getBoots().getEnchantments() != null) {

                float f3 = user.getPlayer().getInventory().getBoots().getEnchantmentLevel(Enchantment.DEPTH_STRIDER);

                if (f3 > 3.0F) {
                    f3 = 3.0F;
                }

                if (!user.getPositionProcessor().isLastClientOnGround()) {
                    f3 *= 0.5F;
                }

                if (f3 > 0.0F) {
                    f5 += (0.54600006F - f5) * f3 / 3.0F;
                    f4 += (getAIMoveSpeed - f4) * f3 / 3.0F;
                }

                friction = f4;

                this.playerF = f5;
            }
        }


        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);
            if (f < 1.0F) {
                f = 1.0F;
            }
            f = friction / f;
            strafe = strafe * f;
            forward = forward * f;
            float f1 = MathHelper.sin(data.getRotationProcessor().getYaw() * (float) Math.PI / 180.0F);
            float f2 = MathHelper.cos(data.getRotationProcessor().getYaw() * (float) Math.PI / 180.0F);
            float motionXAdd = (strafe * f2 - forward * f1);
            float motionZAdd = (forward * f2 + strafe * f1);
            return Math.hypot(motionXAdd, motionZAdd);
        }

        this.playerF = f5;

        return 0;
    }
}
