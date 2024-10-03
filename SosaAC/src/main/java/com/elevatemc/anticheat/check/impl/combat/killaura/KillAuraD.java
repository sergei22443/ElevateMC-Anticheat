package com.elevatemc.anticheat.check.impl.combat.killaura;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckInfo(name = "Kill Aura", type = "D", description = "Checks for divisor in the rotation")
public class KillAuraD extends Check {

    public KillAuraD(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION || event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            double deltaYaw = data.getRotationProcessor().getDeltaYaw();
            double deltaPitch = data.getRotationProcessor().getDeltaPitch();
            double lastDeltaPitch = data.getRotationProcessor().getDeltaPitch();

            double divisorPitch = (double)MathUtil.getGcd((long)(deltaPitch * MathUtil.EXPANDER), (long)(lastDeltaPitch * MathUtil.EXPANDER));
            double constantPitch = divisorPitch / MathUtil.EXPANDER;

            double currentY = deltaPitch / constantPitch;
            double previousY = lastDeltaPitch / constantPitch;

            if (deltaYaw > 0.0F && deltaPitch > 0.0F && deltaYaw < 20.0F && deltaPitch < 20.0F) {
                double moduloY = currentY % previousY;
                double floorModuloY = Math.abs(Math.floor(moduloY) - moduloY);
                if (Double.isNaN(floorModuloY) || floorModuloY != 0.0) {
                    if (increaseBuffer() > 4.0) {
                        multiplyBuffer(.5);
                        fail("fMY=" + floorModuloY);
                    }
                } else {
                    decreaseBufferBy(.25);
                }
            }
        }
    }
}
