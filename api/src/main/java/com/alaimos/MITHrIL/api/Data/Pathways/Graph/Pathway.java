package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Pathway class
 */
public class Pathway implements Cloneable, Serializable {

    @Serial
    private static final long serialVersionUID = -6296993010293530989L;

    private final String id;
    private final String name;
    private final Graph graph;
    private final ArrayList<String> categories = new ArrayList<>();
    private boolean hidden = false;

    @SuppressWarnings("CopyConstructorMissesField")
    public Pathway(@NotNull Pathway p) {
        this(p.id, p.name, p.hasGraph() ? new Graph(p.graph) : null, p.categories);
    }

    public Pathway(String id, String name, Graph graph) {
        this.id = id;
        this.name = name;
        this.graph = graph;
    }

    public Pathway(String id, String name, Graph graph, Collection<String> categories) {
        this(id, name, graph);
        this.categories.addAll(categories);
    }

    public Pathway(String id, String name, Graph graph, @NotNull String categories) {
        this(id, name, graph);
        String[] cats = categories.split(";");
        for (String c : cats) {
            if (!c.trim().isEmpty()) this.categories.add(c.trim());
        }
    }

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public boolean hasGraph() {
        return (this.graph != null);
    }

    public Graph graph() {
        return this.graph;
    }

    public List<String> categories() {
        return this.categories;
    }

    public boolean hasCategory(String category) {
        return this.categories.contains(category);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pathway pathway)) return false;
        return Objects.equals(id, pathway.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, graph);
    }

    /**
     * Clone the object
     *
     * @return my clone
     */
    public Object clone() {
        try {
            return new Pathway((Pathway) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isHidden() {
        return hidden;
    }
}
