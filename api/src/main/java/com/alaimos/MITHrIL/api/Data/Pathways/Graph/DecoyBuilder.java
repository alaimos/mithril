package com.alaimos.MITHrIL.api.Data.Pathways.Graph;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is used to create decoy pathways
 */
public class DecoyBuilder {

    private static final DecoyBuilder INSTANCE = new DecoyBuilder();

    private DecoyBuilder() {
    }

    /**
     * Check if a pathway is a decoy
     *
     * @param p pathway
     * @return true, if the pathway is a decoy
     */
    public static boolean isDecoy(@NotNull Pathway p) {
        return p.hasCategory("decoy-pathway");
    }

    /**
     * Get a list of decoy pathways within a repository
     *
     * @param r repository
     * @return list of decoy pathways
     */
    public static List<String> listDecoysWithinRepository(@NotNull Repository r) {
        return r.getPathwayIdsByCategory("decoy-pathway");
    }

    public static DecoyBuilder getInstance() {
        return INSTANCE;
    }

    /**
     * Create a decoy pathway by selecting random nodes from the repository and connecting with the same topology as the
     * original pathway
     */
    public Pathway buildSingleDecoy(
            @NotNull Pathway p, @NotNull List<String> allNodes, @NotNull Map<String, Node> idToNodes,
            @NotNull Random rng
    ) {
        var g = p.graph();
        var decoyCategories = new ArrayList<>(p.categories());
        decoyCategories.add("decoy-pathway");
        var decoyGraph = new Graph();
        var decoyPathway = new Pathway(p.id() + "-decoy", p.name() + " - Decoy", decoyGraph, decoyCategories);
        var oldToNew = new HashMap<String, String>();
        var used = new HashSet<String>();
        for (var n : g.nodes().values()) {
            String newId = null, oldId = n.id(), tmp;
            while (newId == null) {
                tmp = allNodes.get(rng.nextInt(allNodes.size()));
                if (!tmp.equals(oldId) && used.add(tmp)) {
                    newId = tmp;
                }
            }
            oldToNew.put(oldId, newId);
            decoyGraph.addNode(new Node(newId, oldId, idToNodes.get(newId).type(), Collections.emptyList()));
        }
        g.edgesStream().forEach(e -> {
            String start = oldToNew.get(e.source().id()), end = oldToNew.get(e.target().id());
            var clonedDetails = e.details().stream().map(e1 -> (EdgeDetail) e1.clone()).collect(Collectors.toList());
            decoyGraph.addEdge(new Edge(decoyGraph.node(start), decoyGraph.node(end), clonedDetails));
        });
        return decoyPathway;
    }

    /**
     * Create decoy pathways for all pathways in a repository. The decoys are added to the repository.
     */
    public void buildAllDecoys(
            @NotNull Repository r, @NotNull List<String> allNodes, @NotNull Map<String, Node> idToNodes,
            @NotNull Random rng, boolean parallel
    ) {
        var pathwayStream = r.stream();
        if (parallel) pathwayStream = pathwayStream.parallel();
        var decoys = pathwayStream.filter(p -> !isDecoy(p))
                                  .map(p -> buildSingleDecoy(p, allNodes, idToNodes, rng))
                                  .toList();
        r.addAll(decoys);
    }
}
