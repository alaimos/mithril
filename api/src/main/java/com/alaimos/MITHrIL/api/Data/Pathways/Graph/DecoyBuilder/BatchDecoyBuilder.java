package com.alaimos.MITHrIL.api.Data.Pathways.Graph.DecoyBuilder;

import com.alaimos.Commons.Algorithm.Pipelines.Batch;
import com.alaimos.MITHrIL.Data.Pathway.Interface.PathwayFactoryInterface;
import com.alaimos.MITHrIL.Data.Pathway.Interface.PathwayInterface;
import com.alaimos.MITHrIL.Data.Pathway.Interface.RepositoryInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Salvatore Alaimo, Ph.D.
 * @version 2.0.0.0
 * @since 31/12/2015
 */
public class BatchDecoyBuilder extends Batch<PathwayInterface> {

    public BatchDecoyBuilder() {
        super(DecoyBuilder.class);
    }

    /**
     * Merges all the pathways in a repository building a new repository with a single pathway graph
     */
    @Override
    public void run() {
        var r = getParameterNotNull("repository", RepositoryInterface.class);
        setParameter("notParallel", true);
        var commonParams = new HashMap<String, Object>();
        commonParams.put("allNodes", getParameter("allNodes"));
        commonParams.put("idToNodes", getParameter("idToNodes"));
        commonParams.put("random", getParameter("random"));
        var factory = getParameter("factory", PathwayFactoryInterface.class);
        if (factory != null) commonParams.put("factory", factory);
        setParameter("common", commonParams);
        var batches = new ArrayList<Map<String, Object>>();
        for (var p : r) {
            var tmp = new HashMap<String, Object>();
            tmp.put("pathway", p);
            batches.add(tmp);
        }
        setParameter("batch", batches);
        super.run();
    }
}
