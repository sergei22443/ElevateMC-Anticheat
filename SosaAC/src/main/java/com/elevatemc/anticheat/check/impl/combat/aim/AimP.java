package com.elevatemc.anticheat.check.impl.combat.aim;

import com.elevatemc.anticheat.check.Check;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.type.EvictingList;
import com.elevatemc.api.CheckInfo;
import com.elevatemc.anticheat.data.PlayerData;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

import java.util.List;

@CheckInfo(name = "Aim", type = "P", experimental = true, description = "Smooth symmetric-matrix rotation")
public class AimP extends Check {

    private EvictingList<Double> samples = new EvictingList<>(30);
    public AimP(PlayerData data) {
        super(data);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION ||
                event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {

            boolean valid = hitTicks() < 5 && !data.getRotationProcessor().isZooming();

            if (valid) {
                double deviation = getDeviation();
                // Add deviation to samples for further analysis
                samples.add(deviation);
                // Get the mean and stDev.
                double mean = calculateMean(samples);
                double stDev = calculateStandardDeviation(samples);
                // After debugging
                boolean smooth = mean > -.03 && mean < 0.0 && stDev < 0.7;

                if (samples.isFull()) {
                    if (smooth) {
                        if (increaseBuffer() > 6) {
                            fail("symmetric=" + stDev + " | mean=" + mean);
                        }
                    }
                    samples.clear();
                }
            }
        }
    }

    private double getDeviation() {
        float currentYaw = data.getRotationProcessor().getDeltaYaw();
        float currentPitch = data.getRotationProcessor().getDeltaPitch();

        float previousYaw = data.getRotationProcessor().getLastDeltaYaw(); // Get the previous yaw angle
        float previousPitch = data.getRotationProcessor().getLastDeltaPitch(); // Get the previous pitch angle

        float yawDerivation = currentYaw - previousYaw;
        float pitchDerivation = currentPitch - previousPitch;

        // Clamp!
        double clampedYaw = MathUtil.clamp180(yawDerivation);
        double clampedPitch = MathUtil.clamp180(pitchDerivation);

        // Calculate symmetric deviation
        return (clampedYaw + clampedPitch) / 2;
    }

    private double calculateMean(List<Double> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Input list is null or empty");
        }

        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }

        return sum / values.size();
    }

    private double calculateStandardDeviation(List<Double> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Input list is null or empty");
        }

        double mean = calculateMean(values);
        double sumSquaredDeviations = 0.0;

        for (double value : values) {
            double deviation = value - mean;
            sumSquaredDeviations += deviation * deviation;
        }

        double variance = sumSquaredDeviations / values.size();
        return Math.sqrt(variance);
    }

}
