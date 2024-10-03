package com.elevatemc.anticheat.base.check.impl.fight.aimassist.analytics;

import com.elevatemc.anticheat.base.PlayerData;
import com.elevatemc.anticheat.base.check.type.RotationCheck;
import com.elevatemc.anticheat.base.check.violation.category.Category;
import com.elevatemc.anticheat.base.check.violation.category.SubCategory;
import com.elevatemc.anticheat.base.check.violation.handler.ViolationHandler;
import com.elevatemc.anticheat.base.check.violation.impl.DetailedPlayerViolation;
import com.elevatemc.anticheat.util.chat.CC;
import com.elevatemc.anticheat.util.math.EvictingList;
import com.elevatemc.anticheat.util.math.LinearRegression;
import com.elevatemc.anticheat.util.math.MathUtil;

public class AimAnalyticsF extends RotationCheck {

    EvictingList<Double> data = new EvictingList<>(250), lel = new EvictingList<>(250);

    public AimAnalyticsF(PlayerData playerData) {
        super(playerData, "Analytics F", "Sampled player rotation data", new ViolationHandler(11, 3000L), Category.COMBAT, SubCategory.AIM_ASSIST, 2);
    }

    @Override
    public void handle() {
        boolean attacking = actionTracker.getLastAttack() <= 4;

        handle:
        {
            if (!attacking || rotationTracker.isZooming()) break handle;

            double deltaYaw = rotationTracker.getDeltaYaw();
            double deltaPitch = rotationTracker.getPitch();

            data.add(deltaYaw);
            lel.add(deltaPitch);

            if (data.isFull()) {

                Double[] regressionX = new Double[data.size()];
                Double[] regressionY = new Double[lel.size()];

                regressionX = data.toArray(regressionX);
                regressionY = lel.toArray(regressionY);

                final LinearRegression regression = new LinearRegression(regressionX, regressionY);

                double st1 = MathUtil.getStandardDeviation(data);
                double st2 = MathUtil.getStandardDeviation(lel);
                double error = regression.interceptStdErr();
                double prediction = regression.predict(1.75);

                String information = CC.translate("Y " + format(st1) + " P " + format(st2) + " E " + format(error));
                if (st1 > 6.5 && st2 > 12.35 && error > 1.0 && error < 3.5 && prediction > 1.44) {
                    if (increaseBuffer() > 2) {
                        handleViolation(new DetailedPlayerViolation(this, information));
                        resetBuffer();
                    }
                }
                data.clear();
                lel.clear();
            }
        }
    }
}