package com.alaimos.MITHrIL.app.Data.Writers.PHENSIM;

import com.alaimos.MITHrIL.api.Commons.Utils;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Edge;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Graph;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Repository;
import com.alaimos.MITHrIL.api.Data.Writer.AbstractDataWriter;
import com.alaimos.MITHrIL.app.Algorithms.PHENSIM.SimulationOutput;
import com.alaimos.MITHrIL.app.Data.Records.RepositoryMatrix;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.sbml.jsbml.*;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class SBMLWriter extends AbstractDataWriter<SimulationOutput> {

    private final Repository r;
    private final RepositoryMatrix rm;
    private final Map<String, Species> speciesMap = new HashMap<>();
    private final Map<String, Double> absoluteWeights = new HashMap<>();
    private Graph graph = null;
    private Model model = null;
    private Compartment compartment = null;

    public SBMLWriter(Repository r, RepositoryMatrix rm) {
        this.r  = r;
        this.rm = rm;
    }

    /**
     * Clean-up the node identifier so it is valid for the SBML standard. Entrez gene identifiers are prefixed with
     * "entrezId_" since number only identifiers are not supported. All "-" are replaced with "_". All ":" are replaced
     * with a "_".
     *
     * @param id An identifier
     * @return A valid identifier for SBML
     */
    public String cleanNodeIdentifier(String id) {
        var result = id;
        if (NumberUtils.isParsable(id)) {
            result = "entrezId_" + result;
        } else if (Character.isDigit(result.charAt(0))) {
            result = "node_" + result;
        }
        return result.replace("-", "_").replace(":", "_");
    }

    /**
     * Compute the total weight of a given node. The total weight is obtained by summing up the absolute weight values
     * for all outgoing edges.
     *
     * @param u The node for which the total weight is computed
     * @return the total weight
     */
    private double absoluteTotalWeight(String u) {
        try {
            if (absoluteWeights.containsKey(u)) return absoluteWeights.get(u);
            var source = graph.node(u);
            double weight = 0.0;
            for (Edge e : graph.outgoingEdges(source)) {
                weight += Math.abs(e.weight());
            }
            absoluteWeights.put(u, weight);
            return weight;
        } catch (NullPointerException ignore) {
        }
        return Double.NaN;
    }

    /**
     * Add all species to the model
     *
     * @param data phensim results
     */
    public void createSpecies(@NotNull SimulationOutput data) {
        var indicesMap = rm.pathwayMatrix().id2Index();
        var avgPerturbations = data.nodePerturbationsAverage();
        var sdPerturbations = data.nodePerturbationsStdDev();
        var activityScores = data.nodeActivityScores();
        var pValues = data.nodePValues();
        var fdrValues = data.nodePValuesAdjusted();
        for (var n : graph.nodes().values()) {
            var id = n.id();
            var idx = indicesMap.getInt(id);
            var cleanedId = this.cleanNodeIdentifier(id);
            var s = model.createSpecies(cleanedId, compartment);
            s.setName(n.name());
            s.setValue(avgPerturbations[idx]);
            try {
                s.setNotes("Perturbation: " + avgPerturbations[idx] + " +/- " + sdPerturbations[idx] +
                                   "<br />Activity Score: " + activityScores[idx] +
                                   "<br />p-value: " + pValues[idx] +
                                   "<br />FDR: " + fdrValues[idx]);
            } catch (XMLStreamException ignore) {
            }
            var type = n.type().name();
            s.setSBOTerm(switch (type) {
                case "GENE" -> SBO.getGene();
                case "COMPOUND" -> 299; // metabolite
                case "MIRNA" -> 316; // microRNA
                default -> SBO.getEntity();
            });
            speciesMap.put(cleanedId, s);
        }
    }

    /**
     * Create all reactions
     */
    public void createReactions() {
        var counter = 0;
        for (var edgesFromSource : graph.edges().entrySet()) {
            var sourceId = edgesFromSource.getKey();
            var source = this.cleanNodeIdentifier(sourceId);
            var container = edgesFromSource.getValue();
            for (var edgeEntry : container.entrySet()) {
                var edge = edgeEntry.getValue();
                var target = this.cleanNodeIdentifier(edgeEntry.getKey());
                var weight = edge.weight() / absoluteTotalWeight(sourceId);
                if (Double.isFinite(weight) && weight != 0) {
                    var reaction = model.createReaction("edge_" + (++counter));
                    if (weight > 0) reaction.setSBOTerm(656); // activation
                    else reaction.setSBOTerm(169); // inhibition
                    reaction.setReversible(false);
                    var reactant = reaction.createReactant(speciesMap.get(source));
                    reactant.setSBOTerm(15);
                    reactant.setValue(weight);
                    reaction.createProduct(speciesMap.get(target)).setSBOTerm(11);
                }
            }
        }
    }

    /**
     * Write data
     *
     * @param data the data that will be written into a file
     * @return this object for a fluent interface
     */
    @Override
    public SBMLWriter write(@NotNull SimulationOutput data) throws IOException {
        speciesMap.clear();
        String pathway = r.get().id();
        graph = r.get().graph();
        var document = new SBMLDocument(3, 1);
        model       = document.createModel(pathway + "_model");
        compartment = model.createCompartment(pathway + "_compartment");
        createSpecies(data);
        createReactions();
        try {
            org.sbml.jsbml.SBMLWriter.write(document, file, "PHENSIM", Utils.getCurrentVersion());
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
        return this;
    }
}
