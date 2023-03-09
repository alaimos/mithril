package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
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
    private final Map<String, List<Pair<String, String>>> virtualPathwaysEdges = new HashMap<>();
    private final Map<String, String> virtualPathwaysName = new HashMap<>();
    private final Map<String, Pathway> virtualPathwaysSource = new HashMap<>();

    public Repository() {
    }

    public Repository(@NotNull Repository r) {
        r.forEach(p -> this.add((Pathway) p.clone()));
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
        this.pathways.values().stream()
                .filter(Pathway::hasGraph)
                .map(Pathway::graph)
                .flatMap(g -> g.nodes().values().stream())
                .forEach(n -> {
                    var id = n.id();
                    if (!idToGenes.containsKey(id)) {
                        idToGenes.put(id, n);
                        allGenes.add(id);
                    }
                });
        DecoyBuilder.getInstance().buildAllDecoys(this, allGenes, idToGenes, rng, parallel);
    }

    public boolean contains(@NotNull Pathway p) {
        return pathways.containsKey(p.id());
    }

    public boolean contains(String p) {
        return pathways.containsKey(p);
    }

    public Pathway get(String pathwayId) {
        return pathways.get(pathwayId);
    }


    public List<Pathway> getPathwaysByCategory(String category) {
        List<String> l = pathwaysByCategory.get(category);
        if (l == null) return null;
        return l.stream().map(this::get).collect(Collectors.toList());
    }

    public List<Pathway> getPathwaysByCategory(@NotNull List<String> category) {
        return category.stream().flatMap(c -> getPathwaysByCategory(c).stream()).distinct().collect(Collectors.toList());
    }

    public List<String> getPathwayIdsByCategory(String category) {
        return pathwaysByCategory.get(category);
    }

    public List<String> getPathwayIdsByCategory(@NotNull List<String> category) {
        return category.stream().flatMap(c -> getPathwayIdsByCategory(c).stream()).distinct().collect(Collectors.toList());
    }

    public List<String> getPathwaysByNodeId(String nodeId) {
        return pathways.values().stream().filter(pathway -> pathway.hasGraph() && pathway.graph().hasNode(nodeId))
                .map(Pathway::id).collect(Collectors.toList());
    }

    public List<String> getCategories() {
        return new ArrayList<>(pathwaysByCategory.keySet());
    }

    public Pathway getDefaultVirtualSource() {
        return null;
    }

    public Repository addVirtualPathway(Pathway from, String id, List<Pair<String, String>> edges) {
        virtualPathwaysEdges.put(id, edges);
        virtualPathwaysSource.put(id, from);
        return this;
    }

    public Set<String> getVirtualPathways() {
        return virtualPathwaysEdges.keySet();
    }

    public boolean hasVirtualPathway(String virtualPathwayId) {
        return virtualPathwaysEdges.containsKey(virtualPathwayId);
    }

    public void setVirtualPathwayName(String virtualPathwayId, String name) {
        if (this.hasVirtualPathway(virtualPathwayId)) {
            virtualPathwaysName.put(virtualPathwayId, name);
        }
    }

    public String getVirtualPathwayName(String virtualPathwayId) {
        return virtualPathwaysName.getOrDefault(virtualPathwayId, virtualPathwayId);
    }

    @NotNull
    public List<Node> getVirtualPathwayNodes(String virtualPathwayId) {
        var source = getSourceOfVirtualPathway(virtualPathwayId).graph();
        return virtualPathwaysEdges.get(virtualPathwayId).stream()
                .flatMap(p -> Stream.of(p.getLeft(), p.getRight()))
                .distinct()
                .map(source::getNode)
                .collect(Collectors.toList());
    }

    public List<Edge> getVirtualPathwayEdges(String virtualPathwayId) {
        var source = getSourceOfVirtualPathway(virtualPathwayId).graph();
        return virtualPathwaysEdges.get(virtualPathwayId).stream()
                .map(p -> source.getEdge(p.getLeft(), p.getRight()))
                .collect(Collectors.toList());
    }

    public List<Pair<String, String>> getVirtualPathwayEdgePairs(String virtualPathwayId) {
        return virtualPathwaysEdges.get(virtualPathwayId);
    }

    public Pathway getSourceOfVirtualPathway(String virtualPathwayId) {
        return virtualPathwaysSource.get(virtualPathwayId);
    }

    public List<Pathway> getPathways() {
        return new ArrayList<>(pathways.values());
    }

    public boolean removeNode(String id) {
        boolean res = true;
        for (var p : this) {
            if (p.hasGraph()) {
                var g = p.graph();
                if (g.hasNode(id)) {
                    res = res && g.removeNode(id);
                }
            }
        }
        this.virtualPathwaysEdges.forEach((s, l) -> l.removeIf(e -> e.getLeft().equals(id) || e.getRight().equals(id)));
        return res;
    }

    public boolean removeNode(@NotNull Node n) {
        return remove(n.id());
    }


    @Override
    public int size() {
        return pathways.size();
    }

    @Override
    public boolean isEmpty() {
        return pathways.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof Pathway p && contains(p);
    }

    @Override
    public Iterator<Pathway> iterator() {
        return pathways.values().iterator();
    }

    @Override
    public Object @NotNull [] toArray() {
        return pathways.values().toArray();
    }

    @Override
    public <T> T @NotNull [] toArray(T @NotNull [] a) {
        return pathways.values().toArray(a);
    }

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

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Pathway p)) return false;
        p.categories().forEach(s -> this.pathwaysByCategory.get(s).remove(p.id()));
        return (pathways.remove(p.id()) != null);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return pathways.values().containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Pathway> c) {
        c.forEach(this::add);
        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new RuntimeException("Unsupported operation");
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new RuntimeException("Unsupported operation");
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(pathways, virtualPathwaysName, virtualPathwaysEdges, virtualPathwaysSource);
    }
}
