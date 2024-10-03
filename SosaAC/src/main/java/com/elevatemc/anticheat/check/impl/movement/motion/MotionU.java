package com.elevatemc.anticheat.check.impl.movement.motion;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.server.block.BlockUtil;
import com.elevatemc.api.CheckInfo;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

@CheckInfo(name = "Motion", type = "U", experimental = true, description = "Checks for liquidspeed")
public class MotionU extends Check {

    public MotionU(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent packet) {
        if (WrapperPlayClientPlayerFlying.isFlying(packet.getPacketType())) {
            boolean inLiquid = BlockUtil.isLiquid(data.getPlayer().getLocation().getBlock().getType());
            boolean onGround = data.getPositionProcessor().isClientOnGround();

            boolean sprinting = data.getActionProcessor().isSprinting();

            double deltaX = data.getPositionProcessor().getDeltaX();
            double deltaZ = data.getPositionProcessor().getDeltaZ();

            double lastDeltaX = data.getPositionProcessor().getLastDeltaX();
            double lastDeltaZ = data.getPositionProcessor().getLastDeltaZ();

            ItemStack boots = data.getPlayer().getInventory().getBoots();

            float f1 = 0.8F;
            float f3;

            if (isExempt(ExemptType.DEPTH_STRIDER)) f3 = boots.getEnchantmentLevel(Enchantment.DEPTH_STRIDER);
            else f3 = 0.0F;

            if (f3 > 3.0F) f3 = 3.0F;
            if (!onGround) f3 *= 0.5F;
            if (f3 > 0.0F) f1 += (0.54600006F - f1) * f3 / 3.0F;

            double predictedX = lastDeltaX * f1 + (sprinting ? 0.0263 : 0.02);
            double predictedZ = lastDeltaZ * f1 + (sprinting ? 0.0263 : 0.02);

            double differenceX = deltaX - predictedX;
            double differenceZ = deltaZ - predictedZ;

            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.VEHICLE, ExemptType.FLIGHT,
                    ExemptType.TRAPDOOR, ExemptType.CLIMBABLE, ExemptType.WEB,
                    ExemptType.SLIME, ExemptType.VEHICLE, ExemptType.CHUNK);
            boolean invalid = (differenceX > 0.05 && differenceZ > 0.0 || differenceZ > 0.05 && differenceX > 0.0) && inLiquid;

            if (invalid && !exempt) {
                if (increaseBuffer() > 6) {
                    fail("X-Pred=" + differenceX + " Z-Pred=" + differenceZ);
                }
            } else {
                decreaseBufferBy(0.25);
            }
        }
    }

}
