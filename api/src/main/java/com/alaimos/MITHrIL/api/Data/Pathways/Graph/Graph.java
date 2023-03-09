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
    private final Int2ObjectMap<Int2ObjectMap<Edge>> outgoingEdges = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Int2ObjectMap<Edge>> incomingEdges = new Int2ObjectOpenHashMap<>();
    private final List<String> endpoints = new ArrayList<>();
    private Integer hashCode = null;

    public Graph() {
    }

    @SuppressWarnings("unchecked")
    public Graph(@NotNull Graph g) {
        g.nodes.values().forEach(n -> addNode((Node) n.clone()));
        g.outgoingEdges.values().stream().flatMap(m -> m.values().stream()).forEach(e -> {
            Edge tmpEdge = (Edge) e.clone();
            Edge clonedEdge = new Edge(getNode(e.source()), getNode(e.target()), tmpEdge.details());
            addEdge(clonedEdge);
        });
        endpoints.addAll(g.endpoints);
    }

    /**
     * Add a node object to the graph. If the node is already present, it is not added again.
     *
     * @param n the node object
     */
    public void addNode(@NotNull Node n) {
        var idx = n.hashCode();
        if (!nodes.containsKey(idx)) {
            nodes.put(idx, n);
            outgoingEdges.put(idx, new Int2ObjectOpenHashMap<>());
            incomingEdges.put(idx, new Int2ObjectOpenHashMap<>());
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

    /**
     * Get a node by its id
     *
     * @param id the id
     * @return the node object. Null if not found.
     */
    public Node getNode(String id) {
        return nodes.get(nodeIndex.getInt(id));
    }

    /**
     * Get a node by its hash code
     *
     * @param hashCode the hash code
     * @return the node object. Null if not found.
     */
    public Node getNode(int hashCode) {
        return nodes.get(hashCode);
    }

    /**
     * Get a node object using another node object as reference
     *
     * @param n the reference node
     * @return the node object. Null if not found.
     */
    public Node getNode(@NotNull Node n) {
        return nodes.get(n.hashCode());
    }

    /**
     * Find a node in the graph using its id, name or aliases.
     * First, it tries to find the node by id.
     * Then, a search by name and aliases is performed.
     *
     * @param needle the id, name or alias
     * @return the node object. Null if not found.
     */
    public Node findNode(String needle) {
        if (nodeIndex.containsKey(needle)) return nodes.get(nodeIndex.getInt(needle));
        if (aliases.containsKey(needle)) return nodes.get(aliases.getInt(needle));
        return null;
    }

    /**
     * Remove a node from the graph
     *
     * @param n the node object to remove
     * @return true if the node was removed, false otherwise
     */
    public boolean removeNode(Node n) {
        if (n == null) return false;
        var idx = n.hashCode();
        if (nodes.containsKey(idx)) {
            nodes.remove(idx);
            outgoingEdges.remove(idx);
            incomingEdges.remove(idx);
            outgoingEdges.forEach((s, m) -> m.remove(idx));
            incomingEdges.forEach((s, m) -> m.remove(idx));
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

    /**
     * Remove a node from the graph
     *
     * @param id the id of the node to remove
     * @return true if the node was removed, false otherwise
     */
    public boolean removeNode(String id) {
        return removeNode(getNode(id));
    }

    /**
     * Check if a node is present in the graph
     *
     * @param n the node object
     * @return true if the node is present, false otherwise
     */
    public boolean hasNode(@NotNull Node n) {
        return nodes.containsKey(n.hashCode());
    }

    /**
     * Check if a node is present in the graph
     *
     * @param hashCode the hash code of the node
     * @return true if the node is present, false otherwise
     */
    public boolean hasNode(int hashCode) {
        return nodes.containsKey(hashCode);
    }

    /**
     * Check if a node is present in the graph
     *
     * @param id the id of the node
     * @return true if the node is present, false otherwise
     */
    public boolean hasNode(String id) {
        return nodes.containsKey(nodeIndex.getInt(id));
    }

    /**
     * Get all the endpoints of this graph
     *
     * @return a list of node ids
     */
    public List<String> getEndpoints() {
        return endpoints;
    }

    /**
     * Set the endpoints of this graph
     *
     * @param endpoints a list of node ids
     */
    public void setEndpoints(@NotNull List<String> endpoints) {
        this.endpoints.clear();
        endpoints.stream().filter(this::hasNode).forEachOrdered(this.endpoints::add);
        hashCode = null;
    }

    /**
     * Compute the in-degree of a node
     *
     * @param n the node
     * @return the in-degree of the node
     */
    public int inDegree(@NotNull Node n) {
        return Optional.of(incomingEdges.get(n.hashCode())).map(Map::size).orElse(-1);
    }

    /**
     * Compute the out-degree of a node
     *
     * @param n the node
     * @return the out-degree of the node
     */
    public int outDegree(@NotNull Node n) {
        return Optional.of(outgoingEdges.get(n.hashCode())).map(Map::size).orElse(-1);
    }

    /**
     * Create a stream of all the edges ingoing to a node.
     * The stream is wrapped in an Optional object to avoid NullPointerExceptions.
     *
     * @param n the node
     * @return an Optional object containing the stream of edges
     */
    public Optional<Stream<Node>> ingoingNodesStream(@NotNull Node n) {
        return Optional.of(incomingEdges.get(n.hashCode())).map(m -> m.values().stream().map(Edge::source));
    }

    /**
     * Create a stream of all the edges outgoing from a node.
     * The stream is wrapped in an Optional object to avoid NullPointerExceptions.
     *
     * @param n the node
     * @return an Optional object containing the stream of edges
     */
    public Optional<Stream<Node>> outgoingNodesStream(@NotNull Node n) {
        return Optional.of(outgoingEdges.get(n.hashCode())).map(m -> m.values().stream().map(Edge::target));
    }

    /**
     * Return a map of all the nodes in the graph.
     * The key is the hash code of the node.
     * The value is the node object.
     *
     * @return a map of nodes
     */
    public Int2ObjectMap<Node> nodes() {
        return nodes;
    }

    /**
     * Return a stream of all the nodes in the graph.
     *
     * @return a stream of nodes
     */
    public Stream<Node> getNodesStream() {
        return nodes.values().stream();
    }

    /**
     * Add an edge to the graph.
     *
     * @param e                      the edge to add
     * @param merge                  if true, the edge is merged with the existing edge if present
     * @param usePriority            if true, the edge subtype priority is used to keep only edge details with maximal priority
     * @param allowDuplicatedDetails if true, duplicates are allowed in the edge details
     */
    public void addEdge(@NotNull Edge e, boolean merge, boolean usePriority, boolean allowDuplicatedDetails) {
        if (!hasNode(e.source())) {
            addNode(e.source());
        }
        if (!hasNode(e.target())) {
            addNode(e.target());
        }
        if (!hasEdge(e)) {
            var s = e.source().hashCode();
            var t = e.target().hashCode();
            outgoingEdges.get(s).put(t, e);
            incomingEdges.get(t).put(s, e);
        } else {
            var existingEdge = getEdge(e.source(), e.target());
            if (merge && !existingEdge.equals(e)) {
                existingEdge.merge(e, usePriority, allowDuplicatedDetails);
            }
        }
        hashCode = null;
    }

    /**
     * Add an edge to the graph merging all the details with the existing one, if already present in the graph.
     *
     * @param e                      the edge to add
     * @param usePriority            if true, the edge subtype priority is used to keep only edge details with maximal priority
     * @param allowDuplicatedDetails if true, duplicates are kept in the edge details
     */
    public void addEdge(@NotNull Edge e, boolean usePriority, boolean allowDuplicatedDetails) {
        addEdge(e, true, usePriority, allowDuplicatedDetails);
    }

    /**
     * Add an edge to the graph merging all the details with the existing one, if already present in the graph.
     * No priority is used, and duplicates are removed from the details.
     *
     * @param e the edge to add
     */
    public void addEdge(@NotNull Edge e) {
        addEdge(e, true, false, false);
    }

    /**
     * Get an edge from the graph by its start and end nodes.
     *
     * @param start the start node
     * @param end   the end node
     * @return the edge, or null if not present
     */
    public Edge getEdge(@NotNull Node start, @NotNull Node end) {
        return getEdge(start.hashCode(), end.hashCode());
    }

    /**
     * Get an edge from the graph by its start and end nodes.
     *
     * @param startHashCode the start node hash code
     * @param endHashCode   the end node hash code
     * @return the edge, or null if not present
     */
    public Edge getEdge(int startHashCode, int endHashCode) {
        return outgoingEdges.get(startHashCode).get(endHashCode);
    }

    /**
     * Get an edge from the graph by its start and end nodes.
     *
     * @param startId the start node id
     * @param endId   the end node id
     * @return the edge, or null if not present
     */
    public Edge getEdge(String startId, String endId) {
        if (!hasNode(startId) || !hasNode(endId)) return null;
        return getEdge(nodeIndex.getInt(startId), nodeIndex.getInt(endId));
    }

    /**
     * Check if the graph has an edge.
     *
     * @param e the edge
     * @return true if the edge is present, false otherwise
     */
    public boolean hasEdge(@NotNull Edge e) {
        return outgoingEdges.containsKey(e.source().hashCode()) && outgoingEdges.get(e.source().hashCode()).containsKey(e.target().hashCode());
    }

    /**
     * Check if the graph has an edge.
     *
     * @param start the start node
     * @param end   the end node
     * @return true if the edge is present, false otherwise
     */
    public boolean hasEdge(@NotNull Node start, @NotNull Node end) {
        return getEdge(start, end) != null;
    }

    /**
     * Check if the graph has an edge.
     *
     * @param startId the start node id
     * @param endId   the end node id
     * @return true if the edge is present, false otherwise
     */
    public boolean hasEdge(String startId, String endId) {
        if (!hasNode(startId) || !hasNode(endId)) return false;
        return getEdge(startId, endId) != null;
    }

    /**
     * Get the map of edges in the graph.
     * The key is the hash code of the start node.
     * The value is a map of edges, where the key is the hash code of the end node.
     *
     * @return the map of edges
     */
    public Int2ObjectMap<Int2ObjectMap<Edge>> getEdges() {
        return outgoingEdges;
    }

    /**
     * Get a stream of all the edges in the graph.
     *
     * @return a stream of edges
     */
    public Stream<Edge> getEdgesStream() {
        return outgoingEdges.values().stream().flatMap(e -> e.values().stream());
    }

    /**
     * Count the number of nodes in the graph.
     *
     * @return the number of nodes
     */
    public int countNodes() {
        return nodes.size();
    }

    /**
     * Count the number of edges in the graph.
     *
     * @return the number of edges
     */
    public int countEdges() {
        return outgoingEdges.values().stream().mapToInt(Int2ObjectMap::size).sum();
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

    /**
     * This function runs a traversal in the upstream direction performing the specified action on each node
     *
     * @param currentNode The node where the traversal will start
     * @param action      The action to perform on each node
     */
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
                    for (var e : incomingEdges.get(ni.hashCode()).values()) {
                        traversalGuide.push(e.source());
                    }
                }
            }
        }
    }

    /**
     * This function collects all the upstream nodes of the specified node
     *
     * @param results       A collection of nodes visited by the traversal
     * @param currentNode   The node where the traversal will start
     * @param markTraversal Are nodes marked so that they are visited only once?
     */
    public void collectUpstream(Collection<Node> results, Node currentNode, boolean markTraversal) {
        traversalLogic(this::runUpstream, results, currentNode, markTraversal);
    }

    /**
     * This function runs a traversal in the downstream direction performing the specified action on each node
     *
     * @param currentNode The node where the traversal will start
     * @param action      The action to perform on each node
     */
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
                    for (var e : outgoingEdges.get(ni.hashCode()).values()) {
                        traversalGuide.push(e.target());
                    }
                }
            }
        }
    }

    /**
     * This function collects all the downstream nodes of the specified node
     *
     * @param results       A collection of nodes visited by the traversal
     * @param currentNode   The node where the traversal will start
     * @param markTraversal Are nodes marked so that they are visited only once?
     */
    public void collectDownstream(Collection<Node> results, Node currentNode, boolean markTraversal) {
        traversalLogic(this::runDownstream, results, currentNode, markTraversal);
    }

    /**
     * Find all the nodes upstream of the specified node
     *
     * @param n The node
     * @return A list of nodes
     */
    public List<Node> upstreamNodes(Node n) {
        HashSet<Node> result = new HashSet<>();
        collectUpstream(result, n, true);
        return new ArrayList<>(result);
    }

    /**
     * Find all the nodes downstream of the specified node
     *
     * @param n The node
     * @return A list of nodes
     */
    public List<Node> downstreamNodes(Node n) {
        HashSet<Node> result = new HashSet<>();
        collectDownstream(result, n, true);
        return new ArrayList<>(result);
    }

    /**
     * Iterate over all the nodes in the graph
     *
     * @return An iterator
     */
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

    /**
     * Check if two graphs are equal.
     * Two graphs are equal if they have the same nodes, edges and endpoints
     * The order of the nodes and edges or the endpoints does not matter.
     * To speed up the comparison, the hash codes of the graphs are compared first, then the number of nodes and edges.
     *
     * @param o The other graph
     * @return true if the graphs are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Graph g)) return false;
        return hashCode() == g.hashCode() &&
                countNodes() == g.countNodes() &&
                countEdges() == g.countEdges() &&
                Objects.equals(nodes, g.nodes) &&
                Objects.equals(outgoingEdges, g.outgoingEdges) &&
                Objects.equals(incomingEdges, g.incomingEdges) &&
                Objects.equals(endpoints, g.endpoints);
    }

    /**
     * Compute the hash code of the graph
     *
     * @return The hash code
     */
    @Override
    public int hashCode() {
        if (hashCode == null) {
            hashCode = Objects.hash(nodes, outgoingEdges, incomingEdges, endpoints);
        }
        return hashCode;
    }

    /**
     * The result of a traversal action, either continue, prune or stop.
     * Continue means that the traversal will continue to the adjacent nodes.
     * Prune means that the traversal will not continue to the adjacent nodes.
     * Stop means that the traversal will stop.
     */
    public enum TraversalAction {
        CONTINUE,
        PRUNE,
        STOP
    }
}
