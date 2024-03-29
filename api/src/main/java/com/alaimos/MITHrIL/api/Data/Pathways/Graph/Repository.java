package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Contract;
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
        for (var virtualPathway : r.virtualPathways.values()) {
            var id = virtualPathway.id();
            var source = get(virtualPathway.source.id());
            var edges = new ArrayList<Pair<String, String>>();
            for (var edge : virtualPathway.edges) {
                edges.add(Pair.of(edge.first(), edge.second()));
            }
            this.virtualPathways.put(id, new VirtualPathway(id, virtualPathway.name, edges, source));
        }
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
        return category.stream()
                       .flatMap(c -> getPathwaysByCategory(c).stream())
                       .distinct()
                       .collect(Collectors.toList());
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
        return category.stream()
                       .flatMap(c -> getPathwayIdsByCategory(c).stream())
                       .distinct()
                       .collect(Collectors.toList());
    }

    /**
     * Get all pathways containing a node
     *
     * @param nodeId node id
     * @return a list of pathways
     */
    public List<String> getPathwaysByNodeId(String nodeId) {
        return pathways.values()
                       .stream()
                       .filter(pathway -> pathway.graph().hasNode(nodeId))
                       .map(Pathway::id)
                       .collect(Collectors.toList());
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
    public Collection<VirtualPathway> virtualPathways() {
        return virtualPathways.values();
    }

    /**
     * Check if a virtual pathway is contained in the repository
     *
     * @param id virtual pathway id
     * @return true if the virtual pathway is contained in the repository
     */
    public boolean hasVirtualPathway(String id) {
        return virtualPathways.containsKey(id);
    }

    /**
     * Get a virtual pathway by its id
     *
     * @param id virtual pathway id
     * @return the virtual pathway or null if it is not contained in the repository
     */
    public VirtualPathway virtualPathway(String id) {
        return virtualPathways.get(id);
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
     */
    public void removeNode(String id) {
        for (var p : this) {
            var g = p.graph();
            if (g.hasNode(id)) {
                g.removeNode(id);
            }
        }
        this.virtualPathways.values().forEach(v -> v.removeNode(id));
    }

    /**
     * Remove a node from all pathways and virtual pathways in the repository
     *
     * @param n node object
     */
    public void removeNode(@NotNull Node n) {
        removeNode(n.id());
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
     * @param a   the array into which the elements of this collection are to be stored, if it is big enough; otherwise,
     *            a new array of the same runtime type is allocated for this purpose.
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
     * Create a bidirectional index between a contiguous sequence integers (1 to N, where N is the total number of
     * virtual pathways) and pathway ids. This method works only for a metapathway repository.
     *
     * @return A pair of maps, the first maps from index to id, the second from id to index
     */
    public Pair<Int2ObjectMap<String>, Object2IntOpenHashMap<String>> indexMetapathway() {
        if (defaultPathway == null || !defaultPathway.equals("metapathway")) {
            return null;
        }
        var index2Id = new Int2ObjectOpenHashMap<String>();
        var id2Index = new Object2IntOpenHashMap<String>();
        int i = 0;
        for (var p : this.virtualPathways.values()) {
            index2Id.put(i, p.id());
            id2Index.put(p.id(), i);
            i++;
        }
        return Pair.of(index2Id, id2Index);
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
     * Extend all pathways contained in this repository with the nodes and edges of another graph. This is an iterative
     * process, and it is different from the metapathway build operation. First, all the edges with both source and
     * target nodes in this graph are added. Then, until no more edges can be added, all the edges with at least one
     * node in this graph are added. The process stops when the set of edges to be added is empty, or when the number of
     * edges in the graph does not change.
     *
     * @param other The other graph
     */
    public void extendWith(@NotNull Graph other) {
        pathways.forEach((s, p) -> p.graph().extendWith(other));
        virtualPathways.forEach((s, p) -> p.extendWith(other));
    }

    /**
     * Build a metapathway from the repository
     *
     * @param filters                pathway filters
     * @param disablePriority        disable priority
     * @param keepAllVirtualPathways keep all virtual pathways (even if they were filtered out)
     * @return the metapathway
     */
    public Repository buildMetapathway(
            RepositoryFilter filters, boolean disablePriority, boolean keepAllVirtualPathways
    ) {
        var selectedPathways = processFilter(filters);
        var metapathwayRepository = new Repository();
        var metapathwayGraph = new Graph();
        var metapathway = new Pathway("metapathway", "Metapathway", metapathwayGraph);
        var nodeFilters = (filters != null) ? filters.nodeFilters : null;
        metapathway.setHidden(true);
        metapathwayRepository.add(metapathway);
        metapathwayRepository.defaultPathway = "metapathway";
        for (var p : selectedPathways) {
            var virtualEdges = metapathwayGraph.mergeWith(p.graph(), nodeFilters, disablePriority);
            if (keepAllVirtualPathways || !virtualEdges.isEmpty()) {
                metapathwayRepository.addVirtualPathway(metapathway, p.id(), p.name(), virtualEdges);
            }
        }
        if (keepAllVirtualPathways) {
            for (var p : this) {
                if (metapathwayRepository.hasVirtualPathway(p.id())) continue;
                var virtualEdges = p.graph().edgesStream()
                                    .filter(e -> metapathwayGraph.hasNode(e.source()) && metapathwayGraph.hasNode(
                                            e.target()))
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
     * Build a repository where all the pathways and virtual pathways are inverted. An inverted pathway is a pathway
     * where all the edge directions are inverted (A->B becomes B->A).
     *
     * @return the inverted repository
     */
    public Repository inverted() {
        var clonedRepository = (Repository) this.clone();
        for (var pathway : clonedRepository) {
            pathway.graph().invert();
        }
        clonedRepository.virtualPathways.clear();
        for (var virtualPathway : virtualPathways.values()) {
            var invertedVirtualPathway = virtualPathway.invert();
            clonedRepository.virtualPathways.put(invertedVirtualPathway.id, invertedVirtualPathway);
        }
        return clonedRepository;
    }

    @Override
    public String toString() {
        return "Repository{" +
                "pathways=" + pathways +
                ", virtualPathways=" + virtualPathways +
                '}';
    }

    /**
     * A record to store the filters applied to a repository to build a metapathway First we take all the pathways that
     * are in the included categories and not in the excluded categories. Then we remove the pathways that are in the
     * excluded pathways and add the ones that are in the included pathways. Node filters are applied to the resulting
     * pathways. Nodes matching any of the node filters are removed.
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

        @Contract(" -> new")
        public @NotNull VirtualPathway invert() {
            var invertedEdges = new ArrayList<Pair<String, String>>();
            for (var edge : edges) {
                invertedEdges.add(Pair.of(edge.right(), edge.left()));
            }
            return new VirtualPathway(id, name, invertedEdges, source);
        }

        private @NotNull Map<String, Set<String>> getEdgesMap() {
            var edgesMap = new HashMap<String, Set<String>>();
            for (var e : edges) {
                var source = e.left();
                var target = e.right();
                if (!edgesMap.containsKey(source)) {
                    edgesMap.put(source, new HashSet<>());
                }
                edgesMap.get(source).add(target);
            }
            return edgesMap;
        }

        public void extendWith(@NotNull Graph other) {
            var previousCountEdges = -1;
            var currentCountEdges = edges.size();
            var edgesMap = getEdgesMap();
            var nodes = nodes().stream().map(Node::id).collect(Collectors.toSet());
            var remainingEdges = other.edgesStream().collect(Collectors.toSet());
            while (previousCountEdges != currentCountEdges && !remainingEdges.isEmpty()) {
                previousCountEdges = currentCountEdges;
                var newRemainingEdges = new HashSet<Edge>();
                for (var e : remainingEdges) {
                    var source = e.source();
                    var target = e.target();
                    var sourceId = source.id();
                    var targetId = target.id();
                    if (edgesMap.containsKey(sourceId) && edgesMap.get(sourceId).contains(targetId)) {
                        continue;
                    }
                    if (nodes.contains(targetId)) {
                        if (!edgesMap.containsKey(sourceId)) {
                            edgesMap.put(sourceId, new HashSet<>());
                        }
                        edges.add(Pair.of(sourceId, targetId));
                        edgesMap.get(sourceId).add(targetId);
                        nodes.add(targetId);
                        currentCountEdges++;
                    } else {
                        newRemainingEdges.add(e);
                    }
                }
                remainingEdges = newRemainingEdges;
            }
        }
    }
}
