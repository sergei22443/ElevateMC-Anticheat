package com.elevatemc.anticheat.data.processor.rotation;

import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import com.elevatemc.anticheat.util.values.Values;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SensitivityProcessor {

    private final PlayerData data;

    private float rotationYaw;
    private float rotationPitch;
    private float lastRotationYaw;
    private float lastRotationPitch;
    private float deltaPitch;
    private float lastDeltaYaw;
    private float lastDeltaPitch;

    public SensitivityProcessor(final PlayerData data) {
        this.data = data;
    }

    public void handle(float yaw, float pitch) {
        this.rotationYaw = yaw;
        this.rotationPitch = pitch;
        boolean isGayPitch = Math.abs(this.rotationPitch) > 85.0f;

        if (isGayPitch) {
            return;
        }

        float deltaYaw1 = Math.abs(this.rotationYaw - this.lastRotationYaw);
        this.deltaPitch = Math.abs(this.rotationPitch - this.lastRotationPitch);

        float differenceYaw = Math.abs(deltaYaw1 - this.lastDeltaYaw);
        float differencePitch = Math.abs(this.deltaPitch - this.lastDeltaPitch);
        float joltX = Math.abs(deltaYaw1 - differenceYaw);
        float joltY = Math.abs(this.deltaPitch - differencePitch);

        if (joltX > 1.0 && joltY > 1.0) {
            data.getSensitivityHolder().rate = System.currentTimeMillis();
        }

        double divisorYaw = getDivisor(deltaYaw1, this.lastDeltaYaw);
        double divisorPitch = getDivisor(this.deltaPitch, this.lastDeltaPitch);

        double computed = getDivisor((float)divisorYaw, (float)divisorPitch);
        computed = Math.max(computed, divisorYaw);
        computed = Math.max(computed, divisorPitch);

        if (divisorYaw > 1.0E-12 && divisorPitch > 1.0E-12 && divisorYaw < 2.0 && divisorPitch < 2.0) {
            data.getSensitivityHolder().currentDivisorYaw = divisorYaw;
            data.getSensitivityHolder().currentDivisorPitch = divisorPitch;
            data.getSensitivityHolder().currentDivisorComputed = computed;
            data.getSensitivityHolder().gridYaw[data.getSensitivityHolder().rotations % data.getSensitivityHolder().gridYaw.length] = divisorYaw;
            data.getSensitivityHolder().gridPitch[data.getSensitivityHolder().rotations % data.getSensitivityHolder().gridPitch.length] = divisorPitch;
            data.getSensitivityHolder().gridComputed[data.getSensitivityHolder().rotations % data.getSensitivityHolder().gridComputed.length] = computed;
            SensitivityHolder sensitivity = data.getSensitivityHolder();
            ++sensitivity.rotations;
        }
        if (data.getSensitivityHolder().gridYaw.length != 0.0 || data.getSensitivityHolder().gridPitch.length != 0.0) {
            boolean filledYaw = data.getSensitivityHolder().rotations > data.getSensitivityHolder().gridYaw.length;
            boolean filledPitch = data.getSensitivityHolder().rotations > data.getSensitivityHolder().gridPitch.length;
            if (filledYaw && filledPitch) {
                double duplicateYaw = MathUtil.getMode(data.getSensitivityHolder().gridYaw);
                double duplicatePitch = MathUtil.getMode(data.getSensitivityHolder().gridPitch);
                double duplicateComputed = MathUtil.getMode(data.getSensitivityHolder().gridComputed);
                data.getSensitivityHolder().modeYaw = duplicateYaw;
                data.getSensitivityHolder().modePitch = duplicatePitch;
                data.getSensitivityHolder().modeComputed = duplicateComputed;
                data.getSensitivityHolder().gridYaw = new double[data.getSensitivityHolder().gridYaw.length];
                data.getSensitivityHolder().gridPitch = new double[data.getSensitivityHolder().gridPitch.length];
                data.getSensitivityHolder().gridComputed = new double[data.getSensitivityHolder().gridComputed.length];
                data.getSensitivityHolder().rotations = 0;
            }
        }

        if (deltaYaw1 >= 0.1f && this.deltaPitch >= 0.1f && deltaYaw1 <= 20.0f) {
            if (this.deltaPitch <= 20.0f) {
                this.processIntegerSensitivity();
            }
        }

        if (data.getSensitivityHolder().modeYaw != Double.MIN_VALUE) {
            if (data.getSensitivityHolder().modePitch != Double.MIN_VALUE) {
                data.getSensitivityHolder().sensitivityX = this.getSensitivity(data.getSensitivityHolder().modeYaw);
                data.getSensitivityHolder().sensitivityY = getSensitivity(data.getSensitivityHolder().modePitch);
                data.getSensitivityHolder().sensitivity = getSensitivity(data.getSensitivityHolder().modeComputed);
                data.getSensitivityHolder().sensitivityXY = Math.abs(data.getSensitivityHolder().sensitivityX - data.getSensitivityHolder().sensitivityY);
                data.getSensitivityHolder().formatX = getGrid(data.getSensitivityHolder().gridYaw);
                data.getSensitivityHolder().formatY = getGrid(data.getSensitivityHolder().gridPitch);
                data.getSensitivityHolder().lastDeltaX = data.getSensitivityHolder().deltaX;
                data.getSensitivityHolder().lastDeltaY = data.getSensitivityHolder().deltaY;
                data.getSensitivityHolder().deltaX = deltaYaw1 / data.getSensitivityHolder().modeYaw;
                data.getSensitivityHolder().deltaY = deltaPitch / data.getSensitivityHolder().modePitch;

                float currentX = this.toRegularCircle(this.lastRotationYaw);
                float targetX = this.toRegularCircle(this.rotationYaw);
                float distanceX = MathUtil.distanceBetweenAngles(this.lastRotationYaw, this.rotationYaw);

                double polarX = MathUtil.distanceBetweenAngles(currentX + distanceX, targetX);
                double polarX2 = MathUtil.distanceBetweenAngles(currentX - distanceX, targetX);

                int invertX = (polarX < polarX2) ? 1 : -1;
                int invertY = (this.rotationPitch - this.lastRotationPitch > 0.0f) ? 1 : -1;

                data.getSensitivityHolder().inverseYaw = invertX;
                data.getSensitivityHolder().inversePitch = invertY;
                data.getSensitivityHolder().lastPredictedYaw = data.getSensitivityHolder().predictedYaw;
                data.getSensitivityHolder().lastPredictedPitch = data.getSensitivityHolder().predictedPitch;
                data.getSensitivityHolder().predictedYaw = this.getPredictedYaw(data.getSensitivityHolder().deltaX, data.getSensitivityHolder().sensitivityX);
                data.getSensitivityHolder().predictedPitch = this.getPredictedPitch(data.getSensitivityHolder().deltaY, data.getSensitivityHolder().sensitivityY);
                data.getSensitivityHolder().minimumYaw = this.getMinimumRotation(data.getSensitivityHolder().sensitivityX, this.lastRotationYaw, (float)data.getSensitivityHolder().inverseYaw);
                data.getSensitivityHolder().minimumPitch = this.getMinimumRotation(data.getSensitivityHolder().sensitivityY, this.lastRotationPitch, (float)data.getSensitivityHolder().inversePitch);
                data.getSensitivityHolder().computedX = this.getSensitivity(data.getSensitivityHolder().currentDivisorYaw);
                data.getSensitivityHolder().computedY = this.getSensitivity(data.getSensitivityHolder().currentDivisorPitch);
                data.getSensitivityHolder().distanceYaw = (((int)data.getSensitivityHolder().deltaX > 1) ? Math.abs(this.rotationYaw - data.getSensitivityHolder().predictedYaw) : 0.0f);
                data.getSensitivityHolder().distancePitch = (((int)data.getSensitivityHolder().deltaY > 1) ? Math.abs(this.rotationPitch - data.getSensitivityHolder().predictedPitch) : 0.0f);
                data.getSensitivityHolder().enclosesYaw = this.encloses(this.lastRotationYaw, data.getSensitivityHolder().predictedYaw, this.ceil(this.rotationYaw));
                data.getSensitivityHolder().enclosesPitch = this.encloses(this.lastRotationPitch, data.getSensitivityHolder().predictedPitch, this.ceil(this.rotationPitch));


                SensitivityHolder sensitivity2 = data.getSensitivityHolder();
                sensitivity2.smoothCamYaw += this.getProbableYaw(data.getSensitivityHolder().deltaX, data.getSensitivityHolder().sensitivityX);

                SensitivityHolder sensitivity3 = data.getSensitivityHolder();
                sensitivity3.smoothCamPitch += this.getProbablePitch(data.getSensitivityHolder().deltaY, data.getSensitivityHolder().sensitivityY);

                data.getSensitivityHolder().cinematicYaw = this.lastRotationYaw + data.getSensitivityHolder().smoothCamFilterX * data.getSensitivityHolder().inverseYaw * 0.15f;
                data.getSensitivityHolder().cinematicPitch = this.lastRotationPitch + data.getSensitivityHolder().smoothCamFilterY * data.getSensitivityHolder().inversePitch * 0.15f;
                data.getSensitivityHolder().smoothCamYaw = 0.0f;
                data.getSensitivityHolder().smoothCamPitch = 0.0f;
                data.getSensitivityHolder().deltaDifferenceX = Math.abs(data.getSensitivityHolder().deltaX - Math.round(data.getSensitivityHolder().deltaX));
                data.getSensitivityHolder().deltaDifferenceY = Math.abs(data.getSensitivityHolder().deltaY - Math.round(data.getSensitivityHolder().deltaY));

                double derivationX = data.getSensitivityHolder().getDerivation(differenceYaw, data.getSensitivityHolder().deltaDifferenceX);
                double derivationY = data.getSensitivityHolder().getDerivation(differencePitch, data.getSensitivityHolder().deltaDifferenceY);
                data.getSensitivityHolder().derivationX = derivationX;
                data.getSensitivityHolder().derivationY = derivationY;

                float deltaYaw = MathUtil.distanceBetweenAngles(this.rotationYaw, data.getSensitivityHolder().predictedYaw);
                float deltaPitch = Math.abs(this.rotationPitch - data.getSensitivityHolder().predictedPitch);
                data.getSensitivityHolder().differenceX = deltaYaw;
                data.getSensitivityHolder().differenceY = deltaPitch;
            }
        }
        this.lastRotationYaw = this.rotationYaw;
        this.lastRotationPitch = this.rotationPitch;
        this.lastDeltaYaw = deltaYaw1;
        this.lastDeltaPitch = this.deltaPitch;
    }

    private void processIntegerSensitivity() {
        float gcd = (float)getGcd(this.deltaPitch, this.lastDeltaPitch);

        int product = (int)(this.getSensitivity(gcd) * 200.0);
        data.getSensitivityHolder().integerSensitivitySamples.add(product);

        if (data.getSensitivityHolder().integerSensitivitySamples.size() == 40) {
            data.getSensitivityHolder().integerSensitivity = MathUtil.getMode(data.getSensitivityHolder().integerSensitivitySamples);

            int sensitivity = this.data.getSensitivityHolder().integerSensitivity;
            if (sensitivity > 0 && sensitivity < 200) {
                data.getSensitivityHolder().mcpSensitivity = Values.SENSITIVITY_MCP_VALUES.get(sensitivity);
            }
            data.getSensitivityHolder().integerSensitivitySamples.clear();
        }
    }

    private double getGcd(final double a, final double b) {
        if (a < b) {
            return this.getGcd(b, a);
        }
        if (Math.abs(b) < 0.001) {
            return a;
        }
        return this.getGcd(b, a - Math.floor(a / b) * b);
    }

    public double getDivisor(final float a, final float b) {
        double bits = MathUtil.EXPANDER;

        long formatX = (long)(a * bits);
        long formatY = (long)(b * bits);

        double divisor = MathUtil.getGcd(formatX, formatY) / bits;
        return MathUtil.round(divisor, 5);
    }

    public double getSensitivity(final double gcd) {
        double constructed = (float)(gcd / 0.15) / 8.0f;
        double product = Math.cbrt(constructed) - 0.2f;
        return product / 0.6f;
    }

    public double getGrid(final double[] entry) {
        double average = 0.0;
        double min = 0.0;
        double max = 0.0;
        for (final double number : entry) {
            if (number < min) {
                min = number;
            }
            if (number > max) {
                max = number;
            }
            average += number;
        }
        average /= entry.length;
        return max - average - min;
    }

    private float getProbableYaw(final double deltaX, final double sensitivityX) {
        double point = deltaX + 1.0;
        float probable = 0.0f;
        float minimum = Float.MAX_VALUE;
        for (int i = 0; i < 3; ++i) {
            final float predicted = this.getPredictedYaw(point, sensitivityX);
            final float difference = this.rotationYaw - predicted;
            if (difference < minimum) {
                minimum = difference;
                probable = predicted;
            }
            --point;
        }
        return probable;
    }

    private float getProbablePitch(final double deltaY, final double sensitivityY) {
        double point = deltaY + 1.0;
        float probable = 0.0f;
        float minimum = Float.MAX_VALUE;
        for (int i = 0; i < 3; ++i) {
            final float predicted = this.getPredictedPitch(point, sensitivityY);
            final float difference = this.rotationPitch - predicted;
            if (difference < minimum) {
                minimum = difference;
                probable = predicted;
            }
            --point;
        }
        return probable;
    }

    private float getPredictedYaw(final double deltaX, final double sensitivityX) {
        int delta = (int)deltaX;
        float var132 = (float)sensitivityX * 0.6f + 0.2f;
        float var133 = var132 * var132 * var132 * 8.0f;
        float var134 = delta * var133;
        return this.lastRotationYaw + var134 * 0.15f * data.getSensitivityHolder().inverseYaw;
    }

    public float getPredictedPitch(final double deltaY, final double sensitivityY) {
        int delta = (int)deltaY;
        float var132 = (float)sensitivityY * 0.6f + 0.2f;
        float var133 = var132 * var132 * var132 * 8.0f;
        float var134 = delta * var133;
        return this.lastRotationPitch + var134 * 0.15f * data.getSensitivityHolder().inversePitch;
    }

    public float getMinimumRotation(final double sensitivity, final float lastRotation, final float rotation) {
        float var132 = (float)sensitivity * 0.6f + 0.2f;
        float var133 = var132 * var132 * var132 * 8.0f;
        int inverse = (rotation - lastRotation > 0.0f) ? 1 : -1;
        return Math.abs(lastRotation - (lastRotation + var133 * 0.15f * inverse));
    }


    private boolean encloses(final float a, final float b, final float x) {
        final float distance = MathUtil.distanceBetweenAngles(a, b);
        return MathUtil.distanceBetweenAngles(a, x) < distance || MathUtil.distanceBetweenAngles(b, x) < distance;
    }

    private float ceil(final float a) {
        final double math = Math.ceil(a);
        return (float)math;
    }

    private float toRegularCircle(float angles) {
        angles %= 360.0f;
        return (angles < 0.0f) ? (angles + 360.0f) : angles;
    }
}
