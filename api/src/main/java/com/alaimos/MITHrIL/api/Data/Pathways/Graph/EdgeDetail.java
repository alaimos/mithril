package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import com.alaimos.MITHrIL.api.Data.Pathways.Types.EdgeSubtype;
import com.alaimos.MITHrIL.api.Data.Pathways.Types.EdgeType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Objects;

/**
 * Edge detail. It contains the type and subtype of an edge
 */
public class EdgeDetail implements Cloneable, Serializable {

    @Serial
    private static final long serialVersionUID = 2999560788466357768L;
    private transient EdgeType type;
    private transient EdgeSubtype subtype;

    /**
     * Create a clone of an EdgeDetail object
     * @param d EdgeDetail object
     */
    @Contract(pure = true)
    public EdgeDetail(@NotNull EdgeDetail d) {
        this(d.type.name(), d.subtype.name());
    }

    /**
     * Create an edge detail from its type and subtype
     *
     * @param type    Edge type
     * @param subType Edge subType
     */
    public EdgeDetail(EdgeType type, EdgeSubtype subType) {
        this.type = type;
        this.subtype = subType;
    }

    /**
     * Create an edge detail from its type and subtype
     *
     * @param type    Edge type
     * @param subType Edge subType
     */
    public EdgeDetail(String type, String subType) {
        this(EdgeType.fromString(type), EdgeSubtype.fromString(subType));
    }

    /**
     * Get the edge type
     *
     * @return the type
     */
    public EdgeType type() {
        return type;
    }

    /**
     * Get the edge subtype
     *
     * @return the subtype
     */
    public EdgeSubtype subtype() {
        return subtype;
    }

    /**
     * Compare two edge details
     * @param o EdgeDetail object
     * @return true, if the two objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EdgeDetail d)) return false;
        return type.equals(d.type) && subtype.equals(d.subtype);
    }

    /**
     * Get the hash code
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type.name(), subtype.name());
    }

    /**
     * Clone the object
     *
     * @return my clone
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            return new EdgeDetail((EdgeDetail) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Serialize the object
     * @param stream the stream
     * @throws IOException if something goes wrong
     */
    @Serial
    private void writeObject(@NotNull ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeUTF(type.name());
        stream.writeUTF(subtype.name());
    }

    /**
     * Deserialize the object
     * @param stream the stream
     * @throws IOException if something goes wrong
     * @throws ClassNotFoundException if something goes wrong
     */
    @Serial
    private void readObject(@NotNull ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        type = EdgeType.valueOf(stream.readUTF());
        subtype = EdgeSubtype.valueOf(stream.readUTF());
    }

    @Override
    public String toString() {
        return "EdgeDetail{" +
                "type=" + type +
                ", subtype=" + subtype +
                '}';
    }
}
