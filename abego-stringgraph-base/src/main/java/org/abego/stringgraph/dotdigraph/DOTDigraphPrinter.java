package org.abego.stringgraph.dotdigraph;

import org.abego.stringgraph.core.StringGraph;

import java.io.PrintStream;

/**
 * Support for the "digraph" in the 
 * (<a href="https://graphviz.org/doc/info/lang.html">DOT Language</a>).
 */
public interface DOTDigraphPrinter {
    interface PrintOptions {
        default String getName() {
            return "";
        }

        default boolean getSortEdges() {
            return false;
        }
    }

    void printDOTDigraph(PrintStream out, StringGraph graph, PrintOptions options);

    default void printDOTDigraph(PrintStream out, StringGraph graph) {
        printDOTDigraph(out, graph, new PrintOptions() {
        });
    }

    default void printDOTDigraph(PrintStream out, StringGraph graph, String name) {
        printDOTDigraph(out, graph, new PrintOptions() {
            @Override
            public String getName() {
                return name;
            }
        });
    }
}
