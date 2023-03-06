package org.abego.stringgraph.dotdigraph;

import org.abego.stringgraph.core.Edge;
import org.abego.stringgraph.core.StringGraph;
import org.abego.stringgraph.internal.StringUtil;

import java.io.PrintStream;

final class DOTDigraphPrinterImpl implements DOTDigraphPrinter {
    private static final DOTDigraphPrinter INSTANCE = new DOTDigraphPrinterImpl();

    private DOTDigraphPrinterImpl() {
    }

    public static DOTDigraphPrinter getInstance() {
        return INSTANCE;
    }

    public void printDOTDigraph(PrintStream out, StringGraph graph,
                                DOTDigraphPrinter.PrintOptions options) {
        String name = options.getName();
        boolean sortEdges = options.getSortEdges();

        out.print("digraph ");
        out.print(StringUtil.quoted2(name));
        out.println(" {");
        Iterable<Edge> edges = 
                sortEdges ? graph.edges().sorted() : graph.edges();
        edges.forEach(e -> {
            out.print("    ");
            out.print(StringUtil.quoted2(e.getFromNode().id()));
            out.print(" -> ");
            out.print(StringUtil.quoted2(e.getToNode().id()));
            String label = e.getLabel();
            if (!label.isEmpty()) {
                out.print(" [label=");
                out.print(e.getLabel());
                out.print("]");
            }
            out.println(";");
        });
        out.println("}");
    }

}
