package com.elevatemc.anticheat.check.impl.movement.speed;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.util.server.PlayerUtil;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.anticheat.util.server.block.BlockUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Speed", type = "C" ,description = "Checks if a player is going over speed limit")
public class SpeedC extends Check {

    public SpeedC(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            boolean exempt = isExempt(ExemptType.JOINED,
                    ExemptType.TELEPORT,
                    ExemptType.FAST,
                    ExemptType.CREATIVE,
                    ExemptType.FLIGHT,
                    ExemptType.CLIMBABLE,
                    ExemptType.ICE)
                    || data.getPlayer().isInsideVehicle()
                    || BlockUtil.isSlime(data);

            if (exempt) return;
            boolean underBlock = isExempt(ExemptType.COLLIDING_VERTICALLY);
            boolean velocity = data.getVelocityProcessor().getTicksSinceVelocity() < 20;

            double deltaXZ = data.getPositionProcessor().getDeltaXZ();
            double lastDeltaXZ = data.getPositionProcessor().getLastDeltaXZ();

            double speedLimit = PlayerUtil.getBaseSpeed(data.getPlayer());

            int airTicks = data.getPositionProcessor().getClientAirTicks();

            if (underBlock) {
                speedLimit += 0.91;
            }

            if (velocity) {
                speedLimit += data.getVelocityProcessor().getVelocityXZ() + 0.15;
            }

            if (BlockUtil.isAir(data.getPlayer().getLocation().getBlock().getType())) {
                speedLimit += 0.91F;
            }

            if (isExempt(ExemptType.LIQUID)) {
                //double difference = Math.abs(deltaXZ - lastDeltaXZ);
                //debug(difference);
                speedLimit += 0.01;
            }

            if (airTicks > 2 && deltaXZ > speedLimit) {
                if (increaseBuffer() > 6.0) {
                    fail("dXZ=" + deltaXZ);
                    increaseVlBy(.45);
                    staffAlert();
                    multiplyBuffer(.75);
                }
            } else {
             decreaseBufferBy(.25);
            }
        }
    }
}
