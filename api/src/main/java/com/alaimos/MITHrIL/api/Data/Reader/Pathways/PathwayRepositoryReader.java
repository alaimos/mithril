package com.alaimos.MITHrIL.api.Data.Reader.Pathways;

import com.alaimos.MITHrIL.api.Commons.IOUtils;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.*;
import com.alaimos.MITHrIL.api.Data.Reader.AbstractRemoteDataReader;
import com.alaimos.MITHrIL.api.Data.Reader.RemoteDataReaderInterface;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PathwayRepositoryReader extends AbstractRemoteDataReader<Repository> {

    private static final String SEPARATOR = "\t";
    private static final String LIST_SEPARATOR = ";";
    private static final String OTHER_SEPARATOR = ",";
    private static final String PATHWAYS_HEADER = "# Pathways";
    private static final String NODES_HEADER = "# Nodes";
    private static final String EDGES_HEADER = "# Edges";
    private static final String ENDPOINTS_HEADER = "# Endpoints";
    private final HashMap<String, Node> allNodes = new HashMap<>();

    public PathwayRepositoryReader(String url) {
        persisted(true).url(url);
    }

    @Override
    public RemoteDataReaderInterface<Repository> url(String url) {
        super.url(url);
        file("pathway-repository-" + IOUtils.getName(url));
        return this;
    }

    /**
     * Create a pathway object from a line of text
     *
     * @param line the line to parse
     * @return a pathway object
     */
    @Contract("_ -> new")
    private @NotNull Pathway readPathway(@NotNull String line) {
        String[] sLine = line.split(SEPARATOR, -1);
        if (sLine.length != 5 || sLine[0].trim().isEmpty() || sLine[1].trim().isEmpty()) {
            throw new RuntimeException("Incorrect pathway line format -->" + line + "<--.");
        }
        return new Pathway(sLine[0].trim(), sLine[1].trim(), new Graph(), sLine[4].trim());
    }

    /**
     * Create a node object from a line of text
     *
     * @param line the line to parse
     * @return a node object
     */
    @Contract("_ -> new")
    private @NotNull Node readNode(@NotNull String line) {
        String[] sLine = line.split(SEPARATOR, -1);
        if ((sLine.length != 3 && sLine.length != 4) || sLine[0].trim().isEmpty()) {
            throw new RuntimeException("Incorrect node line format -->" + line + "<--.");
        }
        var aliases = Arrays.stream(sLine[2].split(LIST_SEPARATOR))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();
        return new Node(sLine[0].trim(), sLine[1].trim(), (sLine.length != 4) ? "other" : sLine[3].trim(), aliases);
    }

    /**
     * Create an edge object from a line of text
     *
     * @param line the line to parse
     * @return an edge object
     */
    @Nullable
    private Pair<Edge, String> readEdge(@NotNull String line) {
        String[] sLine = line.split(SEPARATOR, -1);
        if (sLine.length != 5) {
            throw new RuntimeException("Incorrect edge line format -->" + line + "<--.");
        }
        if (Arrays.stream(sLine).mapToInt(s -> ((s.trim().isEmpty()) ? 1 : 0)).sum() > 0) {
            throw new RuntimeException("Incorrect edge line format -->" + line + "<--.");
        }
        var source = allNodes.get(sLine[0].trim());
        var target = allNodes.get(sLine[1].trim());
        if (source == null || target == null) return null;
        return Pair.of(new Edge(source, target, new EdgeDetail(sLine[2].trim(), sLine[3].trim())), sLine[4].trim());
    }

    private void readEndpoints(@NotNull String line, Repository repository) {
        String[] sLine = line.split(SEPARATOR, -1);
        if (sLine.length != 2) {
            throw new RuntimeException("Incorrect endpoint line format -->" + line + "<--.");
        }
        String[] endpoints = sLine[1].split(OTHER_SEPARATOR, -1);
        List<String> endpointsList = Arrays.stream(endpoints).map(String::trim).filter(s -> !s.isEmpty()).toList();
        repository.get(sLine[0].trim()).graph().setEndpoints(endpointsList);
    }

    @Override
    protected Repository realReader() throws IOException {
        Repository result = new Repository();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(getInputStream()))) {
            String line;
            boolean isPathway = false, isNode = false, isEdge = false, isEndpoints = false;
            while ((line = r.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    if (line.equalsIgnoreCase(PATHWAYS_HEADER)) {
                        isPathway   = true;
                        isEndpoints = isNode = isEdge = false;
                    } else if (line.equalsIgnoreCase(NODES_HEADER)) {
                        isNode      = true;
                        isEndpoints = isPathway = isEdge = false;
                    } else if (line.equalsIgnoreCase(EDGES_HEADER)) {
                        isEdge      = true;
                        isEndpoints = isPathway = isNode = false;
                    } else if (line.equalsIgnoreCase(ENDPOINTS_HEADER)) {
                        isEndpoints = true;
                        isEdge      = isPathway = isNode = false;
                    } else {
                        if (isPathway) {
                            var p = readPathway(line);
                            result.add(p);
                        } else if (isNode) {
                            var n = readNode(line);
                            allNodes.put(n.id(), n);
                        } else if (isEdge) {
                            var edgePathwayPair = readEdge(line);
                            if (edgePathwayPair == null) continue;
                            var p = result.get(edgePathwayPair.right());
                            if (p == null) continue;
                            p.graph().addEdge(edgePathwayPair.left());
                        } else if (isEndpoints) {
                            readEndpoints(line, result);
                        }
                    }
                }
            }
        }
        return result;
    }
}
