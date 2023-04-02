package com.alaimos.MITHrIL.app.Data.Generators;

import com.alaimos.MITHrIL.api.Commons.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class RandomExpressionGenerator {

    public static final double BASE_LOG_FC_STD_DEV = 4.0;
    public static final double BASE_LOG_FC_MEAN = 0.0;
    public static final double UP_LOG_FC_MEAN = 5.0;
    public static final double DOWN_LOG_FC_MEAN = -5.0;
    public static final double UP_DOWN_LOG_FC_STD_DEV = 2.0;

    private ExpressionConstraint[] constraints;
    private final double epsilon;
    private final Random random;

    public RandomExpressionGenerator(Random random, double epsilon) {
        this.epsilon = (epsilon > 0) ? Math.pow(10, -Math.ceil(-Math.log10(epsilon))) : 0.0;
        this.random  = random;
    }

    /**
     * Generate a normally distributed random number
     *
     * @param mean mean of the distribution
     * @param sd   standard deviation of the distribution
     * @return a number
     */
    public double gaussianRandomNumber(double mean, double sd) {
        return sd * random.nextGaussian() + mean;
    }

    /**
     * Generate a random expression value
     *
     * @param c an expression constraint
     * @return the expression value
     */
    protected double randomExpression(@NotNull ExpressionConstraint c) {
        var direction = c.direction;
        if (c.distribution == null && Double.isNaN(c.baseLog2FoldChange)) {
            return switch (direction) {
                case OVEREXPRESSION -> Math.max(
                        epsilon, gaussianRandomNumber(UP_LOG_FC_MEAN, UP_DOWN_LOG_FC_STD_DEV));
                case UNDEREXPRESSION -> Math.min(
                        -epsilon, gaussianRandomNumber(DOWN_LOG_FC_MEAN, UP_DOWN_LOG_FC_STD_DEV));
                default -> 0.0;
            };
        }
        var mean = (c.distribution != null) ? c.distribution.mean : BASE_LOG_FC_MEAN;
        var sd = (c.distribution != null) ? c.distribution.stdDev : BASE_LOG_FC_STD_DEV;
        if (!Double.isNaN(c.baseLog2FoldChange)) {
            direction = (c.baseLog2FoldChange > 0) ? ExpressionDirection.OVEREXPRESSION : ExpressionDirection.UNDEREXPRESSION;
            mean += c.baseLog2FoldChange;
        }
        return switch (direction) {
            case OVEREXPRESSION -> Math.max(epsilon, gaussianRandomNumber(mean, sd));
            case UNDEREXPRESSION -> Math.min(-epsilon, gaussianRandomNumber(mean, sd));
            default -> 0.0;
        };
    }

    public record ExpressionConstraint(
            String nodeId,
            ExpressionDirection direction,
            ExpressionDistribution distribution,
            double baseLog2FoldChange
    ) {
    }

    public record ExpressionDistribution(double mean, double stdDev) {
    }

    public enum ExpressionDirection {
        OVEREXPRESSION,
        UNDEREXPRESSION,
        NONE;

        public static ExpressionDirection fromString(String string) {
            return Utils.getEnumFromString(ExpressionDirection.class, string, NONE);
        }
    }
}
