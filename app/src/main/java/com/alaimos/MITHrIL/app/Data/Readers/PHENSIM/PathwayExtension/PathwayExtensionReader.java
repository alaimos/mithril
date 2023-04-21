package com.alaimos.MITHrIL.app.Data.Readers.PHENSIM.PathwayExtension;

import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Edge;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.EdgeDetail;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Graph;
import com.alaimos.MITHrIL.api.Data.Pathways.Graph.Node;
import com.alaimos.MITHrIL.api.Data.Reader.AbstractDataReader;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Read a file containing nodes and edges to extend the metapathway graph with custom nodes and edges.
 */
public class PathwayExtensionReader extends AbstractDataReader<Graph> {

    public PathwayExtensionReader() {
        isGzipped = false;
    }

    @Override
    public PathwayExtensionReader file(@NotNull File f) {
        file      = f;
        isGzipped = f.getName().endsWith(".gz");
        return this;
    }

    @Override
    protected Graph realReader() throws IOException {
        var extensionGraph = new Graph();
        isGzipped = file.getName().endsWith(".gz");
        try (var r = new BufferedReader(new InputStreamReader(getInputStream()))) {
            String line;
            String[] s;
            while ((line = r.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                s = line.split("\t", -1);
                if (s.length < 8) continue;
                var source = makeNode(extensionGraph, s[0], s[1], s[2]);
                var target = makeNode(extensionGraph, s[3], s[4], s[5]);
                extensionGraph.addEdge(new Edge(source, target, new EdgeDetail(s[6], s[7])));
            }
        }
        return extensionGraph;
    }

    private static @NotNull Node makeNode(@NotNull Graph g, String id, String name, String type) {
        if (g.hasNode(id)) return g.node(id);
        var node = new Node(id, name, type, List.of());
        g.addNode(node);
        return node;
    }
}
