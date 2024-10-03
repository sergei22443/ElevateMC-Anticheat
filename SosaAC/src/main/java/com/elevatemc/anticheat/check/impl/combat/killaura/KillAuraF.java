package com.elevatemc.anticheat.check.impl.combat.killaura;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.processor.player.validity.type.ExemptType;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckInfo(name = "Kill Aura", type = "F", experimental = true, description = "Checks for invalid mouse axis changes")
public class KillAuraF extends Check {
    public KillAuraF(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (hitTicks() < 5 && !data.getRotationProcessor().isZooming()) {

            boolean exempt = isExempt(ExemptType.JOINED, ExemptType.TELEPORT);

            if (exempt) return;

            float deltaYaw = data.getRotationProcessor().getDeltaYaw(), deltaPitch = data.getRotationProcessor().getDeltaPitch();
            float lastDeltaYaw = data.getRotationProcessor().getLastDeltaYaw(), lastDeltaPitch = data.getRotationProcessor().getLastDeltaPitch();

            if (deltaYaw < 1.0F && deltaPitch < 1.0F) return;

            // get yaw and pitch gcd.
            double divisorX = MathUtil.getGcd(deltaYaw, lastDeltaYaw);
            double divisorY = MathUtil.getGcd(deltaPitch, lastDeltaPitch);
            // yes.
            double maxDivisor = Math.max(divisorX, divisorY);
            // get the deltaX and deltaY
            double x = deltaYaw / maxDivisor;
            double y = deltaPitch / maxDivisor;

            double delta = Math.abs(Math.floor(x) - x);
            double rt = Math.abs(Math.floor(y) - y);

            // 131072 / 16777216 = 0.0078125F.
            boolean invalidDivisor = maxDivisor < 0.0078125F;
            boolean invalidDeltas = MathUtil.isScientificNotation(rt) && delta > 0.2;

            if (invalidDivisor && invalidDeltas) {
                if (increaseBuffer() > 3) fail("divisor=" + maxDivisor + " delta=[" + rt + ", " + delta + "]" );
            } else {
                decreaseBufferBy(0.1D);
            }
        }
    }
}
