package org.abego.stringgraph.dotdigraph;

public final class DOTDigraphs {
    DOTDigraphs() {
        throw new UnsupportedOperationException();
    }
    
    public static DOTDigraphPrinter getDOTDigraphPrinter() {
        return DOTDigraphPrinterImpl.getInstance();
    }
}
