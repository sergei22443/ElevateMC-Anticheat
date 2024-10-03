package com.elevatemc.anticheat.util.math;

import com.elevatemc.anticheat.util.type.Pair;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.experimental.UtilityClass;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@UtilityClass
public class MathUtil {

    public final double EXPANDER = Math.pow(2, 24);

    public static double hypot(final double x, final double z) {
        return Math.sqrt(x * x + z * z);
    }

    public static double getDeviation(final Deque<? extends Number> deque) {
        final double average = deque.stream().mapToDouble(Number::doubleValue).average().orElse(0.0);
        double stdDeviation = 0.0;
        for (final Number number : deque) {
            stdDeviation += Math.pow(number.doubleValue() - average, 2.0);
        }
        return Math.sqrt(stdDeviation / deque.size());
    }

    public double getVariance(final Collection<? extends Number> data) {
        int count = 0;

        double sum = 0.0;
        double variance = 0.0;

        double average;

        for (final Number number : data) {
            sum += number.doubleValue();
            ++count;
        }

        average = sum / count;

        for (final Number number : data) {
            variance += Math.pow(number.doubleValue() - average, 2.0);
        }

        return variance;
    }

    public double getStandardDeviation(final Collection<? extends Number> data) {
        final double variance = getVariance(data);

        return Math.sqrt(variance);
    }

    public double getSkewness(final Collection<? extends Number> data) {
        double sum = 0;
        int count = 0;

        final List<Double> numbers = Lists.newArrayList();

        for (final Number number : data) {
            sum += number.doubleValue();
            ++count;

            numbers.add(number.doubleValue());
        }

        Collections.sort(numbers);

        final double mean =  sum / count;
        final double median = (count % 2 != 0) ? numbers.get(count / 2) : (numbers.get((count - 1) / 2) + numbers.get(count / 2)) / 2;
        final double stDev = getStandardDeviation(data);

        return 3 * (mean - median) / stDev;
    }

    public double getAverage(final Collection<? extends Number> data) {
        return data.stream()
                .mapToDouble(Number::doubleValue)
                .average()
                .orElse(0D);
    }

    public static float getDistanceBetweenAngles(final float angleOne, final float angleTwo) {
        float distance = Math.abs(angleOne - angleTwo) % 360.0f;
        if (distance > 180.0) {
            distance = 360.0f - distance;
        }
        return distance;
    }

    public static float distanceBetweenAngles(final float alpha, final float beta) {
        final float alphax = alpha % 360.0f;
        final float betax = beta % 360.0f;
        final float delta = Math.abs(alphax - betax);
        return (float) Math.abs(Math.min(360.0 - delta, delta));
    }


    public static int getMode(Collection<? extends Number> array) {
        int mode = (int) array.toArray()[0];
        int maxCount = 0;
        for (Number value : array) {
            int count = 1;
            for (Number i : array) {
                if (i.equals(value))
                    count++;
                if (count > maxCount) {
                    mode = (int) value;
                    maxCount = count;
                }
            }
        }
        return mode;
    }

    public static <T extends Number> T getModeTwo(final Collection<T> collect) {
        final Map<T, Integer> repeated = new HashMap<>();

        //Sorting each value by how to repeat into a map.
        collect.forEach(val -> {
            final int number = repeated.getOrDefault(val, 0);

            repeated.put(val, number + 1);
        });

        //Calculating the largest value to the key, which would be the mode.
        return repeated.keySet().stream()
                .map(key -> new Pair<>(key, repeated.get(key))) //We map it into a Tuple for easier sorting.
                .max(Comparator.comparing(Pair::getY, Comparator.naturalOrder()))
                .orElseThrow(NullPointerException::new).getX();
    }

    public static double getMode(final double[] collect) {
        final List<Double> doubles = new ArrayList<>();

        for (double v : collect) {
            doubles.add(v);
        }

        return getModeTwo(doubles);
    }

    public static double invSqrt(double x) {
        double xhalf = 0.5d * x;
        long i = Double.doubleToLongBits(x);
        i = 0x5fe6ec85e7de30daL - (i >> 1);
        x = Double.longBitsToDouble(i);
        x *= (1.5d - xhalf * x * x);
        return x;
    }

    private double getMedian(final List<Double> data) {
        if (data.size() % 2 == 0) {
            return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2;
        } else {
            return data.get(data.size() / 2);
        }
    }

    public static boolean isScientificNotation(final Float f) {
        return f.toString().contains("E");
    }

    public static boolean isScientificNotation(final Double f) {
        return f.toString().contains("E");
    }

    public Pair<List<Double>, List<Double>> getOutliers(final Collection<? extends Number> collection) {
        final List<Double> values = new ArrayList<>();

        for (final Number number : collection) {
            values.add(number.doubleValue());
        }

        final double q1 = getMedian(values.subList(0, values.size() / 2));
        final double q3 = getMedian(values.subList(values.size() / 2, values.size()));

        final double iqr = Math.abs(q1 - q3);
        final double lowThreshold = q1 - 1.5 * iqr, highThreshold = q3 + 1.5 * iqr;

        final Pair<List<Double>, List<Double>> tuple = new Pair<>(new ArrayList<>(), new ArrayList<>());

        for (final Double value : values) {
            if (value < lowThreshold) {
                tuple.getX().add(value);
            }
            else if (value > highThreshold) {
                tuple.getY().add(value);
            }
        }

        return tuple;
    }

