package org.abego.stringgraph.dotdigraph;

import org.abego.stringgraph.core.StringGraph;
import org.abego.stringgraph.core.StringGraphTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DOTDigraphPrinterTest {

    @Test
    void printDOTDigraph() {
        StringGraph graph = StringGraphTest.getSingleEdgeGraph();
        PrintStreamToBuffer stream = PrintStreamToBuffer.newPrintStreamToBuffer();

        DOTDigraphs.getDOTDigraphPrinter().printDOTDigraph(stream, graph);

		assertEqualLines("" +
                        "digraph \"\" {\n" +
                        "    \"from\" -> \"to\" [label=label];\n" +
                        "}\n",
                stream.toString());
    }

    @Test
    void printDOTDigraphWithName() {
        StringGraph graph = StringGraphTest.getSingleEdgeGraph();
        PrintStreamToBuffer stream = PrintStreamToBuffer.newPrintStreamToBuffer();

        DOTDigraphs.getDOTDigraphPrinter().printDOTDigraph(stream, graph, "name");

		assertEqualLines("" +
                        "digraph \"name\" {\n" +
                        "    \"from\" -> \"to\" [label=label];\n" +
                        "}\n",
                stream.toString());
    }

    @Test
    void printDOTDigraphWithOptions() {
        StringGraph graph = StringGraphTest.getSample1();
        PrintStreamToBuffer stream = PrintStreamToBuffer.newPrintStreamToBuffer();

        DOTDigraphs.getDOTDigraphPrinter().
                printDOTDigraph(stream, graph, new DOTDigraphPrinter.PrintOptions() {
            @Override
            public String getName() {
                return "myName";
            }

            @Override
            public boolean getSortEdges() {
                return true;
            }
        });

		assertEqualLines("" +
                        "digraph \"myName\" {\n" +
                        "    \"c\" -> \"c\" [label=cycle];\n" +
                        "    \"d\" -> \"e\";\n" +
                        "    \"f\" -> \"g\" [label=h];\n" +
                        "    \"i\" -> \"i\" [label=cycle];\n" +
                        "    \"o\" -> \"m3\";\n" +
                        "    \"o\" -> \"m1\" [label=field];\n" +
                        "    \"o\" -> \"m2\" [label=field];\n" +
                        "}\n",
                stream.toString());
    }

	/**
	 * Asserts the actual String has the same lines as the expected String (even
	 * though the action line separators may differ).
	 */
	private static void assertEqualLines(String expected, String actual) {
		String e = expected.replaceAll("\r","");
		String a = actual.replaceAll("\r","");
		assertEquals(e,a);
	}
}
