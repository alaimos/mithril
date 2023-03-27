package com.alaimos.MITHrIL.app.Data.Records;

import com.alaimos.MITHrIL.api.Data.Encoders.Int2ObjectMapEncoder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

public class MITHrILOutput implements Serializable {

    @Serial
    private static final long serialVersionUID = 8128179574146138977L;

    //region Data
    private final double[] nodePerturbations;
    private final double[] nodeAccumulators;
    private final double[] pathwayAccumulators;
    private final double[] pathwayCorrectedAccumulators;
    private final double[] pathwayImpactFactors;
    private final double[] nodePValues;
    private final double[] nodeAdjustedPValues;
    private final double[] pathwayPValues;
    private final double[] pathwayAdjustedPValues;
    private final double[] pathwayProbabilities;
    private final double[] pathwayNetworkProbabilities;
    private transient Int2ObjectMap<String> nodeIndex2Id = null;
    private transient Int2ObjectMap<String> pathwayIndex2Id = null;
    //endregion

    public MITHrILOutput(
            double[] nodePerturbations, double[] nodeAccumulators, double[] pathwayAccumulators,
            double[] pathwayCorrectedAccumulators, double[] pathwayImpactFactors, double[] nodePValues,
            double[] nodeAdjustedPValues, double[] pathwayPValues, double[] pathwayAdjustedPValues,
            double[] pathwayProbabilities, double[] pathwayNetworkProbabilities
    ) {
        this.nodePerturbations            = nodePerturbations;
        this.nodeAccumulators             = nodeAccumulators;
        this.pathwayAccumulators          = pathwayAccumulators;
        this.pathwayCorrectedAccumulators = pathwayCorrectedAccumulators;
        this.pathwayImpactFactors         = pathwayImpactFactors;
        this.nodePValues                  = nodePValues;
        this.nodeAdjustedPValues          = nodeAdjustedPValues;
        this.pathwayPValues               = pathwayPValues;
        this.pathwayAdjustedPValues       = pathwayAdjustedPValues;
        this.pathwayProbabilities         = pathwayProbabilities;
        this.pathwayNetworkProbabilities  = pathwayNetworkProbabilities;
    }

    //region Getters
    public double[] nodePerturbations() {
        return nodePerturbations;
    }

    public double[] nodeAccumulators() {
        return nodeAccumulators;
    }

    public double[] pathwayAccumulators() {
        return pathwayAccumulators;
    }

    public double[] pathwayCorrectedAccumulators() {
        return pathwayCorrectedAccumulators;
    }

    public double[] pathwayImpactFactors() {
        return pathwayImpactFactors;
    }

    public double[] nodePValues() {
        return nodePValues;
    }

    public double[] nodeAdjustedPValues() {
        return nodeAdjustedPValues;
    }

    public double[] pathwayPValues() {
        return pathwayPValues;
    }

    public double[] pathwayAdjustedPValues() {
        return pathwayAdjustedPValues;
    }

    public double[] pathwayProbabilities() {
        return pathwayProbabilities;
    }

    public double[] pathwayNetworkProbabilities() {
        return pathwayNetworkProbabilities;
    }

    public Int2ObjectMap<String> nodeIndex2Id() {
        return nodeIndex2Id;
    }

    public Int2ObjectMap<String> pathwayIndex2Id() {
        return pathwayIndex2Id;
    }
    //endregion

    //region Setters
    public void setNodeIndex2Id(Int2ObjectMap<String> nodeIndex2Id) {
        this.nodeIndex2Id = nodeIndex2Id;
    }

    public void setPathwayIndex2Id(Int2ObjectMap<String> pathwayIndex2Id) {
        this.pathwayIndex2Id = pathwayIndex2Id;
    }
    //endregion

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MITHrILOutput) obj;
        return Arrays.equals(this.nodePerturbations, that.nodePerturbations) && Arrays.equals(
                this.nodeAccumulators, that.nodeAccumulators) && Arrays.equals(
                this.pathwayAccumulators, that.pathwayAccumulators) && Arrays.equals(
                this.pathwayCorrectedAccumulators, that.pathwayCorrectedAccumulators) && Arrays.equals(
                this.pathwayImpactFactors, that.pathwayImpactFactors) && Arrays.equals(
                this.nodePValues, that.nodePValues) && Arrays.equals(
                this.nodeAdjustedPValues, that.nodeAdjustedPValues) && Arrays.equals(
                this.pathwayPValues, that.pathwayPValues) && Arrays.equals(
                this.pathwayAdjustedPValues, that.pathwayAdjustedPValues);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new int[]{
                Arrays.hashCode(nodePerturbations), Arrays.hashCode(nodeAccumulators),
                Arrays.hashCode(pathwayAccumulators), Arrays.hashCode(pathwayCorrectedAccumulators),
                Arrays.hashCode(pathwayImpactFactors), Arrays.hashCode(nodePValues),
                Arrays.hashCode(nodeAdjustedPValues), Arrays.hashCode(pathwayPValues),
                Arrays.hashCode(pathwayAdjustedPValues)
        });
    }

    @Serial
    private void writeObject(@NotNull ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeObject(Int2ObjectMapEncoder.encode(nodeIndex2Id));
        stream.writeObject(Int2ObjectMapEncoder.encode(pathwayIndex2Id));
    }

    @Serial
    private void readObject(java.io.@NotNull ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        nodeIndex2Id    = Int2ObjectMapEncoder.decode(stream.readObject());
        pathwayIndex2Id = Int2ObjectMapEncoder.decode(stream.readObject());
    }

}
