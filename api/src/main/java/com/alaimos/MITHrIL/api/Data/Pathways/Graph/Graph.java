package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class Graph implements Serializable, Cloneable, Iterable<Node> {

    @Serial
    private static final long serialVersionUID = -8825355851517533720L;
    private final Object2IntMap<String> nodeIndex = new Object2IntOpenHashMap<>();
    private final Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<String> aliases = new Object2IntOpenHashMap<>();
    private final Int2ObjectMap<Int2ObjectMap<Edge>> outEdges = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Int2ObjectMap<Edge>> inEdges = new Int2ObjectOpenHashMap<>();
    private final List<String> endpoints = new ArrayList<>();
    private Integer hashCode = null;

    public Graph() {
    }

    @SuppressWarnings("unchecked")
    public Graph(@NotNull Graph g) {
        // TODO: implement this
//        g.nodes.forEach((k, v) -> nodes.put(k, (Node) v.clone()));
//        g.outEdges.forEach((k, v) -> outEdges.put(k, new Int2ObjectOpenHashMap<>(v)));
//        g.inEdges.forEach((k, v) -> inEdges.put(k, new Int2ObjectOpenHashMap<>(v)));
//        g.nodeIndex.forEach((k, v) -> nodeIndex.put(k, v));
//        g.aliases.forEach((k, v) -> aliases.put(k, v));
//        clone.nodes = new HashMap<>();
//        clone.outEdges = new HashMap<>();
//        clone.inEdges = new HashMap<>();
//        nodes.forEach((s, n) -> clone.addNode((Node) n.clone()));
//        outEdges.forEach((s, edges) -> edges.forEach((e, edge) -> {
//            Edge edgeC = (Edge) edge.clone();
//            edgeC.setStart(clone.getNode(s)).setEnd(clone.getNode(e));
//            clone.addEdge(edgeC);
//        }));
//        clone.endpoints = (ArrayList<String>) endpoints.clone();
//        clone.weightComputation = this.weightComputation; //Weight computation in never cloned
//        clone.owner = owner;
//        return clone;

        endpoints.addAll(g.endpoints);
    }

    public boolean isEmpty() {
        return this.countNodes() == 0;
    }

    public void addNode(@NotNull Node n) {
        var idx = n.hashCode();
        if (!nodes.containsKey(idx)) {
            nodes.put(idx, n);
            outEdges.put(idx, new Int2ObjectOpenHashMap<>());
            inEdges.put(idx, new Int2ObjectOpenHashMap<>());
            var id = n.id();
            aliases.put(id, idx);
            aliases.put(n.name(), idx);
            for (var a : n.aliases()) {
                aliases.put(a, idx);
            }
            nodeIndex.put(id, idx);
            hashCode = null;
        }
    }

    public Node getNode(String id) {
        return nodes.get(nodeIndex.getInt(id));
    }

    public Node findNode(String needle) {
        if (nodeIndex.containsKey(needle)) return nodes.get(nodeIndex.getInt(needle));
        if (aliases.containsKey(needle)) return nodes.get(aliases.getInt(needle));
        return null;
    }

    public boolean removeNode(Node n) {
        if (n == null) return false;
        var idx = n.hashCode();
        if (nodes.containsKey(idx)) {
            nodes.remove(idx);
            outEdges.remove(idx);
            inEdges.remove(idx);
            outEdges.forEach((s, m) -> m.remove(idx));
            inEdges.forEach((s, m) -> m.remove(idx));
            aliases.removeInt(n.id());
            aliases.removeInt(n.name());
            for (var a : n.aliases()) {
                aliases.removeInt(a);
            }
            nodeIndex.removeInt(n.id());
            hashCode = null;
            return true;
        }
        return false;
    }

    public boolean removeNode(String id) {
        return removeNode(getNode(id));
    }

    public boolean hasNode(@NotNull Node n) {
        return nodes.containsKey(n.hashCode());
    }

    public boolean hasNode(int id) {
        return nodes.containsKey(id);
    }

    public boolean hasNode(String id) {
        return nodes.containsKey(nodeIndex.getInt(id));
    }

    public List<String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(@NotNull List<String> endpoints) {
        this.endpoints.clear();
        endpoints.stream().filter(this::hasNode).forEachOrdered(this.endpoints::add);
        hashCode = null;
    }

    public int inDegree(@NotNull Node n) {
        return Optional.of(inEdges.get(n.hashCode())).map(Map::size).orElse(-1);
    }

    public int outDegree(@NotNull Node n) {
        return Optional.of(outEdges.get(n.hashCode())).map(Map::size).orElse(-1);
    }

    public Optional<Stream<Node>> ingoingNodesStream(@NotNull Node n) {
        return Optional.of(inEdges.get(n.hashCode())).map(m -> m.values().stream().map(Edge::source));
    }

    public Optional<Stream<Node>> outgoingNodesStream(@NotNull Node n) {
        return Optional.of(outEdges.get(n.hashCode())).map(m -> m.values().stream().map(Edge::target));
    }

    public Int2ObjectMap<Node> nodes() {
        return nodes;
    }

    public Stream<Node> getNodesStream() {
        return nodes.values().stream();
    }

    public void addEdge(@NotNull Edge e, boolean merge, boolean usePriority) {
        if (!hasNode(e.source())) {
            addNode(e.source());
        }
        if (!hasNode(e.target())) {
            addNode(e.target());
        }
        if (!hasEdge(e)) {
            var s = e.source().hashCode();
            var t = e.target().hashCode();
            outEdges.get(s).put(t, e);
            inEdges.get(t).put(s, e);
        } else {
            var existingEdge = getEdge(e.source(), e.target());
            if (merge && !existingEdge.equals(e)) {
                // existingEdge.merge(e, usePriority); // TODO
                // e.getDescriptions().stream().filter(d -> !edge.getDescriptions().contains(d)).forEachOrdered(edge::addDescription);
            }
        }
        hashCode = null;
    }

    public void addEdge(@NotNull Edge e, boolean usePriority) {
        addEdge(e, true, usePriority);
    }

    public void addEdge(@NotNull Edge e) {
        addEdge(e, true, false);
    }

    public Edge getEdge(@NotNull Node start, @NotNull Node end) {
        return getEdge(start.hashCode(), end.hashCode());
    }

    public Edge getEdge(int start, int end) {
        return outEdges.get(start).get(end);
    }

    public Edge getEdge(String startId, String endId) {
        if (!hasNode(startId) || !hasNode(endId)) return null;
        return getEdge(nodeIndex.getInt(startId), nodeIndex.getInt(endId));
    }

    public boolean hasEdge(@NotNull Edge e) {
        return outEdges.containsKey(e.source().hashCode()) && outEdges.get(e.source().hashCode()).containsKey(e.target().hashCode());
    }

    public boolean hasEdge(@NotNull Node start, @NotNull Node end) {
        return getEdge(start, end) != null;
    }

    public boolean hasEdge(String startId, String endId) {
        if (!hasNode(startId) || !hasNode(endId)) return false;
        return getEdge(startId, endId) != null;
    }

    public Int2ObjectMap<Int2ObjectMap<Edge>> getEdges() {
        return outEdges;
    }

    public Stream<Edge> getEdgesStream() {
        return outEdges.values().stream().flatMap(e -> e.values().stream());
    }

    public int countNodes() {
        return nodes.size();
    }

    public int countEdges() {
        return outEdges.values().stream().mapToInt(Int2ObjectMap::size).sum();
    }

    /**
     * This function implements all the logic needed for a traversal in this graph
     *
     * @param consumer      A method which runs a traversal in a specific direction
     * @param results       A collection of nodes visited by the traversal
     * @param currentNode   The node where the traversal will start
     * @param markTraversal Are nodes marked so that they are visited only once?
     */
    protected void traversalLogic(@NotNull BiConsumer<Node, Function<Node, TraversalAction>> consumer, @NotNull Collection<Node> results, @NotNull Node currentNode, boolean markTraversal) {
        final HashSet<Node> marked = new HashSet<>();
        consumer.accept(currentNode, o -> {
            if (o.equals(currentNode)) {
                if (markTraversal && !marked.add(currentNode)) {
                    return TraversalAction.PRUNE;
                }
                return TraversalAction.CONTINUE;
            }
            if (!markTraversal) {
                if (results.add(o)) {
                    return TraversalAction.CONTINUE;
                } else {
                    return TraversalAction.PRUNE;
                }
            } else {
                if (marked.add(o) && results.add(o)) {
                    return TraversalAction.CONTINUE;
                } else {
                    return TraversalAction.PRUNE;
                }
            }
        });
    }

    public void runUpstream(Node currentNode, Function<Node, TraversalAction> action) {
        var traversalGuide = new Stack<Node>();
        traversalGuide.push(currentNode);
        while (!traversalGuide.isEmpty()) {
            var ni = traversalGuide.pop();
            var r = action.apply(ni);
            if (r == TraversalAction.STOP) {
                break;
            } else if (r == TraversalAction.CONTINUE) {
                if (inDegree(ni) > 0) {
                    for (var e : inEdges.get(ni.hashCode()).values()) {
                        traversalGuide.push(e.source());
                    }
                }
            }
        }
    }

    public void traverseUpstream(Collection<Node> results, Node currentNode, boolean markTraversal) {
        traversalLogic(this::runUpstream, results, currentNode, markTraversal);
    }

    public void runDownstream(Node currentNode, Function<Node, TraversalAction> action) {
        var traversalGuide = new Stack<Node>();
        traversalGuide.push(currentNode);
        while (!traversalGuide.isEmpty()) {
            var ni = traversalGuide.pop();
            var r = action.apply(ni);
            if (r == TraversalAction.STOP) {
                break;
            } else if (r == TraversalAction.CONTINUE) {
                if (outDegree(ni) > 0) {
                    for (var e : outEdges.get(ni.hashCode()).values()) {
                        traversalGuide.push(e.target());
                    }
                }
            }
        }
    }

    public void traverseDownstream(Collection<Node> results, Node currentNode, boolean markTraversal) {
        traversalLogic(this::runDownstream, results, currentNode, markTraversal);
    }

    public List<Node> upstreamNodes(Node n) {
        HashSet<Node> result = new HashSet<>();
        traverseUpstream(result, n, true);
        return new ArrayList<>(result);
    }

    public List<Node> downstreamNodes(Node n) {
        HashSet<Node> result = new HashSet<>();
        traverseDownstream(result, n, true);
        return new ArrayList<>(result);
    }

    @NotNull
    @Override
    public Iterator<Node> iterator() {
        return this.nodes.values().iterator();
    }

    /**
     * Clone the object
     *
     * @return my clone
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            return new Graph((Graph) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Graph g)) return false;
        return hashCode() == g.hashCode() &&
                countNodes() == g.countNodes() &&
                countEdges() == g.countEdges() &&
                Objects.equals(nodes, g.nodes) &&
                Objects.equals(outEdges, g.outEdges) &&
                Objects.equals(inEdges, g.inEdges) &&
                Objects.equals(endpoints, g.endpoints);
    }

    @Override
    public int hashCode() {
        if (hashCode == null) {
            hashCode = Objects.hash(nodes, outEdges, inEdges, endpoints);
        }
        return hashCode;
    }

    public enum TraversalAction {
        CONTINUE,
        PRUNE,
        STOP
    }
}
