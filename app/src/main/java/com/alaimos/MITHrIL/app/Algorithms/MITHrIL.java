package com.alaimos.MITHrIL.app.Algorithms;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Node;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Pathway;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Math.PValue.Adjusters.AdjusterInterface;
import com.alaimos.MITHrIL.api.Math.PValue.Combiners.CombinerInterface;
import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MITHrIL {

    /**
     * This predicate is used to filter out Differentially Expressed Nodes from other nodes
     */
    private static final Predicate<? super Map.Entry<String, Double>> diffExpFilteringPredicate = e -> Double.isFinite(e.getValue()) && e.getValue() != 0.0;

    //region Input Parameters
    protected Random random;
    protected Map<String, Double> expressions;
    protected Repository repository;
    protected int numberOfRepetitions;
    protected int batchSize;
//    protected ProbabilityComputationInterface probabilityComputation;
    protected CombinerInterface pValueCombiner;
    protected AdjusterInterface pValueAdjuster;
    protected boolean noPValue;
    //endregion
    //region Output Values
    protected Map<String, Map<String, Map<String, Double>>> visitedPerturbations;
    protected Map<String, Map<String, Integer>> count;
    protected Map<String, Map<String, Double>> perturbations;
    protected Map<String, Map<String, Double>> nodeAccumulators;
    protected Map<String, Map<String, Double>> nodePValues;
    protected Map<String, Double> accumulators;
    protected Map<String, Double> impactFactors;
    protected Map<String, Double> probabilities;
    protected Map<String, Double> probabilitiesNetwork;
    protected Map<String, Double> correctedAccumulators;
    protected Map<String, Double> pValues;
    protected Map<String, Double> adjustedPValues;
    protected Map<Pathway, List<Node>> sortedNodes;
    //endregion

    public MITHrIL() {
    }

    /**
     * Intersect a stream with a pathway. That is gets all the elements of a stream that are node of a pathway.
     *
     * @param ids a stream of strings
     * @param p   a pathway with an associated graph
     * @return the list of nodeIds contained in p
     */
    List<String> intersect(Stream<String> ids, PathwayInterface p) {
        return ids.filter(p.getGraph()::hasNode).collect(Collectors.toList());
    }

    /**
     * Intersect a stream with a virtual pathway. That is gets all the elements of a stream that are node of a pathway.
     *
     * @param ids a stream of strings
     * @param p   a pathway with an associated graph
     * @return the list of nodeIds contained in p
     */
    List<String> intersect(Stream<String> ids, String p) {
        var nodes = repository.getNodesOfVirtualPathway(p).stream().map(NodeInterface::getId).collect(Collectors.toList());
        return ids.filter(nodes::contains).collect(Collectors.toList());
    }

    /**
     * Intersect a stream with a pathway. That is gets all the elements of a stream that are node of a pathway.
     *
     * @param ids a stream of strings
     * @param p   a pathway with an associated graph
     * @return the list of nodeIds contained in p
     */
    Stream<Map.Entry<String, Double>> intersectMap(Stream<Map.Entry<String, Double>> ids, PathwayInterface p) {
        return ids.filter(e -> p.getGraph().hasNode(e.getKey()));
    }

    /**
     * Intersect a stream with a virtual pathway. That is gets all the elements of a stream that are node of a pathway.
     *
     * @param ids a stream of strings
     * @param p   a pathway with an associated graph
     * @return the list of nodeIds contained in p
     */
    Stream<Map.Entry<String, Double>> intersectMap(Stream<Map.Entry<String, Double>> ids, String p) {
        var nodes = repository.getNodesOfVirtualPathway(p).stream().map(NodeInterface::getId).collect(Collectors.toList());
        return ids.filter(e -> nodes.contains(e.getKey()));
    }

    /**
     * Get the log-fold-change of a node. If no finite FC has been supplied its value will be set to 0
     *
     * @param node the node
     * @return the fold change of that node
     */
    protected double getExpression(NodeInterface node) {
        return getExpression(node.getId());
    }

    /**
     * Get the log-fold-change of a node. If no finite FC has been supplied its value will be set to 0
     *
     * @param id the id of the node
     * @return the fold change of that node
     */
    private double getExpression(String id) {
        return expressions.getOrDefault(id, 0.0);
    }

    /**
     * Compute probability of having in a pathway a number of differential expressed genes greater than the observer one
     *
     * @param p a pathway
     * @return the probability
     */
    double probability(PathwayInterface p) {
        if (probabilities.containsKey(p.getId())) {
            return probabilities.get(p.getId());
        }
        List<String> deGenes = expressions.entrySet().stream().filter(diffExpFilteringPredicate).map(Map.Entry::getKey).collect(Collectors.toList());
        double prob = probabilityComputation.computeProbability(new ArrayList<>(expressions.keySet()), intersect(expressions.keySet().stream(), p),
                deGenes, intersect(deGenes.stream(), p));
        probabilities.put(p.getId(), prob);
        return prob;
    }

    /**
     * Compute probability of having in a pathway a number of differential expressed genes greater than the observer one
     *
     * @param p a virtual pathway
     * @return the probability
     */
    double probability(String p) {
        if (probabilities.containsKey(p)) {
            return probabilities.get(p);
        }
        if (repository.hasVirtualPathway(p)) {
            List<String> deGenes =
                    expressions.entrySet().stream().filter(diffExpFilteringPredicate).map(Map.Entry::getKey).collect(Collectors.toList());
            double prob = probabilityComputation.computeProbability(new ArrayList<>(expressions.keySet()),
                    intersect(expressions.keySet().stream(), p),
                    deGenes, intersect(deGenes.stream(), p));
            probabilities.put(p, prob);
            return prob;
        }
        return Double.NaN;
    }

    /**
     * Compute sum of absolute values of weight of outgoing edges of a node
     *
     * @param u A node
     * @param p A Pathway which contains such node
     * @return The total weight
     */
    private double absoluteTotalWeight(NodeInterface u, PathwayInterface p) {
        GraphInterface g = p.getGraph();
        double weight = 0.0;
        for (NodeInterface d : g.outgoingNodes(u)) {
            weight += Math.abs(g.getEdge(u, d).computeWeight());
        }
        return weight;
    }

    protected void putPerturbation(String nId, String pId, double pert, double acc) {
        if (!perturbations.containsKey(pId)) {
            visitedPerturbations.put(pId, new HashMap<>());
            nodeAccumulators.put(pId, new HashMap<>());
            perturbations.put(pId, new HashMap<>());
        }
        perturbations.get(pId).put(nId, pert);
        nodeAccumulators.get(pId).put(nId, acc);
    }

    protected double perturbation(@NotNull NodeInterface n, @NotNull PathwayInterface p, String startNode) {
        var pId = p.getId();
        var nId = n.getId();
        if (!visitedPerturbations.containsKey(pId)) {
            nodeAccumulators.put(pId, new HashMap<>());
            perturbations.put(pId, new HashMap<>());
            visitedPerturbations.put(pId, new HashMap<>());
        }
        if (!visitedPerturbations.get(pId).containsKey(startNode)) {
            visitedPerturbations.get(pId).put(startNode, new HashMap<>());
        }
        if (visitedPerturbations.get(pId).get(startNode).containsKey(nId)) {
            return visitedPerturbations.get(pId).get(startNode).get(nId);
        }
        if (perturbations.get(pId).containsKey(nId)) {
            return perturbations.get(pId).get(nId);
        }
        var exp = getExpression(nId);
        visitedPerturbations.get(pId).get(startNode).put(nId, exp);
        var g = p.getGraph();
        var pf = exp;
        for (NodeInterface u : g.ingoingNodes(n)) {
            var tmp = g.getEdge(u, n).computeWeight() * (perturbation(u, p, startNode) / absoluteTotalWeight(u, p));
            if (Double.isFinite(tmp)) pf += tmp;
        }
        visitedPerturbations.get(pId).get(startNode).put(nId, pf);
        putPerturbation(nId, pId, pf, pf - exp);
        return pf;
    }

    /**
     * Compute perturbation of a node in a pathway
     *
     * @param n A node
     * @param p A pathway which contains such node
     * @return The perturbation
     */
    protected double perturbation(NodeInterface n, PathwayInterface p) {
        return perturbation(n, p, n.getId());
        /*var pId = p.getId();
        var nId = n.getId();
        if (!perturbations.containsKey(pId)) {
            nodeAccumulators.put(pId, new HashMap<>());
            perturbations.put(pId, new HashMap<>());
        }
        if (perturbations.get(pId).containsKey(nId)) {
            return perturbations.get(pId).get(nId);
        }
        decrementCount(nId, pId);
        var exp = getExpression(nId);
        if (isCountZero(nId, pId)) {
            perturbations.get(pId).put(nId, exp); // Avoids cycles
        }

        var g = p.getGraph();
        var pf = exp;
        for (NodeInterface u : g.ingoingNodes(n)) {
            var tmp = g.getEdge(u, n).computeWeight() * (perturbation(u, p) / absoluteTotalWeight(u, p));
            if (Double.isFinite(tmp)) pf += tmp;
        }
        putPerturbation(nId, pId, pf, pf - exp);
        return pf;*/
    }

    /**
     * Computes the impact factor of a pathway
     *
     * @param p the pathway
     * @return the impact factor
     */
    protected double impactFactor(PathwayInterface p) {
        if (impactFactors.containsKey(p.getId())) {
            return impactFactors.get(p.getId());
        }
        Supplier<Stream<Map.Entry<String, Double>>> stream = () -> expressions.entrySet().stream().filter(diffExpFilteringPredicate);
        var cif = p.getGraph().getNodes().values().stream().mapToDouble(n -> Math.abs(perturbation(n, p))).sum();
        var deGenesPathway = intersect(stream.get().map(Map.Entry::getKey), p).size();
        var meanDE = intersectMap(stream.get(), p).map(Map.Entry::getValue).map(Math::abs).collect(Collectors.averagingDouble(Double::doubleValue));
        var pi = Math.log(probability(p));
        if ((meanDE * deGenesPathway) == 0.0) {
            impactFactors.put(p.getId(), 0.0);
            return 0.0;
        }
        cif /= (meanDE * deGenesPathway);
        cif -= pi;
        impactFactors.put(p.getId(), cif);
        return cif;
    }

    /**
     * Computes the impact factor of a virtual pathway
     *
     * @param p a virtual pathway
     * @return the impact factor
     */
    protected double impactFactor(String p) {
        if (impactFactors.containsKey(p)) {
            return impactFactors.get(p);
        }
        Supplier<Stream<Map.Entry<String, Double>>> stream = () -> expressions.entrySet().stream().filter(diffExpFilteringPredicate);
        var source = repository.getSourceOfVirtualPathway(p);
        var nodes = repository.getNodesOfVirtualPathway(p);
        var cif = nodes.stream().mapToDouble(n -> Math.abs(perturbation(n, source))).sum();
        var deGenesPathway = intersect(stream.get().map(Map.Entry::getKey), p).size();
        var meanDE = intersectMap(stream.get(), p).map(Map.Entry::getValue).mapToDouble(Math::abs).average().orElse(0.0);
        var pi = Math.log(probability(p));
        if ((meanDE * deGenesPathway) == 0.0) {
            impactFactors.put(p, 0.0);
            return 0.0;
        }
        cif /= (meanDE * deGenesPathway);
        cif -= pi;
        impactFactors.put(p, cif);
        return cif;
    }

    /**
     * Computes the total perturbation accumulation of a pathway
     *
     * @param p the pathway
     * @return the accumulation
     */
    protected double accumulation(PathwayInterface p) {
        if (accumulators.containsKey(p.getId())) {
            return accumulators.get(p.getId());
        }
        double a = 0.0;
        for (NodeInterface v : p.getGraph().getNodes().values()) {
            a += v.getType().sign() * (perturbation(v, p) - getExpression(v));
        }
        accumulators.put(p.getId(), a);
        return a;
    }

    /**
     * Computes the total perturbation accumulation of a virtual pathway
     *
     * @param p a virtual pathway
     * @return the accumulation
     */
    protected double accumulation(String p) {
        if (accumulators.containsKey(p)) {
            return accumulators.get(p);
        }
        var source = repository.getSourceOfVirtualPathway(p);
        var nodes = repository.getNodesOfVirtualPathway(p);
        double a = 0.0;
        for (var v : nodes) {
            a += v.getType().sign() * (perturbation(v, source) - getExpression(v));
        }
        accumulators.put(p, a);
        return a;
    }

    /**
     * Create an empty copy of an String=>Double HashMap. Only keys are preserved.
     *
     * @param h an HashMap
     * @return the empty copy
     */
    private static Map<String, Double> emptyCopy(Map<String, Double> h) {
        var copy = new HashMap<String, Double>();
        for (var s : h.keySet()) {
            copy.put(s, 0.0);
        }
        return copy;
    }

    /**
     * Permute the expression values in an expression map in order to compute random accumulators
     *
     * @param expChange an array of expression values
     * @param nodes     a list of nodes to permute
     * @return an expression maps
     */
    Map<String, Double> permuteExpressionMap(double[] expChange, List<String> nodes) {
        var tmpCopy = emptyCopy(expressions);
        int k;
        String no;
        for (double e : expChange) {
            do {
                k = random.nextInt(nodes.size());
                no = nodes.get(k);
            } while (tmpCopy.get(no) != 0.0);
            tmpCopy.put(no, e);
        }
        return tmpCopy;
    }

    /**
     * Filters out all pathways for which no p-value computation is needed.
     * <p>
     * A pathway whose number of nodes or edges is zero will never have a valid accumulator so no p-value computation
     * is needed.
     * <p>
     * A pathway whose accumulator is zero or NaN will need no p-value computation.
     *
     * @return a list of pathway for which p-value computation should be run
     */
    private List<PathwayInterface> getPathwaysForPValue() {
        ArrayList<PathwayInterface> pathways = new ArrayList<>();
        for (PathwayInterface p : repository) {
            var pId = p.getId();
            var g = p.getGraph();
            if (p.hasGraph() && g.countNodes() > 0 && g.countEdges() > 0) {
                if (accumulators.containsKey(pId) && Double.isFinite(accumulators.get(pId)) && accumulators.get(pId) != 0.0) {
                    pathways.add(p);
                    pValues.put(pId, 0.0);
                    nodePValues.put(pId, g.getNodesStream().collect(Collectors.toMap(NodeInterface::getId, v -> 0.0)));
                } else {
                    accumulators.put(pId, 0.0);
                    correctedAccumulators.put(pId, 0.0);
                    pValues.put(pId, 1.0);
                    nodePValues.put(pId, g.getNodesStream().collect(Collectors.toMap(NodeInterface::getId, v -> 1.0)));
                }
            }
        }
        return pathways;
    }

    private void initVirtualPathwaysPValues() {
        for (var p : repository.getVirtualPathways()) {
            if (accumulators.containsKey(p) && Double.isFinite(accumulators.get(p)) && accumulators.get(p) != 0.0) {
                pValues.put(p, 0.0);
                nodePValues.put(p, repository.getNodesOfVirtualPathway(p).stream().collect(Collectors.toMap(NodeInterface::getId, v -> 0.0)));
            } else {
                accumulators.put(p, 0.0);
                correctedAccumulators.put(p, 0.0);
                pValues.put(p, 1.0);
                nodePValues.put(p, repository.getNodesOfVirtualPathway(p).stream().collect(Collectors.toMap(NodeInterface::getId, v -> 1.0)));
            }
        }
    }

    /**
     * Init new MITHrIL Computation object
     *
     * @param expChange an array of random log-fold-changes
     * @param nodes     a list of nodes
     * @param pathways  a list of pathways
     * @return a new instance of this object
     */
    protected MITHrIL initSelf(double[] expChange, List<String> nodes, List<PathwayInterface> pathways) {
        MITHrIL tmp = null;
        try {
            tmp = getClass().getDeclaredConstructor().newInstance();
            tmp.init();
            tmp.random = random;
            tmp.expressions = permuteExpressionMap(expChange, nodes);
            tmp.repository = repository;
            tmp.internalRun(pathways);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new AlgorithmExecutionException(e);
        }
        return tmp;
    }

    void incrementNodesCounters(GraphInterface g, MITHrIL tmp, String pId1, String pId2) {
        incrementNodesCounters(g.getNodes().values(), tmp, pId1, pId2);
    }

    void incrementNodesCounters(Collection<NodeInterface> nodes, MITHrIL tmp, String pId1, String pId2) {
        for (NodeInterface n : nodes) {
            var nId = n.getId();
            var a = perturbations.get(pId2).getOrDefault(nId, 0.0);
            var r = tmp.perturbations.get(pId2).getOrDefault(nId, 0.0);
            var counter = nodePValues.get(pId1).get(nId) + (((a > 0) ? r >= a : r <= a) ? 1.0 : 0.0);
            nodePValues.get(pId1).put(nId, counter);
        }
    }

    /**
     * Increment counters for p-value computation
     *
     * @param p                a pathway
     * @param tmp              a temporary mithril instance
     * @param i                the current iteration index
     * @param randAccumulators the map of random accumulators
     */
    protected void incrementCounters(PathwayInterface p, MITHrIL tmp, int i, HashMap<String, double[]> randAccumulators) {
        String pId = p.getId();
        GraphInterface g = p.getGraph();
        randAccumulators.get(pId)[i] = tmp.accumulators.get(pId);
        incrementNodesCounters(g, tmp, pId, pId);
    }

    /**
     * Increment counters for p-value computation
     *
     * @param p                a virtual pathway
     * @param tmp              a temporary mithril instance
     * @param i                the current iteration index
     * @param randAccumulators the map of random accumulators
     */
    protected void incrementCounters(String p, MITHrIL tmp, int i, HashMap<String, double[]> randAccumulators) {
        randAccumulators.get(p)[i] = tmp.accumulators.get(p);
        incrementNodesCounters(repository.getNodesOfVirtualPathway(p), tmp, p, repository.getSourceOfVirtualPathway(p).getId());
    }

    private void finalizePValue(String pId, Median medCalc, HashMap<String, double[]> randAccumulators, PathwayInterface p,
                                Collection<NodeInterface> nodes, double minPV) {
        var median = medCalc.evaluate(randAccumulators.get(pId));
        var acc = accumulators.getOrDefault(pId, 0.0);
        var pi = (p != null) ? probability(p) : probability(pId);
        var corr = acc - median;
        var pValue = 0.0;
        if (corr != 0.0) {
            pValue = 0.0;
            for (double r : randAccumulators.get(pId)) {
                pValue += ((acc > 0) ? r >= acc : r <= acc) ? 1.0 : 0.0;
            }
            pValue = pValue / numberOfRepetitions;
            if (pValue <= minPV) {
                pValue = minPV;
            } else if (pValue > 1) { //This should never happen!
                pValue = 1.0;
            }
        } else {
            pValue = 1.0;
        }
        probabilitiesNetwork.put(pId, pValue);
        pValue = pValueCombiner.combine(pi, pValue);
        correctedAccumulators.put(pId, corr);
        pValues.put(pId, pValue);
        for (NodeInterface n : nodes) {
            var nId = n.getId();
            var tmpVal = (nodePValues.get(pId).get(nId) / (double) numberOfRepetitions);
            tmpVal = (tmpVal > 1) ? 1.0 : tmpVal;
            nodePValues.get(pId).put(nId, tmpVal);
        }
    }

    /**
     * Compute raw p-values
     */
    void pValues() {
        var pathways = getPathwaysForPValue();
        double minPV = 1.0 / (double) numberOfRepetitions / 100.0;
        HashMap<String, double[]> randAccumulators = new HashMap<>();
        for (var p : pathways) {
            randAccumulators.put(p.getId(), new double[numberOfRepetitions]);
        }
        for (var p : repository.getVirtualPathways()) {
            randAccumulators.put(p, new double[numberOfRepetitions]);
        }
        initVirtualPathwaysPValues();
        var expChange = expressions.entrySet().stream().filter(diffExpFilteringPredicate).mapToDouble(Map.Entry::getValue).toArray();
        var nodes = new ArrayList<>(expressions.keySet());
        for (var i = 0; i < numberOfRepetitions; i++) {
            notifyObservers("pValueIteration", i);
            var tmp = initSelf(expChange, nodes, pathways);
            for (var p : pathways) {
                incrementCounters(p, tmp, i, randAccumulators);
            }
            for (var p : repository.getVirtualPathways()) {
                incrementCounters(p, tmp, i, randAccumulators);
            }
        }
        var medCalc = new Median();
        for (var p : pathways) {
            notifyObservers("pValueFinalizing", p);
            finalizePValue(p.getId(), medCalc, randAccumulators, p, p.getGraph().getNodes().values(), minPV);
        }
        for (var p : repository.getVirtualPathways()) {
            notifyObservers("pValueFinalizingVirtual", p);
            finalizePValue(p, medCalc, randAccumulators, null, repository.getNodesOfVirtualPathway(p), minPV);
        }
    }

    /**
     * Adjust p-values
     */
    void adjustPValues() {
        var pathways = new String[pValues.size()];
        var pv = new double[pValues.size()];
        pathways = pValues.keySet().toArray(pathways);
        for (int i = 0; i < pathways.length; i++) {
            pv[i] = pValues.get(pathways[i]);
        }
        pv = pValueAdjuster.adjust(pv);
        for (int i = 0; i < pathways.length; i++) {
            adjustedPValues.put(pathways[i], pv[i]);
        }
    }

    /**
     * Gets object which sorts nodes first in order of increasing upstream nodes, then in order of increasing
     * in-degree, and finally in order of decreasing out-degree.
     *
     * @param g a pathway graph
     * @return a comparator to use with Collections.sort
     */
    private Comparator<NodeInterface> pathwayNodesComparator(@NotNull GraphInterface g) {
        Comparator<NodeInterface> c2 = Comparator.comparingInt(g::inDegree), c3 = Comparator.comparingInt(g::outDegree);
        return c2.thenComparing(c3.reversed());
    }

    /**
     * Get the list of nodes for the current pathway sorted by using a pseudo-topological ordering
     *
     * @param p a pathway
     * @return a list of sorted nodes
     */
    protected List<NodeInterface> getSortedNodes(@NotNull PathwayInterface p) {
        GraphInterface g = p.getGraph();
        List<NodeInterface> nodes;
        if (sortedNodes.containsKey(p)) {
            nodes = sortedNodes.get(p);
        } else {
            nodes = new ArrayList<>(g.getNodes().values());
            nodes = sortNodes(nodes, g); //Sort nodes to reduce recursion
            sortedNodes.put(p, nodes);
        }
        return nodes;
    }

    /**
     * Sort nodes using a topological ordering algorithm
     *
     * @param nodes a list of nodes
     * @param g     a graph
     * @return a list of sorted nodes
     */
    @NotNull
    private List<NodeInterface> sortNodes(@NotNull List<NodeInterface> nodes, GraphInterface g) {
        nodes.sort(pathwayNodesComparator(g));
        ArrayList<NodeInterface> sorted = new ArrayList<>(nodes.size());
        HashSet<NodeInterface> visited = new HashSet<>();
        for (NodeInterface n : nodes) {
            if (!visited.contains(n)) {
                Stack<NodeInterface> s = new Stack<>();
                s.push(n);
                while (!s.isEmpty()) {
                    NodeInterface curr = s.pop();
                    if (visited.contains(curr)) continue;
                    visited.add(curr);
                    sorted.add(curr);
                    g.outgoingNodesStream(curr)
                            .sorted(pathwayNodesComparator(g))
                            .filter(nxt -> !visited.contains(nxt))
                            .forEach(s::push);
                }
            }
        }
        assert sorted.size() == nodes.size();
        return sorted;
    }

    /**
     * Runs MITHrIL computation on a single pathway
     *
     * @param p a Pathway on which mithril will be executed
     */
    void singlePathwayRun(@NotNull PathwayInterface p) {
        if (p.hasGraph()) {
            notifyObservers("computingOn", p);
            GraphInterface g = p.getGraph();
            if (g.countNodes() > 0 && g.countEdges() > 0) {
                notifyObservers("sortingNodes", p);
                notifyObservers("computingPerturbations", p);
                for (NodeInterface n : getSortedNodes(p)) {
                    perturbation(n, p);
                }
                notifyObservers("computingImpactFactor", p);
                impactFactor(p);
                notifyObservers("computingAccumulator", p);
                accumulation(p);
            }
            notifyObservers("doneComputingOn", p);
        }
    }

    /**
     * Runs MITHrIL computation on a single virtual pathway
     *
     * @param p a virtual pathway on which mithril will be executed
     */
    void singleVirtualPathwayRun(String p) {
        notifyObservers("computingOnVirtual", p);
        //No perturbation computation since virtual pathways are part of real pathways
        notifyObservers("computingImpactFactorVirtual", p);
        impactFactor(p);
        notifyObservers("computingAccumulatorVirtual", p);
        accumulation(p);
        notifyObservers("doneComputingOnVirtual", p);
    }

    /**
     * Prepares the workflow for a new execution
     *
     * @return This object for a fluent interface
     */
    @Override
    public AlgorithmInterface<PathwayAnalysisResult> init() {
        super.init();
        expressions = null;
        repository = null;
        pValueCombiner = null;
        pValueAdjuster = null;
        noPValue = false;
        visitedPerturbations = new HashMap<>();
        count = new HashMap<>();
        nodePValues = new HashMap<>();
        perturbations = new HashMap<>();
        nodeAccumulators = new HashMap<>();
        accumulators = new HashMap<>();
        impactFactors = new HashMap<>();
        probabilities = new HashMap<>();
        probabilitiesNetwork = new HashMap<>();
        correctedAccumulators = new HashMap<>();
        pValues = new HashMap<>();
        adjustedPValues = new HashMap<>();
        sortedNodes = new HashMap<>();
        return this;
    }

    /**
     * Clears this algorithm to free memory
     *
     * @return This object for a fluent interface
     */
    @Override
    public AlgorithmInterface<PathwayAnalysisResult> clear() {
        super.clear();
        random = null;
        expressions = null;
        repository = null;
        pValueCombiner = null;
        pValueAdjuster = null;
        visitedPerturbations.clear();
        nodePValues.clear();
        perturbations.clear();
        nodeAccumulators.clear();
        accumulators.clear();
        impactFactors.clear();
        probabilities.clear();
        probabilitiesNetwork.clear();
        correctedAccumulators.clear();
        pValues.clear();
        adjustedPValues.clear();
        sortedNodes.clear();
        return this;
    }

    /**
     * Runs MITHrIL algorithm.
     * <p>
     * Requires parameter <code>expressions</code> of type <code>HashMap&lt;String, Double&gt;</code>.</p>
     * <p>
     * Requires parameter <code>repository</code> of type <code>RepositoryInterface</code>.</p>
     * <p>
     * Requires parameter <code>random</code> of type <code>Random</code>.</p>
     * <p>
     * Optional parameter <code>numberOfRepetitions</code> of type <code>int</code>.</p>
     * <p>
     * Optional parameter <code>probabilityComputation</code> of type <code>ProbabilityComputationInterface</code>.</p>
     * <p>
     * Optional parameter <code>pValueCombiner</code> of type <code>com.alaimos.Commons.Math.PValue.Combiner</code>.</p>
     * <p>
     * Optional parameter <code>pValueAdjuster</code> of type <code>com.alaimos.Commons.Math.PValue.Adjuster</code>.</p>
     */
    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        expressions = (Map<String, Double>) getParameterNotNull("expressions", Map.class);
        repository = getParameterNotNull("repository", RepositoryInterface.class);
        random = getParameterNotNull("random", Random.class);
        numberOfRepetitions = getOptionalParameter("numberOfRepetitions", Integer.class).orElse(2001);
        probabilityComputation = getOptionalParameter("probabilityComputation", ProbabilityComputationInterface.class)
                .orElse(this::defaultProbabilityComputation);
        pValueCombiner = getOptionalParameter("pValueCombiner", Combiner.class).orElse(Combiners::stouffer);
        pValueAdjuster = getOptionalParameter("pValueAdjuster", Adjuster.class).orElse(Adjusters::benjaminiHochberg);
        notifyObservers("startingMITHrILComputation");
        for (var p : repository) {
            this.singlePathwayRun(p);
        }
        for (var p : repository.getVirtualPathways()) {
            this.singleVirtualPathwayRun(p);
        }
        notifyObservers("computingPValues");
        pValues();
        notifyObservers("adjustingPValues");
        adjustPValues();
        notifyObservers("doneMITHrILComputation");
        output = new PathwayAnalysisResult(perturbations, nodeAccumulators, accumulators, impactFactors, probabilities,
                probabilitiesNetwork, correctedAccumulators, nodePValues, pValues, adjustedPValues);
    }

    /**
     * Run method used internally in pValue computation
     */
    protected void internalRun(List<PathwayInterface> pathways) {
        for (PathwayInterface p : pathways) {
            for (NodeInterface n : getSortedNodes(p)) {
                perturbation(n, p);
            }
            accumulation(p);
        }
        for (var p : repository.getVirtualPathways()) {
            accumulation(p);
        }
    }

    /**
     * The default implementation of probability computation. Uses an hypergeometric distribution do estimate the
     * probability of obtaining a number of differentially expressed elements greater than the observed one in a
     * pathway.
     *
     * @param allExperimentNodes  all nodes for which expressions were determined
     * @param allNodesInPathway   the nodes in a pathway for which expressions where determined
     * @param allDENodes          all differentially expressed nodes
     * @param allDENodesInPathway differentially expressed nodes in a pathway
     * @return a probability
     */
    double defaultProbabilityComputation(List<String> allExperimentNodes, List<String> allNodesInPathway,
                                         List<String> allDENodes, List<String> allDENodesInPathway) {
        int p = allExperimentNodes.size(), m = allNodesInPathway.size(),
                k = allDENodes.size(), x = allDENodesInPathway.size();
        HypergeometricDistribution hp = new HypergeometricDistribution(p, m, k);
        return hp.upperCumulativeProbability(x);
    }
}
