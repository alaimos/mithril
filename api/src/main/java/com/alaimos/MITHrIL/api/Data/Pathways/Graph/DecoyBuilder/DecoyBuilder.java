package com.alaimos.MITHrIL.api.Data.Pathways.Graph.DecoyBuilder;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is used to create decoy pathways
 */
public class DecoyBuilder {

    public DecoyBuilder() {
    }

    /**
     * Create a decoy pathway by selecting random nodes from the repository and connecting with the same topology
     * as the original pathway
     */
    public Pathway build(@NotNull Pathway p, @NotNull List<String> allNodes, @NotNull Map<String, Node> idToNodes, Random rng) {
        var g = p.graph();
        var decoyGraph = new Graph();
        var decoyPathway = new Pathway(p.id() + "-decoy", p.name() + " - Decoy", decoyGraph, p.categories());
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
            g.addNode(new Node(newId, oldId, idToNodes.get(newId).type(), Collections.emptyList()));
        }
        g.getEdgesStream().forEach(e -> {
            String start = oldToNew.get(e.source().id()), end = oldToNew.get(e.source().id());
            var clonedDetails = e.details().stream().map(e1 -> (EdgeDetail) e1.clone()).collect(Collectors.toList());
            decoyGraph.addEdge(new Edge(decoyGraph.getNode(start), decoyGraph.getNode(end), clonedDetails));
        });
        return decoyPathway;
    }
}
