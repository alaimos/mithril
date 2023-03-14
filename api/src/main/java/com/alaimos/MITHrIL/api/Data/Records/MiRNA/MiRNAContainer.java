package com.alaimos.MITHrIL.api.Data.Records.MiRNA;

//import com.alaimos.MITHrIL.Data.Pathway.Factory.EnrichmentFactory;
//import com.alaimos.MITHrIL.Data.Pathway.Interface.Enrichment.EdgeEnrichmentInterface;
//import com.alaimos.MITHrIL.Data.Pathway.Interface.Enrichment.NodeEnrichmentInterface;
//import com.alaimos.MITHrIL.Data.Pathway.Interface.Enrichment.RepositoryEnrichmentInterface;
//import com.alaimos.MITHrIL.Data.Pathway.Type.EdgeSubType;
//import com.alaimos.MITHrIL.Data.Pathway.Type.EdgeType;
//import com.alaimos.MITHrIL.Data.Pathway.Type.NodeType;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Edge;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.EdgeDetail;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Graph;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Node;
import com.alaimos.MITHrIL.api.Data.Pathways.Types.EdgeSubtype;
import com.alaimos.MITHrIL.api.Data.Pathways.Types.EdgeType;
import com.alaimos.MITHrIL.api.Data.Pathways.Types.NodeType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public class MiRNAContainer extends HashMap<String, MiRNA> {

    /**
     * Add a new miRNA to the container
     *
     * @param m miRNA
     * @return true, if the miRNA was added, false otherwise
     */
    public boolean add(@NotNull MiRNA m) {
        if (!containsKey(m.id())) {
            this.put(m.id(), m);
            return true;
        }
        return false;
    }

    /**
     * Convert this container to a graph
     *
     * @param maxEvidenceType maximum evidence type to consider (stronger evidences have lower values)
     * @return a graph
     */
    public Graph toGraph(EvidenceType maxEvidenceType) {
        var miRNANodeType = NodeType.valueOf("MIRNA");
        var geneNodeType = NodeType.valueOf("GENE");
        var miRNATargetEdgeType = EdgeType.valueOf("MGREL");
        var miRNATargetEdgeSubType = EdgeSubtype.valueOf("MIRNA_INHIBITION");
        var miRNATFEdgeType = EdgeType.valueOf("MGREL");
        var miRNATFEdgeSubTypeInhibition = EdgeSubtype.valueOf("TFMIRNA_INHIBITION");
        var miRNATFEdgeSubTypeActivation = EdgeSubtype.valueOf("TFMIRNA_ACTIVATION");
        var g = new Graph();
        for (var m : values()) {
            var id = m.id();
            var miRNANode = new Node(id, id, miRNANodeType, List.of());
            g.addNode(miRNANode);
            m.targets().forEach(t -> {
                if (t.evidenceType().value() > maxEvidenceType.value()) return;
                var targetNode = g.node(t.id());
                if (targetNode == null) {
                    g.addNode(targetNode = new Node(t.id(), t.name(), geneNodeType, List.of()));
                }
                g.addEdge(new Edge(miRNANode, targetNode, new EdgeDetail(miRNATargetEdgeType, miRNATargetEdgeSubType)));
            });
            m.transcriptionFactors().forEach(tf -> {
                if (tf.evidenceType().value() > maxEvidenceType.value()) return;
                var tfNode = g.node(tf.id());
                if (tfNode == null) {
                    g.addNode(tfNode = new Node(tf.id(), tf.id(), geneNodeType, List.of()));
                }
                EdgeSubtype subtype = tf.type().equalsIgnoreCase("inhibition") ? miRNATFEdgeSubTypeInhibition : miRNATFEdgeSubTypeActivation;
                g.addEdge(new Edge(tfNode, miRNANode, new EdgeDetail(miRNATFEdgeType, subtype)));
            });
        }
        return g;
    }
}
