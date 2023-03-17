package com.alaimos.MITHrIL.api.Data.Reader.Pathways;

import com.alaimos.MITHrIL.api.Commons.IOUtils;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.*;
import com.alaimos.MITHrIL.api.Data.Reader.AbstractRemoteDataReader;
import com.alaimos.MITHrIL.api.Data.Reader.RemoteDataReaderInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReactomeRepositoryReader extends AbstractRemoteDataReader<Repository> {

    private static final String SEPARATOR = "\t";
    private static final String OTHER_SEPARATOR = ",";

    private final Repository repository;
    private final Map<String, Node> allNodes = new HashMap<>();

    public ReactomeRepositoryReader(String url, Repository r) {
        persisted(true).url(url);
        repository = r;
    }

    @Override
    public RemoteDataReaderInterface<Repository> url(String url) {
        super.url(url);
        file("pathway-repository-reactome-" + IOUtils.getName(url));
        return this;
    }

    private void findAllNodes() {
        for (var p : repository) {
            allNodes.putAll(p.graph().nodes());
        }
    }

    private void createPathway(String @NotNull [] line) {
        var pathwayId = line[0];
        if (!repository.contains(pathwayId)) {
            repository.add(new Pathway(pathwayId, line[1], new Graph(), List.of("reactome")));
        }
    }

    private @Nullable Node readNode(String @NotNull [] line, int field) {
        var nodeId = line[field];
        var nodeName = line[field + 1];
        if (nodeId.equalsIgnoreCase("na")) return null;
        if (allNodes.containsKey(nodeId)) {
            return allNodes.get(nodeId);
        } else {
            var nodeType = (nodeId.startsWith("chebi:") || nodeId.startsWith("cpd:")) ? "compound" : "gene";
            var node = new Node(nodeId, nodeName, nodeType, List.of());
            allNodes.put(nodeId, node);
            return node;
        }
    }

    private void createEdge(String[] line) {
        var source = readNode(line, 2);
        var target = readNode(line, 4);
        if (source == null || target == null) return;
        var pathway = repository.get(line[0]).graph();
        var edge = new Edge(source, target, new EdgeDetail(line[7], line[8]));
        pathway.addEdge(edge);
    }

    private void readEndpoints(String @NotNull [] line) {
        var endpoints = line[9].split(OTHER_SEPARATOR, -1);
        var epList = Arrays.stream(endpoints).map(String::trim).filter(trim -> !trim.isEmpty()).collect(Collectors.toList());
        if (epList.size() > 0) {
            var pathway = repository.get(line[0]).graph();
            pathway.setEndpoints(epList);
        }
    }

    @Override
    protected Repository realReader() throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(getInputStream()))) {
            String line, pathwayId;
            String[] sLine;
            boolean create;
            findAllNodes();
            while ((line = r.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    if (line.startsWith("pathwayId")) continue;
                    sLine = line.split(SEPARATOR, -1);
                    pathwayId = sLine[0];
                    if (sLine.length != 11 || sLine[10].equalsIgnoreCase("true")) continue;
                    create = !repository.contains(pathwayId);
                    if (create) createPathway(sLine);
                    createEdge(sLine);
                    if (create) readEndpoints(sLine);
                }
            }
        }
        return repository;
    }
}
