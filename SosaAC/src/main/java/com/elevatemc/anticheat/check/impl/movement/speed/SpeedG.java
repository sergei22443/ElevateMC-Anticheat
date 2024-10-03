package com.elevatemc.anticheat.check.impl.movement.speed;
import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckInfo(name = "Speed", type = "G", description = "Checks if player is going faster than possible")
public class SpeedG extends Check {

    public SpeedG(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) {
            boolean exempt = isExempt(ExemptType.TELEPORT, ExemptType.JOINED, ExemptType.TPS,
                    ExemptType.FLIGHT, ExemptType.CREATIVE, ExemptType.DEAD, ExemptType.LAG_SPIKE,
                    ExemptType.DEPTH_STRIDER, ExemptType.LIQUID, ExemptType.CLIMBABLE);

            boolean exemptedDeltas =  (data.getPositionProcessor().getDeltaY() !=  0.07840000152587834)
                    || data.getPositionProcessor().getDeltaY() !=  data.getPositionProcessor().getJumpMotion();

            if (!exempt && !exemptedDeltas) {
                double deltaXZ = data.getPositionProcessor().getDeltaXZ();
                double lastDeltaXZ = data.getPositionProcessor().getLastDeltaXZ();

                boolean touchingAir = !data.getPositionProcessor().isClientOnGround() && !data.getPositionProcessor().isLastClientOnGround();
                if (touchingAir && deltaXZ > 0.05) {

                    double attributeSpeed = lastDeltaXZ * 0.9100000262260437 + 0.02;
                    final boolean sprinting = data.getActionProcessor().isSprinting();

                    if (sprinting) {
                        attributeSpeed += 0.0063;
                    }
                    double maxAttributeSpeed = 0.31;
                    if (data.getPotionProcessor().getSpeedBoostAmplifier() > 0) {
                        maxAttributeSpeed += data.getPotionProcessor().getSpeedBoostAmplifier() * 0.025;
                    }

                    if (deltaXZ - attributeSpeed > 1.0E-12 && deltaXZ > 0.1 && attributeSpeed > maxAttributeSpeed) {
                        if (increaseBuffer() > 3.0) {
                            increaseVlBy(.45);
                            fail("speed=" + attributeSpeed + " deltaXZ=" + deltaXZ);
                            staffAlert();
                            multiplyBuffer(.5);
                        }
                    } else {
                        decreaseBufferBy(.5);
                    }
                }
            }
        }
    }
}

