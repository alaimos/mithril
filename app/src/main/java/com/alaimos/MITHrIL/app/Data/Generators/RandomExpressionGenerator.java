package com.alaimos.MITHrIL.app.Data.Generators;

import com.alaimos.MITHrIL.api.Commons.Utils;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.Contract;
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
     * Set the constraints used to generate random expression values
     *
     * @param constraints constraints
     */
    public void constraints(ExpressionConstraint[] constraints) {
        this.constraints = constraints;
    }

    /**
     * Generate the next random expressions
     *
     * @return a pair of arrays containing node identifiers and their respective expression values
     */
    public Pair<String[], double[]> nextRandomExpression() {
        var nodeIds = new String[constraints.length];
        var expressions = new double[constraints.length];
        for (int i = 0; i < constraints.length; i++) {
            nodeIds[i]     = constraints[i].nodeId;
            expressions[i] = nextRandomExpressionFromConstraint(constraints[i]);
        }
        return Pair.of(nodeIds, expressions);
    }

    /**
     * Generate a normally distributed random number with a minimum value. The sign of the minimum value is used to
     * determine the direction of the distribution. If the minimum value is positive, the random number is taken from
     * the right tail of the distribution, otherwise from the left tail. This is achieved by generating a
     * normally-distributed random number and then copying the sign of the minimum value to the generated number.
     * Finally, if the generated number is smaller (in absolute value) than the minimum value, the minimum value is
     * returned.
     *
     * @param mean    mean of the distribution
     * @param sd      standard deviation of the distribution
     * @param epsilon the minimum value to return
     * @return a random number
     */
    private double nextGaussianRandomNumber(double mean, double sd, double epsilon) {
        var r = Math.copySign(Math.abs(sd * random.nextGaussian() + mean), epsilon);
        return ((r < 0 && r > epsilon) || (r > 0 && r < epsilon)) ? epsilon : r;
    }


    /**
     * Generate a random expression value
     *
     * @param c an expression constraint
     * @return the expression value
     */
    private double nextRandomExpressionFromConstraint(@NotNull ExpressionConstraint c) {
        var direction = c.direction;
        if (c.distribution == null && Double.isNaN(c.baseLog2FoldChange)) {
            return switch (direction) {
                case OVEREXPRESSION -> nextGaussianRandomNumber(UP_LOG_FC_MEAN, UP_DOWN_LOG_FC_STD_DEV, epsilon);
                case UNDEREXPRESSION -> nextGaussianRandomNumber(DOWN_LOG_FC_MEAN, UP_DOWN_LOG_FC_STD_DEV, -epsilon);
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
            case OVEREXPRESSION -> nextGaussianRandomNumber(mean, sd, epsilon);
            case UNDEREXPRESSION -> nextGaussianRandomNumber(mean, sd, -epsilon);
            default -> 0.0;
        };
    }

    public record ExpressionConstraint(
            String nodeId,
            ExpressionDirection direction,
            ExpressionDistribution distribution,
            double baseLog2FoldChange
    ) {

        @Contract("_, _ -> new")
        public static @NotNull ExpressionConstraint of(String nodeId, @NotNull ExpressionConstraint c) {
            return new ExpressionConstraint(nodeId, c.direction, c.distribution, c.baseLog2FoldChange);
        }

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
