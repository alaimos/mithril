package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights.WeightComputationInterface;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Edge class
 */
public class Edge implements Cloneable, Serializable {

    private static WeightComputationInterface weightComputationMethod = null;
    @Serial
    private static final long serialVersionUID = 6045908408087788911L;
    private final Node source;
    private final Node target;
    private final List<EdgeDetail> details = new ArrayList<>();
    private transient double weight = Double.NaN;

    public Edge(@NotNull Edge e) {
        this(e.source, e.target, e.details.stream().map(EdgeDetail::new).collect(Collectors.toList()));
    }

    public Edge(Node source, Node target) {
        this.source = source;
        this.target = target;
    }

    public Edge(Node source, Node target, EdgeDetail detail) {
        this(source, target);
        details.add(detail);
    }

    public Edge(Node source, Node target, List<EdgeDetail> details) {
        this(source, target);
        this.details.addAll(details);
    }

    public Node source() {
        return source;
    }

    public Node target() {
        return target;
    }

    public List<EdgeDetail> details() {
        return details;
    }

    public boolean isMultiEdge() {
        return (this.details.size() > 1);
    }

    public static void setWeightComputationMethod(WeightComputationInterface w) {
        weightComputationMethod = w;
    }

    public double weight() {
        if (weightComputationMethod == null) {
            throw new RuntimeException("Weight computation procedure is not set.");
        }
        if (!Double.isNaN(weight)) return weight;
        return weight = weightComputationMethod.weight(this);
    }

    public void merge(@NotNull Edge e, boolean usePriority, boolean allowDuplicatedDetails) {
        // Add details from e to this edge
        e.details.stream().filter(d -> allowDuplicatedDetails || !details.contains(d)).forEach(d -> details.add(new EdgeDetail(d)));
        if (usePriority) { // if priority is used, keep only the details with the highest priority
            var maxPriority = details.stream().mapToInt(d -> d.subtype().priority()).max().orElse(0);
            details.removeIf(d -> d.subtype().priority() < maxPriority);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge e)) return false;
        return Objects.equals(source, e.source) &&
                Objects.equals(target, e.target) &&
                Objects.equals(details, e.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, details);
    }

    /**
     * Clone the object
     *
     * @return my clone
     */
    public Object clone() {
        try {
            return new Edge((Edge) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "Edge{" +
                "source=" + source.id() +
                ", target=" + target.id() +
                ", details=" + details +
                '}';
    }
}
