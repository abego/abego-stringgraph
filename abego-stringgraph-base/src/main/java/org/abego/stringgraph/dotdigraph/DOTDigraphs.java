package org.abego.stringgraph.dotdigraph;

import org.abego.commons.lang.exception.MustNotInstantiateException;

public final class DOTDigraphs {
    DOTDigraphs() {
        throw new MustNotInstantiateException();
    }
    
    public static DOTDigraphPrinter getDOTDigraphPrinter() {
        return DOTDigraphPrinterImpl.getInstance();
    }
}
