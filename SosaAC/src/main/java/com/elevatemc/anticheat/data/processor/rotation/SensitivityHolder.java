package com.elevatemc.anticheat.data.processor.rotation;

import com.elevatemc.anticheat.data.PlayerData;
import com.elevatemc.anticheat.util.type.EvictingList;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayDeque;

@Getter
@Setter
public class SensitivityHolder {

    public PlayerData data;
    public final EvictingList<Double> samplesYaw;
    public final EvictingList<Double> samplesPitch;
    public final ArrayDeque<Integer> integerSensitivitySamples;

    public float smoothCamFilterX;
    public float smoothCamFilterY;
    public float cinematicYaw;
    public float cinematicPitch;
    public float smoothCamYaw;
    public float smoothCamPitch;
    public double[] gridYaw;
    public double[] gridPitch;
    public double[] gridComputed;
    public double currentDivisorYaw;
    public double currentDivisorPitch;
    public double currentDivisorComputed;
    public double modeYaw;
    public double modePitch;
    public double modeComputed;
    public double sensitivity;
    public double sensitivityX;
    public double sensitivityY;
    public double sensitivityXY;
    public double formatX;
    public double formatY;
    public int inverseYaw;
    public int inversePitch;
    public double computedX;
    public double computedY;
    public double deltaX;
    public double deltaY;
    public float predictedYaw;
    public float predictedPitch;
    public float distanceYaw;
    public float distancePitch;
    public float minimumYaw;
    public float minimumPitch;
    public boolean enclosesYaw;
    public boolean enclosesPitch;
    public int rotations;
    public double lastDeltaX;
    public double lastDeltaY;
    public boolean encloseX;
    public boolean encloseY;
    public float differenceX;
    public float differenceY;
    public double deltaDifferenceX;
    public double deltaDifferenceY;
    public double derivationX;
    public double derivationY;
    public double mcpSensitivity;
    public float lastPredictedYaw;
    public float lastPredictedPitch;
    public long rate;
    public int integerSensitivity;

    public SensitivityHolder(final PlayerData data) {
        this.data = data;
        this.samplesYaw = new EvictingList<>(50);
        this.samplesPitch = new EvictingList<>(50);
        this.integerSensitivitySamples = new ArrayDeque<>();
        this.smoothCamFilterX = 0.0f;
        this.smoothCamFilterY = 0.0f;
        this.cinematicYaw = 0.0f;
        this.cinematicPitch = 0.0f;
        this.smoothCamYaw = 0.0f;
        this.smoothCamPitch = 0.0f;
        this.gridYaw = new double[40];
        this.gridPitch = new double[40];
        this.gridComputed = new double[40];
        this.currentDivisorYaw = 0.0;
        this.currentDivisorPitch = 0.0;
        this.currentDivisorComputed = 0.0;
        this.modeYaw = Double.MIN_VALUE;
        this.modePitch = Double.MIN_VALUE;
        this.modeComputed = Double.MIN_VALUE;
        this.sensitivity = 0.0;
        this.sensitivityX = 0.0;
        this.sensitivityY = 0.0;
        this.sensitivityXY = 0.0;
        this.formatX = 0.0;
        this.formatY = 0.0;
        this.inverseYaw = 1;
        this.inversePitch = 1;
        this.computedX = 0.0;
        this.computedY = 0.0;
        this.deltaX = 0.0;
        this.deltaY = 0.0;
        this.predictedYaw = 0.0f;
        this.predictedPitch = 0.0f;
        this.distanceYaw = 0.0f;
        this.distancePitch = 0.0f;
        this.minimumYaw = 0.0f;
        this.minimumPitch = 0.0f;
        this.rotations = 0;
        this.lastDeltaX = 0.0;
        this.lastDeltaY = 0.0;
        this.differenceX = 0.0f;
        this.differenceY = 0.0f;
        this.deltaDifferenceX = 0.0;
        this.deltaDifferenceY = 0.0;
        this.mcpSensitivity = -1.0;
        this.lastPredictedYaw = 0.0f;
        this.lastPredictedPitch = 0.0f;
        this.integerSensitivity = 0;
    }

    public boolean hasValidSensitivity() {
        return this.integerSensitivity > 0 && this.integerSensitivity < 200;
    }

    public double getDerivation(final double dx, final double var) {
        return dx / var;
    }

}