    public static double roundToPlace(final double value, final int places) {
        final double multiplier = Math.pow(10.0, places);
        return Math.round(value * multiplier) / multiplier;
    }

    public double getGcd(final double a, final double b) {
        if (a < b) {
            return getGcd(b, a);
        }

        if (Math.abs(b) < 0.001) {
            return a;
        } else {
            return getGcd(b, a - Math.floor(a / b) * b);
        }
    }

    public long getGcd(final long current, final long previous) {
        return (previous <= 16384L) ? current : getGcd(previous, current % previous);
    }

    public long johannessGcd(long a, long b) {
        if (b <= 0x4000) {
            return a;
        }
        return johannessGcd(b, a % b);
    }

    public double getCps(final Collection<? extends Number> data) {
        return (20 / getAverage(data)) * 50;
    }

    public int getDuplicates(final Collection<? extends Number> data) {
        return (int)(data.size() - data.stream().distinct().count());
    }

    public int getDistinct(final Collection<? extends Number> data) {
        return (int)data.stream().distinct().count();
    }

    public double getStDev(Collection<? extends Number> values) {
        double average = getAverage(values);

        AtomicDouble variance = new AtomicDouble(0D);

        values.forEach(delay -> variance.getAndAdd(Math.pow(delay.doubleValue() - average, 2D)));

        return Math.sqrt(variance.get() / values.size());
    }
    public double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public double getRangeDifference (Collection<? extends Number> numbers){
        OptionalDouble minOptional = numbers.stream().mapToDouble(Number::doubleValue).min();
        OptionalDouble maxOptional = numbers.stream().mapToDouble(Number::doubleValue).max();
        if (minOptional.isPresent() || maxOptional.isPresent()) {
            return 500.0;
        }
        return maxOptional.getAsDouble() - minOptional.getAsDouble();
    }

    public double getRandomDouble(double number1, double number2) {
        return number1 + (number2 - number1) * new Random().nextDouble();
    }

    public double getKurtosis(final Collection<? extends Number> data) {
        double sum = 0.0;
        int count = 0;

        for (Number number : data) {
            sum += number.doubleValue();
            ++count;
        }

        if (count < 3.0) {
            return 0.0;
        }

        final double efficiencyFirst = count * (count + 1.0) / ((count - 1.0) * (count - 2.0) * (count - 3.0));
        final double efficiencySecond = 3.0 * Math.pow(count - 1.0, 2.0) / ((count - 2.0) * (count - 3.0));
        final double average = sum / count;

        double variance = 0.0;
        double varianceSquared = 0.0;

        for (final Number number : data) {
            variance += Math.pow(average - number.doubleValue(), 2.0);
            varianceSquared += Math.pow(average - number.doubleValue(), 4.0);
        }

        return efficiencyFirst * (varianceSquared / Math.pow(variance / sum, 2.0)) - efficiencySecond;
    }

    // Credit goes out to Simon
    public double kurtosis(Collection<? extends Number> values) {
        double n = values.size();

        if (n < 3)
            return Double.NaN;

        double average = getAverage(values);
        double stDev = getStDev(values);

        AtomicDouble accum = new AtomicDouble(0D);

        values.forEach(delay -> accum.getAndAdd(Math.pow(delay.doubleValue() - average, 4D)));

        return n * (n + 1) / ((n - 1) * (n - 2) * (n - 3)) *
                (accum.get() / Math.pow(stDev, 4D)) - 3 *
                Math.pow(n - 1, 2D) / ((n - 2) * (n - 3));
    }

    public static float opt(float value) {
        return ((float) Math.cbrt((value / 0.15f) / 8f) - 0.2f) / .6f;
    }

    public static double clamp180(double theta) {
        theta %= 360.0;
        if (theta >= 180.0) {
            theta -= 360.0;
        }
        if (theta < -180.0) {
            theta += 360.0;
        }
        return theta;
    }

    public static float convertYaw(float yaw) {
        if (yaw <= -360.0f) {
            yaw = -(-yaw % 360.0f);
        }
        else if (yaw >= 360.0f) {
            yaw %= 360.0f;
        }
        return yaw;
    }

    public static float getYawDiff(float yaw1, float yaw2) {
        if (yaw1 <= -360.0f) {
            yaw1 = -(-yaw1 % 360.0f);
        }
        else if (yaw1 >= 360.0f) {
            yaw1 %= 360.0f;
        }
        if (yaw2 <= -360.0f) {
            yaw2 = -(-yaw2 % 360.0f);
        }
        else if (yaw2 >= 360.0f) {
            yaw2 %= 360.0f;
        }
        float yawDiff = yaw1 - yaw2;
        if (yawDiff < -180.0f) {
            yawDiff += 360.0f;
        }
        else if (yawDiff > 180.0f) {
            yawDiff -= 360.0f;
        }
        return yawDiff;
    }
}
