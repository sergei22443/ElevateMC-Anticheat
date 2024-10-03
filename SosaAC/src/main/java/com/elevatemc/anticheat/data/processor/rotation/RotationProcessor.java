package com.elevatemc.anticheat.data.processor.rotation;

import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.math.MathUtil;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RotationProcessor
{

    private final PlayerData data;
    private float yaw;
    private float pitch;
    private float lastYaw;
    private float lastPitch;
    private float deltaYaw;
    private float deltaPitch;
    private float lastDeltaYaw;
    private float lastDeltaPitch;
    private float expectedYaw;
    private float expectedPitch;

    private float predictedPitch;
    private float predictedYaw;
    private float yawDiff;
    private float pitchDiff;

    private float yawAcceleration;
    private float pitchAcceleration;
    private double finalSensitivity;
    private double mcpSensitivity;
    public int zoomTicks, smoothZoomTicks, smoothingTicks, smoothOptTicks, nanTicks, invSqrtTicks;

    private boolean isZooming;

    private long lastZoom;
    private double previousPitchSquaredInversed;

    private float x = 0, y = 0, z = 0;

    public RotationProcessor(final PlayerData data) {
        this.data = data;
    }

    public void handle(final float yaw, final float pitch) {
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
        this.yaw = yaw;
        this.pitch = pitch;
        this.lastDeltaYaw = this.deltaYaw;
        this.lastDeltaPitch = this.deltaPitch;
        this.deltaYaw = Math.abs(yaw - this.lastYaw);
        this.deltaPitch = Math.abs(pitch - this.lastPitch);
        this.expectedYaw = deltaYaw * 1.073742f + (float) (deltaYaw + 0.15);
        this.expectedPitch = deltaPitch * 1.073742f - (float)(deltaPitch - 0.15);
        this.pitchDiff = Math.abs(deltaPitch - expectedPitch);
        this.yawDiff = Math.abs(deltaYaw - expectedYaw);
        this.predictedPitch = Math.abs(deltaPitch - pitchDiff);
        this.predictedYaw = Math.abs(deltaYaw - yawDiff);
        this.yawAcceleration = Math.abs(deltaYaw - lastDeltaYaw);
        this.pitchAcceleration = Math.abs(deltaPitch - lastDeltaPitch);

        this.processTicks();
        this.processOptifine();
    }
    private void processOptifine() {
        double yawDiff = Math.abs(this.deltaYaw - this.lastDeltaYaw);
        double pitchDiff = Math.abs(this.deltaPitch - this.lastDeltaPitch);
        if ((pitchDiff < 0.009400162506103517 && pitchDiff > 0.0) || (yawDiff < 0.009400162506103517 && yawDiff > 0.0)) {
            ++this.zoomTicks;
        }
        if (this.zoomTicks >= 2) {
            this.setZooming(true);
            this.setLastZoom(System.currentTimeMillis());
            this.setZoomTicks(0);
        }
        else {
            if ((this.isZooming && pitchDiff > 0.009400162506103517) || (this.isZooming && pitchDiff == 0.0) || (this.isZooming && yawDiff == 0.0) || (this.isZooming && yawDiff > 0.009400162506103517)) {
                ++this.smoothZoomTicks;
            }
            else if (this.smoothZoomTicks > 0 && this.isZooming) {
                --this.smoothZoomTicks;
            }
            if (this.smoothZoomTicks > 4) {
                this.setZooming(false);
                this.smoothZoomTicks = 0;
            }
        }
    }

    private void processTicks() {
        float yaw = MathUtil.opt(deltaYaw % 360), pitch = MathUtil.opt(deltaPitch);
        float smoothing = ((float) Math.cbrt(((deltaYaw % 360) / 0.15f) / 8f) - 0.2f) / .6f;
        float smooth = smooth(yaw, pitch * 0.05f);
        boolean smoothing2 = (Math.abs(smooth - smoothing) > 0.2 && smoothing > 1.2);
        if (!smoothing2) {
            ++this.smoothingTicks;
        } else {
            this.smoothingTicks = 0;
        }
        if (!smoothing2) {
            if (smoothOptTicks <= 100)
                smoothOptTicks += 1;
        } else {
            if (smoothOptTicks > 0)
                smoothOptTicks -= 5;
        }

        double invalidPitch = deltaPitch % lastDeltaPitch;
        double invalidYaw = (deltaYaw % 360) % invalidPitch;
        if (Double.isNaN(invalidYaw) && Double.isNaN(invalidPitch) || invalidYaw == invalidPitch) {
            ++this.nanTicks;
        } else {
            this.nanTicks = 0;
        }

        double yawSquaredInverse = MathUtil.invSqrt(yawAcceleration);
        double pitchSquaredInverse = MathUtil.invSqrt(pitchAcceleration);       //Fix invalid ticks :)
        if (yawSquaredInverse != pitchSquaredInverse && Math.abs(pitch) < 80) {
            if (pitchSquaredInverse == previousPitchSquaredInversed) {
                invSqrtTicks++;
            } else {
                invSqrtTicks = 0;
            }
            this.previousPitchSquaredInversed = pitchSquaredInverse;
        }
    }

    public PlayerData getData() {
        return this.data;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getLastYaw() {
        return this.lastYaw;
    }

    public float getLastPitch() {
        return this.lastPitch;
    }

    public float getDeltaYaw() {
        return this.deltaYaw;
    }

    public float getDeltaPitch() {
        return this.deltaPitch;
    }

    public float getLastDeltaYaw() {
        return this.lastDeltaYaw;
    }

    public float getLastDeltaPitch() {
        return this.lastDeltaPitch;
    }

    public void setLastZoom(long lastZoom) {
        this.lastZoom = lastZoom;
    }

    public int getZoomTicks() {
        return zoomTicks;
    }

    public long getLastZoom() {
        return lastZoom;
    }

    public boolean isSmooth() {
        return smoothZoomTicks > 0 || zoomTicks > 0;
    }

    public float smooth(float toSmooth, float increment) {
        x += toSmooth;
        toSmooth = (x - y) * increment;
        z += (toSmooth - z) * 0.5f;

        if (toSmooth > 0f && toSmooth > z || toSmooth < 0f && toSmooth < z) {
            toSmooth = z;
        }

        y += toSmooth;
        return toSmooth;
    }
}
