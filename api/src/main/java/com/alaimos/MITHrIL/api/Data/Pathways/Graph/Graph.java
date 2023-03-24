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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Graph implements Serializable, Cloneable, Iterable<Node> {

    @Serial
    private static final long serialVersionUID = -8825355851517533720L;
    private final Map<String, Node> nodes = new HashMap<>();
    private final Map<String, String> aliases = new HashMap<>();
    private final Map<String, Map<String, Edge>> outgoingEdges = new HashMap<>();
    private final Map<String, Map<String, Edge>> incomingEdges = new HashMap<>();
    private final List<String> endpoints = new ArrayList<>();
    private Integer hashCode = null;

    public Graph() {
    }

    @SuppressWarnings("unchecked")
    public Graph(@NotNull Graph g) {
        g.nodes.values().forEach(n -> addNode((Node) n.clone()));
        g.outgoingEdges.values().stream().flatMap(m -> m.values().stream()).forEach(e -> {
            Edge tmpEdge = (Edge) e.clone();
            Edge clonedEdge = new Edge(node(e.source()), node(e.target()), tmpEdge.details());
            addEdge(clonedEdge);
        });
        endpoints.addAll(g.endpoints);
    }

    /**
     * Given a set of patterns, create a predicate that returns false if a node matches at least one of the patterns. If
     * the patterns are null or empty, the predicate always returns true.
     *
     * @param patterns The patterns
     * @return The predicate
     */
    @Contract(pure = true)
    private static @NotNull Predicate<Node> nodeFilteringPredicate(Pattern[] patterns) {
        if (patterns == null || patterns.length == 0) return n -> true;
        return n -> {
            for (Pattern p : patterns) {
                if (p.matcher(n.id()).matches()) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Add a node object to the graph. If the node is already present, it is not added again.
     *
     * @param n the node object
     */
    public void addNode(@NotNull Node n) {
        var id = n.id();
        if (!nodes.containsKey(id)) {
            nodes.put(id, n);
            outgoingEdges.put(id, new HashMap<>());
            incomingEdges.put(id, new HashMap<>());
            aliases.put(id, id);
            aliases.put(n.name(), id);
            for (var a : n.aliases()) {
                aliases.put(a, id);
            }
            hashCode = null;
        }
    }

    /**
     * Add a collection of node objects to the graph
     *
     * @param nodes the collection of node objects
     */
    public void addNode(@NotNull Collection<? extends Node> nodes) {
        nodes.forEach(this::addNode);
    }

    /**
     * Get a node by its id
     *
     * @param id the id
     * @return the node object. Null if not found.
     */
    public Node node(String id) {
        return nodes.get(id);
    }

    /**
     * Get a node object using another node object as reference
     *
     * @param n the reference node
     * @return the node object. Null if not found.
     */
    public Node node(@NotNull Node n) {
        return nodes.get(n.id());
    }

    /**
     * Find a node in the graph using its id, name or aliases. First, it tries to find the node by id. Then, a search by
     * name and aliases is performed.
     *
     * @param needle the id, name or alias
     * @return the node object. Null if not found.
     */
    public Node findNode(String needle) {
        if (nodes.containsKey(needle)) return nodes.get(needle);
        if (aliases.containsKey(needle)) return nodes.get(aliases.get(needle));
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
        var id = n.id();
        if (nodes.containsKey(id)) {
            nodes.remove(id);
            outgoingEdges.remove(id);
            incomingEdges.remove(id);
            outgoingEdges.forEach((s, m) -> m.remove(id));
            incomingEdges.forEach((s, m) -> m.remove(id));
            aliases.remove(n.id());
            aliases.remove(n.name());
            for (var a : n.aliases()) {
                aliases.remove(a);
            }
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
        return removeNode(node(id));
    }

    /**
     * Check if a node is present in the graph
     *
     * @param n the node object
     * @return true if the node is present, false otherwise
     */
    public boolean hasNode(@NotNull Node n) {
        return nodes.containsKey(n.id());
    }

    /**
     * Check if a node is present in the graph
     *
     * @param id the id of the node
     * @return true if the node is present, false otherwise
     */
    public boolean hasNode(String id) {
        return nodes.containsKey(id);
    }

    /**
     * Get all the endpoints of this graph
     *
     * @return a list of node ids
     */
    public List<String> endpoints() {
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
        return Optional.of(incomingEdges.get(n.id())).map(Map::size).orElse(-1);
    }

    /**
     * Compute the out-degree of a node
     *
     * @param n the node
     * @return the out-degree of the node
     */
    public int outDegree(@NotNull Node n) {
        return Optional.of(outgoingEdges.get(n.id())).map(Map::size).orElse(-1);
    }

    /**
     * Create a stream of all the edges ingoing to a node. The stream is wrapped in an Optional object to avoid
     * NullPointerExceptions.
     *
     * @param n the node
     * @return an Optional object containing the stream of edges
     */
    public Optional<Stream<Node>> ingoingNodesStream(@NotNull Node n) {
        return Optional.of(incomingEdges.get(n.id())).map(m -> m.values().stream().map(Edge::source));
    }

    /**
     * Returns a collection of edges ingoing in a node.
     *
     * @param n the node
     * @return a collection of edges
     */
    public Collection<Edge> ingoingEdges(@NotNull Node n) {
        return incomingEdges.getOrDefault(n.id(), Collections.emptyMap()).values();
    }

    /**
     * Create a stream of all the edges outgoing from a node. The stream is wrapped in an Optional object to avoid
     * NullPointerExceptions.
     *
     * @param n the node
     * @return an Optional object containing the stream of edges
     */
    public Optional<Stream<Node>> outgoingNodesStream(@NotNull Node n) {
        return Optional.of(outgoingEdges.get(n.id())).map(m -> m.values().stream().map(Edge::target));
    }

    /**
     * Returns a collection of edges outgoing from a node.
     *
     * @param n the node
     * @return a collection of edges
     */
    public Collection<Edge> outgoingEdges(@NotNull Node n) {
        return outgoingEdges.getOrDefault(n.id(), Collections.emptyMap()).values();
    }

    /**
     * Return a map of all the nodes in the graph. The key is the hash code of the node. The value is the node object.
     *
     * @return a map of nodes
     */
    public Map<String, Node> nodes() {
        return nodes;
    }

    /**
     * Return a stream of all the nodes in the graph.
     *
     * @return a stream of nodes
     */
    public Stream<Node> nodesStream() {
        return nodes.values().stream();
    }

    /**
     * Add an edge to the graph.
     *
     * @param e                      the edge to add
     * @param merge                  if true, the edge is merged with the existing edge if present
     * @param usePriority            if true, the edge subtype priority is used to keep only edge details with maximal
     *                               priority
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
            var s = e.source().id();
            var t = e.target().id();
            outgoingEdges.get(s).put(t, e);
            incomingEdges.get(t).put(s, e);
        } else {
            var existingEdge = edge(e.source(), e.target());
            if (merge && !existingEdge.equals(e)) {
                existingEdge.mergeWith(e, usePriority, allowDuplicatedDetails);
            }
        }
        hashCode = null;
    }

    /**
     * Add an edge to the graph merging all the details with the existing one, if already present in the graph.
     *
     * @param e                      the edge to add
     * @param usePriority            if true, the edge subtype priority is used to keep only edge details with maximal
     *                               priority
     * @param allowDuplicatedDetails if true, duplicates are kept in the edge details
     */
    public void addEdge(@NotNull Edge e, boolean usePriority, boolean allowDuplicatedDetails) {
        addEdge(e, true, usePriority, allowDuplicatedDetails);
    }

    /**
     * Add an edge to the graph merging all the details with the existing one, if already present in the graph. No
     * priority is used, and duplicates are removed from the details.
     *
     * @param e the edge to add
     */
    public void addEdge(@NotNull Edge e) {
        addEdge(e, true, false, false);
    }

    /**
     * Add a collection of edges to the graph merging all the details with the existing one, if already present in the
     * graph. No priority is used, and duplicates are removed from the details.
     *
     * @param edges the edges to add
     */
    public void addEdge(@NotNull Collection<? extends Edge> edges) {
        edges.forEach(this::addEdge);
    }

    /**
     * Get an edge from the graph by its start and end nodes.
     *
     * @param start the start node
     * @param end   the end node
     * @return the edge, or null if not present
     */
    public Edge edge(@NotNull Node start, @NotNull Node end) {
        return edge(start.id(), end.id());
    }

    /**
     * Get an edge from the graph by its start and end nodes.
     *
     * @param startId the start node id
     * @param endId   the end node id
     * @return the edge, or null if not present
     */
    public Edge edge(String startId, String endId) {
        if (!hasNode(startId) || !hasNode(endId)) return null;
        return outgoingEdges.getOrDefault(startId, Map.of()).get(endId);
    }

    /**
     * Check if the graph has an edge.
     *
     * @param e the edge
     * @return true if the edge is present, false otherwise
     */
    public boolean hasEdge(@NotNull Edge e) {
        return hasEdge(e.source(), e.target());
    }

    /**
     * Check if the graph has an edge.
     *
     * @param start the start node
     * @param end   the end node
     * @return true if the edge is present, false otherwise
     */
    public boolean hasEdge(@NotNull Node start, @NotNull Node end) {
        return edge(start.id(), end.id()) != null;
    }

    /**
     * Check if the graph has an edge.
     *
     * @param startId the start node id
     * @param endId   the end node id
     * @return true if the edge is present, false otherwise
     */
    public boolean hasEdge(String startId, String endId) {
        return edge(startId, endId) != null;
    }

    /**
     * Get the map of edges in the graph. The key is the hash code of the start node. The value is a map of edges, where
     * the key is the hash code of the end node.
     *
     * @return the map of edges
     */
    public Map<String, Map<String, Edge>> edges() {
        return outgoingEdges;
    }

    /**
     * Get a stream of all the edges in the graph.
     *
     * @return a stream of edges
     */
    public Stream<Edge> edgesStream() {
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
        return outgoingEdges.values().stream().mapToInt(Map::size).sum();
    }

    /**
     * This function implements all the logic needed for a traversal in this graph
     *
     * @param consumer      A method which runs a traversal in a specific direction
     * @param results       A collection of nodes visited by the traversal
     * @param currentNode   The node where the traversal will start
     * @param markTraversal Are nodes marked so that they are visited only once?
     */
    protected void traversalLogic(
            @NotNull BiConsumer<Node, Function<Node, TraversalAction>> consumer, @NotNull Collection<Node> results,
            @NotNull Node currentNode, boolean markTraversal
    ) {
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
                    for (var e : incomingEdges.get(ni.id()).values()) {
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
                    for (var e : outgoingEdges.get(ni.id()).values()) {
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
     * Check if two graphs are equal. Two graphs are equal if they have the same nodes, edges and endpoints The order of
     * the nodes and edges or the endpoints does not matter. To speed up the comparison, the hash codes of the graphs
     * are compared first, then the number of nodes and edges.
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
     * Create a bidirectional index between a contiguous sequence integers (1 to N, where N is the number of nodes) and
     * node ids.
     *
     * @return A pair of maps, the first maps from index to id, the second from id to index
     */
    public Pair<Int2ObjectMap<String>, Object2IntOpenHashMap<String>> index() {
        var index2Id = new Int2ObjectOpenHashMap<String>();
        var id2Index = new Object2IntOpenHashMap<String>();
        int i = 0;
        for (var n : nodes.values()) {
            index2Id.put(i, n.id());
            id2Index.put(n.id(), i);
            i++;
        }
        return Pair.of(index2Id, id2Index);
    }

    /**
     * Extend this graph with the nodes and edges of another graph. This is an iterative process, and it is different
     * from the merge operation. First, all the edges with both source and target nodes in this graph are added. Then,
     * until no more edges can be added, all the edges with at least one node in this graph are added. The process stops
     * when the set of edges to be added is empty, or when the number of edges in the graph does not change.
     *
     * @param other The other graph
     */
    public void extendWith(@NotNull Graph other) {
        var previousCountEdges = -1;
        var currentCountEdges = countEdges();
        var remainingEdges = other.edgesStream().filter(e -> {
            if (hasEdge(e)) {
                addEdge(e);
                return false;
            }
            return true;
        }).collect(Collectors.toSet());
        while (previousCountEdges != currentCountEdges && !remainingEdges.isEmpty()) {
            previousCountEdges = currentCountEdges;
            var newRemainingEdges = new HashSet<Edge>();
            for (var e : remainingEdges) {
                var source = e.source();
                var target = e.target();
                if (nodes.containsKey(source.id()) || nodes.containsKey(target.id())) {
                    if (!nodes.containsKey(source.id())) {
                        addNode(source);
                    }
                    if (!nodes.containsKey(target.id())) {
                        addNode(target);
                    }
                    addEdge(e);
                    currentCountEdges++;
                } else {
                    newRemainingEdges.add(e);
                }
            }
            remainingEdges = newRemainingEdges;
        }
    }

    /**
     * Merge this graph with another one. If a node is present in both graphs, the node is added only once. If an edge
     * is present in both graphs, the two edges are merged keeping only details with maximal priority. Nodes are
     * filtered using the user-defined filters. Priority filtering can be disabled.
     *
     * @param other           The other graph
     * @param nodeFilters     The node filters
     * @param disablePriority Disable priority filtering
     * @return A list of pairs of node ids, each pair represents an edge that was added to the graph
     */
    public List<Pair<String, String>> mergeWith(
            @NotNull Graph other, @Nullable Pattern[] nodeFilters, boolean disablePriority
    ) {
        var addedEdges = new ArrayList<Pair<String, String>>();
        var filteringPredicate = nodeFilteringPredicate(nodeFilters);
        other.nodesStream()
             .filter(filteringPredicate.or(Predicate.not(this::hasNode)))
             .map(n -> (Node) n.clone())
             .forEach(this::addNode);
        other.edgesStream().forEach(e -> {
            var source = e.source();
            var target = e.target();
            if (hasNode(source) && hasNode(target)) {
                var edge = edge(source, target);
                if (edge == null) {
                    addEdge((Edge) e.clone());
                } else {
                    edge.mergeWith(e, disablePriority, true);
                }
                addedEdges.add(Pair.of(source.id(), target.id()));
            }
        });
        setEndpoints(Stream.of(endpoints.stream(), other.endpoints.stream())
                           .flatMap(Function.identity())
                           .distinct()
                           .toList());
        return addedEdges;
    }

    /**
     * The result of a traversal action, either continue, prune or stop. Continue means that the traversal will continue
     * to the adjacent nodes. Prune means that the traversal will not continue to the adjacent nodes. Stop means that
     * the traversal will stop.
     */
    public enum TraversalAction {
        CONTINUE,
        PRUNE,
        STOP
    }
}
