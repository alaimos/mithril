package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Weights.NodeWeightComputationInterface;
import com.alaimos.MITHrIL.api.Data.Pathways.Types.NodeType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;
import java.util.Objects;

/**
 * Node class
 */
public class Node implements Comparable<Node>, Cloneable, Serializable {

    private static NodeWeightComputationInterface weightComputationMethod = null;
    @Serial
    private static final long serialVersionUID = 9051335523535138069L;
    private final String id;
    private final String name;
    private final List<String> aliases;
    private transient NodeType type;
    private transient double weight = Double.NaN;

    @SuppressWarnings("CopyConstructorMissesField")
    @Contract(pure = true)
    public Node(@NotNull Node n) {
        this(n.id, n.name, n.type.name(), List.copyOf(n.aliases));
    }

    /**
     * Constructor
     *
     * @param id      node id
     * @param name    node name
     * @param type    node type
     * @param aliases node aliases
     */
    public Node(String id, String name, NodeType type, List<String> aliases) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.aliases = aliases;
    }

    /**
     * Constructor
     *
     * @param id      node id
     * @param name    node name
     * @param type    node type
     * @param aliases node aliases
     */
    public Node(String id, String name, String type, List<String> aliases) {
        this(id, name, NodeType.fromString(type), aliases);
    }

    /**
     * Set the weight computation method
     *
     * @param w the method
     */
    public static void setWeightComputationMethod(NodeWeightComputationInterface w) {
        weightComputationMethod = w;
    }

    /**
     * Get the node id
     *
     * @return node id
     */
    public String id() {
        return this.id;
    }

    /**
     * Get the node name
     *
     * @return node name
     */
    public String name() {
        return this.name;
    }

    /**
     * Get the node aliases
     *
     * @return node aliases
     */
    public List<String> aliases() {
        return this.aliases;
    }

    /**
     * Get the node type
     *
     * @return node type
     */
    public NodeType type() {
        return this.type;
    }

    /**
     * Compute the weight of this node
     *
     * @return the weight
     */
    public double weight() {
        if (weightComputationMethod == null) {
            throw new RuntimeException("Weight computation procedure is not set.");
        }
        if (!Double.isNaN(weight)) return weight;
        return weight = weightComputationMethod.weight(this);
    }

    /**
     * Compare this node with another one
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(@NotNull Node o) {
        return id.compareTo(o.id);
    }

    /**
     * Checks if this node is equal to another one
     *
     * @param o the object to be compared.
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node node)) return false;
        return Objects.equals(id, node.id);
    }

    /**
     * Get the hash code of this node
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Clone the object
     *
     * @return my clone
     */
    public Object clone() {
        try {
            return new Node((Node) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Write the object to a stream
     *
     * @param stream the stream
     * @throws IOException if an I/O error occurs
     */
    @Serial
    private void writeObject(@NotNull ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeUTF(type.name());
    }

    /**
     * Read the object from a stream
     *
     * @param stream the stream
     * @throws IOException            if an I/O error occurs
     * @throws ClassNotFoundException if the class of a serialized object cannot be found
     */
    @Serial
    private void readObject(@NotNull ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        type = NodeType.fromString(stream.readUTF());
    }

    /**
     * Get a string representation of this node
     *
     * @return a string representation of this node
     */
    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type.name() +
                '}';
    }
}
