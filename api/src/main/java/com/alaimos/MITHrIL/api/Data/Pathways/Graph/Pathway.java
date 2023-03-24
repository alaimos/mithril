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

    public Pathway(@NotNull Pathway p) {
        this(p.id, p.name, (Graph) p.graph.clone(), p.categories);
        this.hidden = p.hidden;
    }

    public Pathway(String id, String name, @NotNull Graph graph) {
        this.id    = id;
        this.name  = name;
        this.graph = graph;
    }

    public Pathway(String id, String name, @NotNull Graph graph, Collection<String> categories) {
        this(id, name, graph);
        this.categories.addAll(categories);
    }

    public Pathway(String id, String name, @NotNull Graph graph, @NotNull String categories) {
        this(id, name, graph);
        String[] cats = categories.split(";");
        for (String c : cats) {
            if (!c.trim().isEmpty()) this.categories.add(c.trim());
        }
    }

    /**
     * Get the pathway id
     *
     * @return the id
     */
    public String id() {
        return this.id;
    }

    /**
     * Get the pathway name
     *
     * @return the name
     */
    public String name() {
        return this.name;
    }

    /**
     * Get the pathway graph
     *
     * @return the graph
     */
    public @NotNull Graph graph() {
        return this.graph;
    }

    /**
     * Get the pathway categories
     *
     * @return the categories
     */
    public List<String> categories() {
        return this.categories;
    }

    /**
     * Check if the pathway has a category
     *
     * @param category the category
     * @return true if the pathway has the category
     */
    public boolean hasCategory(String category) {
        return this.categories.contains(category);
    }

    /**
     * Check if this pathway is equal to another one. Two pathways are equal if they have the same id.
     *
     * @param o the other pathway
     * @return true if the two pathways are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pathway pathway)) return false;
        return Objects.equals(id, pathway.id);
    }

    /**
     * Get the hash code
     *
     * @return the hash code
     */
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

    /**
     * Is the pathway hidden? A hidden pathway should not be saved in the result file of any analysis performed on its
     * repository. This is useful to hide the meta-pathway in the final results.
     *
     * @return the hidden flag
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Set the hidden flag
     *
     * @param hidden the hidden flag
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
