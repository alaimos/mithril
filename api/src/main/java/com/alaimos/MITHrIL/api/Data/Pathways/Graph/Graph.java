package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class Graph implements Serializable, Cloneable, Iterable<Node> {

    @Serial
    private static final long serialVersionUID = -8825355851517533720L;
    private HashMap<String, Node> nodes = new HashMap<>();
    protected HashMap<String, Integer> upstream = new HashMap<>();
    protected HashMap<String, Integer> downstream = new HashMap<>();
    protected HashMap<String, HashMap<String, EdgeInterface>> outEdges = new HashMap<>();
    protected HashMap<String, HashMap<String, EdgeInterface>> inEdges = new HashMap<>();
    protected ArrayList<String> endpoints = new ArrayList<>();
    protected WeightComputationInterface weightComputation = null;
    protected PathwayInterface owner;
    protected Integer hashCode = null;

    public Graph() {
        owner = null;
    }

    @Override
    public boolean isEmpty() {
        return this.countNodes() == 0;
    }

    @Override
    public GraphInterface addNode(NodeInterface n) {
        if (!nodes.containsKey(n.getId())) {
            nodes.put(n.getId(), n);
            outEdges.put(n.getId(), new HashMap<>());
            inEdges.put(n.getId(), new HashMap<>());
            upstream.put(n.getId(), null);
            downstream.put(n.getId(), null);
            hashCode = null;
        }
        return this;
    }

    @Override
    public NodeInterface addNode(String id, String name, String type) {
        return this.addNode(id, name, NodeType.fromString(type));
    }

    @Override
    public NodeInterface addNode(String id, String name, NodeType type) {
        Node n = new Node(id, name, type);
        this.addNode(n);
        return n;
    }

    @Override
    public NodeInterface getNode(String id) {
        return (hasNode(id)) ? nodes.get(id) : null;
    }

    @Override
    public NodeInterface findNode(String needle) {
        if (hasNode(needle)) {
            return nodes.get(needle);
        }
        for (NodeInterface n : nodes.values()) {
            if (n.contains(needle)) return n;
        }
        return null;
    }

    @Override
    public boolean removeNode(String id) {
        if (nodes.containsKey(id)) {
            nodes.remove(id);
            outEdges.remove(id);
            outEdges.forEach((s, m) -> m.remove(id));
            inEdges.remove(id);
            inEdges.forEach((s, m) -> m.remove(id));
            upstream.remove(id);
            downstream.remove(id);
            hashCode = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean removeNode(NodeInterface n) {
        return removeNode(n.getId());
    }

    @Override
    public boolean hasNode(NodeInterface n) {
        return nodes.containsKey(n.getId());
    }

    @Override
    public boolean hasNode(String id) {
        return nodes.containsKey(id);
    }

    @Override
    public List<String> getEndpoints() {
        return endpoints;
    }

    @Override
    public GraphInterface setEndpoints(List<String> endpoints) {
        this.endpoints.clear();
        endpoints.stream().filter(this::hasNode).forEachOrdered(this.endpoints::add);
        hashCode = null;
        return this;
    }

    @Override
    public int inDegree(NodeInterface n) {
        return (hasNode(n)) ? inEdges.get(n.getId()).size() : -1;
    }

    @Override
    public int outDegree(NodeInterface n) {
        return (hasNode(n)) ? outEdges.get(n.getId()).size() : -1;
    }

    @Override
    public Stream<NodeInterface> ingoingNodesStream(NodeInterface n) {
        if (inEdges.containsKey(n.getId())) {
            return inEdges.get(n.getId()).values().stream().map(EdgeInterface::getStart);
        }
        return null;
    }

    @Override
    public Stream<NodeInterface> outgoingNodesStream(NodeInterface n) {
        if (outEdges.containsKey(n.getId())) {
            return outEdges.get(n.getId()).values().stream().map(EdgeInterface::getEnd);
        }
        return null;
    }

    @Override
    public int countUpstreamNodes(NodeInterface n) {
        upstream.computeIfAbsent(n.getId(), k -> upstreamNodes(n).size());
        return upstream.get(n.getId());
    }

    @Override
    public List<NodeInterface> upstreamNodes(NodeInterface n) {
        HashSet<NodeInterface> result = new HashSet<>();
        traverseUpstream(result, n);
        return new ArrayList<>(result);
    }

    @Override
    public int countDownstreamNodes(NodeInterface n) {
        downstream.computeIfAbsent(n.getId(), k -> downstreamNodes(n).size());
        return downstream.get(n.getId());
    }

    @Override
    public List<NodeInterface> downstreamNodes(NodeInterface n) {
        HashSet<NodeInterface> result = new HashSet<>();
        traverseDownstream(result, n);
        return new ArrayList<>(result);
    }

    @Override
    public Map<String, NodeInterface> getNodes() {
        return nodes;
    }

    @Override
    public Stream<NodeInterface> getNodesStream() {
        return nodes.values().stream();
    }

    @Override
    public GraphInterface addEdge(EdgeInterface e) {
        if (!hasNode(e.getStart())) {
            addNode(e.getStart());
        }
        if (!hasNode(e.getEnd())) {
            addNode(e.getEnd());
        }
        if (!hasEdge(e)) {
            e.setWeightComputationInterface(this.weightComputation);
            outEdges.get(e.getStart().getId()).put(e.getEnd().getId(), e);
            inEdges.get(e.getEnd().getId()).put(e.getStart().getId(), e);
            downstream.put(e.getStart().getId(), null);
            upstream.put(e.getEnd().getId(), null);
        } else {
            EdgeInterface edge = getEdge(e.getStart(), e.getEnd());
            if (!edge.equals(e)) {
                e.getDescriptions().stream().filter(d -> !edge.getDescriptions().contains(d))
                        .forEachOrdered(edge::addDescription);
            }
        }
        if (owner != null) {
            getEdge(e.getStart(), e.getEnd()).getDescriptions().stream().filter(d -> d.getOwner() == null)
                    .forEach(d -> d.setOwner(owner));
        }
        hashCode = null;
        return this;
    }

    @Override
    public EdgeInterface addEdge(NodeInterface start, NodeInterface end, List<EdgeDescriptionInterface> descriptions) {
        Edge e = new Edge(start, end, descriptions);
        this.addEdge(e);
        return e;
    }

    @Override
    public EdgeInterface addEdge(NodeInterface start, NodeInterface end, EdgeType type, EdgeSubType subType) {
        Edge e = new Edge(start, end, type, subType, owner);
        this.addEdge(e);
        return e;
    }

    @Override
    public EdgeInterface addEdge(String startId, String endId, String type, String subType) {
        return this.addEdge(this.getNode(startId), this.getNode(endId), EdgeType.fromString(type),
                EdgeSubType.fromString(subType));
    }

    @Override
    public EdgeInterface getEdge(NodeInterface start, NodeInterface end) {
        return getEdge(start.getId(), end.getId());
    }

    @Override
    public EdgeInterface getEdge(String startId, String endId) {
        return (hasEdge(startId, endId)) ? outEdges.get(startId).get(endId) : null;
    }

    @Override
    public boolean hasEdge(EdgeInterface e) {
        return outEdges.containsKey(e.getStart().getId()) &&
                outEdges.get(e.getStart().getId()).containsKey(e.getEnd().getId());
    }

    @Override
    public boolean hasEdge(NodeInterface start, NodeInterface end) {
        return outEdges.containsKey(start.getId()) &&
                outEdges.get(start.getId()).containsKey(end.getId());
    }

    @Override
    public boolean hasEdge(String startId, String endId) {
        return outEdges.containsKey(startId) &&
                outEdges.get(startId).containsKey(endId);
    }

    @Override
    public Map<String, ? extends Map<String, EdgeInterface>> getEdges() {
        return outEdges;
    }

    @Override
    public Stream<EdgeInterface> getEdgesStream() {
        return outEdges.entrySet().stream().flatMap(e -> e.getValue().entrySet().stream()).map(Map.Entry::getValue);
    }

    @Override
    public int countNodes() {
        return nodes.size();
    }

    @Override
    public int countEdges() {
        return outEdges.values().stream().mapToInt(HashMap::size).sum();
    }

    /**
     * This function implements all the logic needed for a traversal in this graph
     *
     * @param consumer      A method which runs a traversal in a specific direction
     * @param results       A collection of nodes visited by the traversal
     * @param currentNode   The node where the traversal will start
     * @param markTraversal Are node marked so that they are visited only once?
     */
    protected void traversalLogic(BiConsumer<NodeInterface, Function<NodeInterface, ActionResult>> consumer,
                                  Collection<NodeInterface> results, NodeInterface currentNode, boolean markTraversal) {
        final HashSet<NodeInterface> marked = new HashSet<>();
        consumer.accept(currentNode, o -> {
            if (o.equals(currentNode)) {
                if (markTraversal) {
                    if (!marked.add(currentNode)) {
                        return ActionResult.PRUNE;
                    }
                }
                return ActionResult.CONTINUE;
            }
            if (!markTraversal) {
                if (results.add(o)) {
                    return ActionResult.CONTINUE;
                } else {
                    return ActionResult.PRUNE;
                }
            } else {
                if (marked.add(o)) {
                    results.add(o);
                    return ActionResult.CONTINUE;
                } else {
                    return ActionResult.PRUNE;
                }
            }
        });
    }

    @Override
    public void runUpstream(NodeInterface currentNode, Function<NodeInterface, ActionResult> action) {
        Stack<NodeInterface> traversalGuide = new Stack<>();
        traversalGuide.push(currentNode);
        while (!traversalGuide.isEmpty()) {
            NodeInterface ni = traversalGuide.pop();
            ActionResult r = action.apply(ni);
            if (r == ActionResult.STOP) {
                break;
            } else if (r == ActionResult.CONTINUE) {
                if (inDegree(ni) > 0) {
                    inEdges.get(ni.getId()).forEach((s, un) -> traversalGuide.push(un.getStart()));
                }
            }
        }
    }

    @Override
    public void traverseUpstream(Collection<NodeInterface> results, NodeInterface currentNode, boolean markTraversal) {
        traversalLogic(this::runUpstream, results, currentNode, markTraversal);
    }

    @Override
    public void runDownstream(NodeInterface currentNode, Function<NodeInterface, ActionResult> action) {
        Stack<NodeInterface> traversalGuide = new Stack<>();
        traversalGuide.push(currentNode);
        while (!traversalGuide.isEmpty()) {
            NodeInterface ni = traversalGuide.pop();
            ActionResult r = action.apply(ni);
            if (r == ActionResult.STOP) {
                break;
            } else if (r == ActionResult.CONTINUE) {
                if (outDegree(ni) > 0) {
                    outEdges.get(ni.getId()).forEach((s, un) -> traversalGuide.push(un.getEnd()));
                }
            }
        }
    }

    @Override
    public void traverseDownstream(Collection<NodeInterface> results, NodeInterface currentNode,
                                   boolean markTraversal) {
        traversalLogic(this::runDownstream, results, currentNode, markTraversal);
    }

    @NotNull
    @Override
    public Iterator<NodeInterface> iterator() {
        return this.nodes.values().iterator();
    }

    /**
     * Clone the object
     *
     * @return my clone
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        Graph clone;
        try {
            clone = (Graph) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
        clone.nodes = new HashMap<>();
        clone.outEdges = new HashMap<>();
        clone.inEdges = new HashMap<>();
        nodes.forEach((s, n) -> clone.addNode((NodeInterface) n.clone()));
        outEdges.forEach((s, edges) -> edges.forEach((e, edge) -> {
            EdgeInterface edgeC = (EdgeInterface) edge.clone();
            edgeC.setStart(clone.getNode(s)).setEnd(clone.getNode(e));
            clone.addEdge(edgeC);
        }));
        clone.endpoints = (ArrayList<String>) endpoints.clone();
        clone.weightComputation = this.weightComputation; //Weight computation in never cloned
        clone.owner = owner;
        return clone;
    }

    @Override
    public GraphInterface setOwner(PathwayInterface o) {
        if (this.owner != o) {
            outEdges.forEach((s, edges) -> edges.forEach((e, edge) -> edge.getDescriptionsOwnedBy(owner).forEach(d -> d.setOwner(o))));
        }
        this.owner = o;
        return this;
    }

    @Override
    public PathwayInterface getOwner() {
        return this.owner;
    }

    @Override
    public boolean isOwnedBy(PathwayInterface o) {
        return this.owner.equals(o);
    }

    @Override
    public GraphInterface setDefaultWeightComputation(WeightComputationInterface defaultWeightComputation,
                                                      boolean changeAll) {
        WeightComputationInterface old = this.weightComputation;
        this.weightComputation = defaultWeightComputation;
        if (this.weightComputation != old && changeAll) {
            this.outEdges.forEach((s, edges) -> edges.forEach((s1, edge) -> edge.setWeightComputationInterface(this.weightComputation)));
        }
        return this;
    }

    @Override
    public WeightComputationInterface getDefaultWeightComputation() {
        return weightComputation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Graph)) return false;
        Graph that = (Graph) o;
        return countNodes() == that.countNodes() &&
                countEdges() == that.countEdges() &&
                Objects.equals(nodes, that.nodes) &&
                Objects.equals(outEdges, that.outEdges) &&
                Objects.equals(inEdges, that.inEdges) &&
                Objects.equals(endpoints, that.endpoints);
    }

    @Override
    public int hashCode() {
        if (hashCode == null) {
            hashCode = Objects.hash(nodes, outEdges, inEdges, endpoints);
        }
        return hashCode;
    }
}
