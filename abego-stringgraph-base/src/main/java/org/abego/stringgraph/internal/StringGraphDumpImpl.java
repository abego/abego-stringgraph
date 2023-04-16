/*
 * MIT License
 *
 * Copyright (c) 2023 Udo Borkowski, (ub@abego.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.abego.stringgraph.internal;

import org.abego.stringgraph.core.Edge;
import org.abego.stringgraph.core.Edges;
import org.abego.stringgraph.core.Node;
import org.abego.stringgraph.core.Nodes;
import org.abego.stringgraph.core.Properties;
import org.abego.stringgraph.core.StringGraph;
import org.abego.stringgraph.core.StringGraphDump;
import org.abego.stringgraph.internal.commons.StringUtil;
import org.eclipse.jdt.annotation.Nullable;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class StringGraphDumpImpl implements StringGraphDump {
    private final StringGraph graph;
    private final Map<String, String> translation;

    private StringGraphDumpImpl(StringGraph graph, Map<String, String> translation) {
        this.graph = graph;
        this.translation = translation;
    }

    public static StringGraphDump createStringGraphDump(StringGraph graph, Map<String, String> translation) {
        return new StringGraphDumpImpl(graph, translation);
    }

    public static StringGraphDump createStringGraphDump(StringGraph graph) {
        //noinspection unchecked
        return new StringGraphDumpImpl(graph, Collections.EMPTY_MAP);
    }

    @Override
    public void write(PrintWriter writer) {

        Nodes nodes = graph.nodes();
        Comparator<Node> nodeComparator =
                (a, b) -> StringUtil.compareToIgnoreCaseStable(text(a, translation), text(b, translation));

        nodes.stream().sorted(nodeComparator).forEach(fromNode -> {
            Edges edges = graph.edgesFromNode(fromNode.id());
            int edgeCount = edges.getSize();
            String fromNodeText = text(fromNode, translation);
            if (edgeCount == 0) {
                writer.println(fromNodeText + propertiesText(fromNode) + " .");
            } else if (edgeCount == 1) {
                Edge edge = edges.iterator().next();
                writer.println(fromNodeText + propertiesText(fromNode) +
                        " " + labeltext(edge, translation) +
                        " " + text(edge.getToNode(), translation) +
                        " .");
            } else {
                writer.println(fromNodeText + propertiesText(fromNode));
                Comparator<Edge> edgeComparator =
                        (a, b) -> {
                            String la = labeltext(a, translation);
                            String lb = labeltext(b, translation);
                            int r = StringUtil.compareToIgnoreCaseStable(la, lb);
                            return r != 0
                                    ? r
                                    : StringUtil.compareToIgnoreCaseStable(
                                    text(a.getToNode(), translation), text(b.getToNode(), translation));
                        };
                int[] i = new int[]{0};
                edges.stream().sorted(edgeComparator).forEach(edge -> {
                    i[0]++;
                    writer.println(
                            "\t" + labeltext(edge, translation) +
                                    " " + text(edge.getToNode(), translation) +
                                    (i[0] < edgeCount ? " ;" : " ."));

                });
            }
        });
    }

    private static String text(Node node, Map<String, String> idTranslation) {
        @Nullable String result = idTranslation.get(node.id());
        return result != null ? result : node.getText();
    }

    private static String labeltext(Edge edge, Map<String, String> translation) {
        @Nullable String result = translation.get(edge.getLabel());
        return result != null ? result : edge.getLabelText();
    }

    private String propertiesText(Node node) {
        Properties props = graph.getNodeProperties(node.id());

        return props.getSize() == 0
                ? ""
                : "{" + props.propertyNames().sorted().map(props::getProperty)
                .map(p -> p.getName() + ": " + p.getValue())
                .collect(Collectors.joining(", ")) +
                "}";
    }


}
