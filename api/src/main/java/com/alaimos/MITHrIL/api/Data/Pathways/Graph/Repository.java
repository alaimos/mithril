package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class Repository implements Collection<Pathway>, Iterable<Pathway>, Cloneable, Serializable {

    @Serial
    private static final long serialVersionUID = 3494925951227066338L;
    private final Map<String, List<String>> pathwaysByCategory = new HashMap<>();
    private final Map<String, Pathway> pathways = new HashMap<>();
    private final Map<String, VirtualPathway> virtualPathways = new HashMap<>();
    private String defaultPathway = null;

    public Repository() {
    }

    public Repository(@NotNull Repository r) {
        r.forEach(p -> this.add((Pathway) p.clone()));
        defaultPathway = r.defaultPathway;
    }

    /**
     * Add decoy pathways to the repository
     *
     * @param rngSeed random number generator seed
     */
    public void addDecoys(long rngSeed) {
        addDecoys(new Random(rngSeed), false);
    }

    /**
     * Add decoy pathways to the repository
     *
     * @param rng      random number generator
     * @param parallel true, if decoys should be built in parallel
     */
    public void addDecoys(Random rng, boolean parallel) {
        Map<String, Node> idToGenes = new HashMap<>();
        ArrayList<String> allGenes = new ArrayList<>();
        this.pathways.values().stream().map(Pathway::graph).flatMap(g -> g.nodes().values().stream()).forEach(n -> {
            var id = n.id();
            if (!idToGenes.containsKey(id)) {
                idToGenes.put(id, n);
                allGenes.add(id);
            }
        });
        DecoyBuilder.getInstance().buildAllDecoys(this, allGenes, idToGenes, rng, parallel);
    }

    /**
     * Check if a pathway is contained in the repository
     *
     * @param p pathway
     * @return true if the pathway is contained in the repository
     */
    public boolean contains(@NotNull Pathway p) {
        return pathways.containsKey(p.id());
    }

    /**
     * Check if a pathway is contained in the repository
     *
     * @param p pathway id
     * @return true if the pathway is contained in the repository
     */
    public boolean contains(String p) {
        return pathways.containsKey(p);
    }

    /**
     * Get a pathway by its id
     *
     * @param pathwayId pathway id
     * @return the pathway or null if it is not contained in the repository
     */
    public Pathway get(String pathwayId) {
        return pathways.get(pathwayId);
    }

    /**
     * Get the default pathway if it is set
     *
     * @return the default pathway or null if it is not set
     */
    public Pathway get() {
        return get(defaultPathway);
    }

    /**
     * Get pathways by their category
     *
     * @param category category
     * @return a list of pathways
     */
    public List<Pathway> getPathwaysByCategory(String category) {
        List<String> l = pathwaysByCategory.get(category);
        if (l == null) return null;
        return l.stream().map(this::get).collect(Collectors.toList());
    }

    /**
     * Get pathways by their category
     *
     * @param category a list of categories
     * @return a list of pathways
     */
    public List<Pathway> getPathwaysByCategory(@NotNull List<String> category) {
        return category.stream().flatMap(c -> getPathwaysByCategory(c).stream()).distinct().collect(Collectors.toList());
    }

    /**
     * Get pathway ids by their category
     *
     * @param category category
     * @return a list of pathway ids
     */
    public List<String> getPathwayIdsByCategory(String category) {
        return pathwaysByCategory.get(category);
    }

    /**
     * Get pathway ids by their category
     *
     * @param category a list of categories
     * @return a list of pathway ids
     */
    public List<String> getPathwayIdsByCategory(@NotNull List<String> category) {
        return category.stream().flatMap(c -> getPathwayIdsByCategory(c).stream()).distinct().collect(Collectors.toList());
    }

    /**
     * Get all pathways containing a node
     *
     * @param nodeId node id
     * @return a list of pathways
     */
    public List<String> getPathwaysByNodeId(String nodeId) {
        return pathways.values().stream().filter(pathway -> pathway.graph().hasNode(nodeId)).map(Pathway::id).collect(Collectors.toList());
    }

    /**
     * Get all categories
     *
     * @return a list of categories
     */
    public List<String> getCategories() {
        return new ArrayList<>(pathwaysByCategory.keySet());
    }

    /**
     * Add a virtual pathway
     *
     * @param from  source pathway
     * @param id    virtual pathway id
     * @param name  virtual pathway name
     * @param edges list of edges
     */
    public void addVirtualPathway(Pathway from, String id, String name, List<Pair<String, String>> edges) {
        virtualPathways.put(id, new VirtualPathway(id, name, edges, from));
    }

    /**
     * Get a set of virtual pathway ids
     *
     * @return a set of virtual pathway ids
     */
    public Set<String> getVirtualPathways() {
        return virtualPathways.keySet();
    }

    /**
     * Check if a virtual pathway is contained in the repository
     *
     * @param virtualPathwayId virtual pathway id
     * @return true if the virtual pathway is contained in the repository
     */
    public boolean hasVirtualPathway(String virtualPathwayId) {
        return virtualPathways.containsKey(virtualPathwayId);
    }

    /**
     * Get a virtual pathway by its id
     *
     * @param virtualPathwayId virtual pathway id
     * @return the virtual pathway or null if it is not contained in the repository
     */
    public VirtualPathway getVirtualPathway(String virtualPathwayId) {
        return virtualPathways.get(virtualPathwayId);
    }

    /**
     * Get all pathways
     *
     * @return a list of pathways
     */
    public List<Pathway> pathways() {
        return new ArrayList<>(pathways.values());
    }

    /**
     * Remove a node from all pathways and virtual pathways in the repository
     *
     * @param id node id
     * @return true if the node was removed from all pathways
     */
    public boolean removeNode(String id) {
        boolean res = true;
        for (var p : this) {
            var g = p.graph();
            if (g.hasNode(id)) {
                res = res && g.removeNode(id);
            }
        }
        this.virtualPathways.values().forEach(v -> v.removeNode(id));
        return res;
    }

    /**
     * Remove a node from all pathways and virtual pathways in the repository
     *
     * @param n node object
     * @return true if the node was removed from all pathways
     */
    public boolean removeNode(@NotNull Node n) {
        return remove(n.id());
    }

    /**
     * The number of pathways in the repository
     *
     * @return the number of pathways
     */
    @Override
    public int size() {
        return pathways.size();
    }

    /**
     * Check if the repository is empty
     *
     * @return true, if the repository is empty
     */
    @Override
    public boolean isEmpty() {
        return pathways.isEmpty();
    }

    /**
     * Check if a pathway is contained in the repository
     *
     * @param o element whose presence in this collection is to be tested
     * @return true, if the pathway is contained in the repository
     */
    @Override
    public boolean contains(Object o) {
        return o instanceof Pathway p && contains(p);
    }

    /**
     * Iterator over all pathways in the repository
     *
     * @return an iterator over all pathways in the repository
     */
    @Override
    public Iterator<Pathway> iterator() {
        return pathways.values().iterator();
    }

    /**
     * Get an array of all pathways in the repository
     *
     * @return an array of all pathways in the repository
     */
    @Override
    public Object @NotNull [] toArray() {
        return pathways.values().toArray();
    }

    /**
     * Get an array of all pathways in the repository
     *
     * @param a   the array into which the elements of this collection are to be stored, if it is big enough;
     *            otherwise, a new array of the same runtime type is allocated for this purpose.
     * @param <T> the type of the array to contain the collection
     * @return an array of all pathways in the repository
     */
    @Override
    public <T> T @NotNull [] toArray(T @NotNull [] a) {
        return pathways.values().toArray(a);
    }

    /**
     * Add a pathway to the repository
     *
     * @param p pathway
     * @return true, if the pathway was added to the repository
     */
    @Override
    public boolean add(@NotNull Pathway p) {
        if (pathways.containsKey(p.id())) return false;
        pathways.put(p.id(), p);
        p.categories().forEach(s -> {
            if (!pathwaysByCategory.containsKey(s)) {
                pathwaysByCategory.put(s, new ArrayList<>());
            }
            pathwaysByCategory.get(s).add(p.id());
        });
        return true;
    }

    /**
     * Remove a pathway from the repository
     *
     * @param o pathway
     * @return true, if the pathway was removed from the repository
     */
    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Pathway p)) return false;
        p.categories().forEach(s -> this.pathwaysByCategory.get(s).remove(p.id()));
        return (pathways.remove(p.id()) != null);
    }

    /**
     * Check if all pathways in a collection are contained in the repository
     *
     * @param c collection of pathways
     * @return true, if all pathways in the collection are contained in the repository
     */
    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return pathways.values().containsAll(c);
    }

    /**
     * Add all pathways in a collection to the repository
     *
     * @param c collection of pathways
     * @return true, if all pathways in the collection were added to the repository
     */
    @Override
    public boolean addAll(@NotNull Collection<? extends Pathway> c) {
        c.forEach(this::add);
        return true;
    }

    /**
     * Unsupported operation
     *
     * @param c ignored
     * @return nothing
     * @throws RuntimeException always
     */
    @Override
    @Deprecated
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new RuntimeException("Unsupported operation");
    }

    /**
     * Unsupported operation
     *
     * @param c ignored
     * @return nothing
     * @throws RuntimeException always
     */
    @Override
    @Deprecated
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new RuntimeException("Unsupported operation");
    }

    /**
     * Remove all pathways from the repository
     */
    @Override
    public void clear() {
        pathways.clear();
    }

    /**
     * Clone the object
     *
     * @return my clone
     */
    public Object clone() {
        try {
            return new Repository((Repository) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Get the hash code of the repository
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(pathways, virtualPathways);
    }

    /**
     * Given a repository filter, return the list of pathways that match the filter
     *
     * @param r repository filter
     * @return list of pathways that match the filter
     */
    private @NotNull List<Pathway> processFilter(@Nullable RepositoryFilter r) {
        var selectedPathways = new ArrayList<Pathway>();
        if (r == null) {
            selectedPathways.addAll(pathways.values());
        } else {
            if (r.includedCategories != null) {
                selectedPathways.addAll(getPathwaysByCategory(r.includedCategories));
            } else {
                selectedPathways.addAll(pathways.values());
            }
            if (r.excludedCategories != null) {
                selectedPathways.removeAll(getPathwaysByCategory(r.excludedCategories));
            }
            if (r.includedPathways != null) {
                r.includedPathways.stream().map(this::get).filter(Objects::nonNull).forEach(selectedPathways::add);
            }
            if (r.excludedPathways != null) {
                r.excludedPathways.stream().map(this::get).filter(Objects::nonNull).forEach(selectedPathways::remove);
            }
        }
        var decoys = DecoyBuilder.listDecoysWithinRepository(this);
        if (decoys != null && decoys.size() > 0) {
            selectedPathways.removeIf(p -> decoys.contains(p.id()));
        }
        return selectedPathways;
    }

    /**
     * Build a metapathway from the repository
     *
     * @param filters                pathway filters
     * @param disablePriority        disable priority
     * @param keepAllVirtualPathways keep all virtual pathways (even if they were filtered out)
     * @return the metapathway
     */
    public Repository buildMetapathway(RepositoryFilter filters, boolean disablePriority, boolean keepAllVirtualPathways) {
        var selectedPathways = processFilter(filters);
        var metapathwayRepository = new Repository();
        var metapathwayGraph = new Graph();
        var metapathway = new Pathway("metapathway", "Metapathway", metapathwayGraph);
        metapathway.setHidden(true);
        metapathwayRepository.add(metapathway);
        metapathwayRepository.defaultPathway = "metapathway";
        for (var p : selectedPathways) {
            var virtualEdges = metapathwayGraph.mergeWith(p.graph(), filters.nodeFilters, disablePriority);
            if (keepAllVirtualPathways || !virtualEdges.isEmpty()) {
                metapathwayRepository.addVirtualPathway(metapathway, p.id(), p.name(), virtualEdges);
            }
        }
        if (keepAllVirtualPathways) {
            for (var p : this) {
                if (metapathwayRepository.hasVirtualPathway(p.id())) continue;
                var virtualEdges = p.graph().edgesStream()
                        .filter(e -> metapathwayGraph.hasNode(e.source()) && metapathwayGraph.hasNode(e.target()))
                        .map(e -> Pair.of(e.source().id(), e.target().id()))
                        .toList();
                metapathwayRepository.addVirtualPathway(metapathway, p.id(), p.name(), virtualEdges);
            }
        }
        for (var vp : virtualPathways.values()) {
            var newEdges = new ArrayList<>(vp.edges);
            newEdges.removeIf(e -> !metapathwayGraph.hasNode(e.left()) || !metapathwayGraph.hasNode(e.right()));
            metapathwayRepository.addVirtualPathway(metapathway, vp.id(), vp.name(), newEdges);
        }
        return metapathwayRepository;
    }

    /**
     * A record to store the filters applied to a repository to build a metapathway
     * First we take all the pathways that are in the included categories and not in the excluded categories.
     * Then we remove the pathways that are in the excluded pathways and add the ones that are in the included pathways.
     * Node filters are applied to the resulting pathways.
     * Nodes matching any of the node filters are removed.
     *
     * @param includedCategories categories to include
     * @param excludedCategories categories to exclude
     * @param includedPathways   pathways to include
     * @param excludedPathways   pathways to exclude
     * @param nodeFilters        node filters
     */
    public record RepositoryFilter(
            @Nullable List<String> includedCategories,
            @Nullable List<String> excludedCategories,
            @Nullable List<String> includedPathways,
            @Nullable List<String> excludedPathways,
            @Nullable Pattern[] nodeFilters
    ) {
    }

    /**
     * A virtual pathway is a subset of a pathway that is contained in the repository.
     *
     * @param id     id of the virtual pathway
     * @param name   name of the virtual pathway
     * @param edges  list of edges in the virtual pathway
     * @param source source pathway
     */
    public record VirtualPathway(String id, String name, List<Pair<String, String>> edges, Pathway source) {
        @NotNull
        public List<Node> nodes() {
            var graph = source.graph();
            return edges.stream().flatMap(p -> Stream.of(p.left(), p.right())).distinct().map(graph::node).toList();
        }

        public void removeNode(String id) {
            edges.removeIf(e -> e.left().equals(id) || e.right().equals(id));
        }
    }

    @Override
    public String toString() {
        return "Repository{" +
                "pathways=" + pathways +
                ", virtualPathways=" + virtualPathways +
                '}';
    }
}
