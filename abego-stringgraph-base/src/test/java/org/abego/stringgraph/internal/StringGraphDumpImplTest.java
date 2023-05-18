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

import org.abego.stringgraph.core.StringGraph;
import org.abego.stringgraph.core.StringGraphDump;
import org.abego.stringgraph.core.StringGraphTest;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class StringGraphDumpImplTest {
    @Test
    void smokeTest() {
        StringGraph sample = StringGraphTest.getSample1();

        StringGraphDump dump = StringGraphDumpImpl.createStringGraphDump(
                sample, id -> "*" + id + "*");

        StringWriter writer = new StringWriter();
        dump.write(new PrintWriter(writer));
        
        assertEquals(""+
                "*a*{prop1: , prop2: foo} .\n" +
                "*b* .\n" +
                "*c* *cycle* *c* .\n" +
                "*d* ** *e* .\n" +
                "*e* .\n" +
                "*f* *h* *g* .\n" +
                "*g* .\n" +
                "*i* *cycle* *i* .\n" +
                "*m1* .\n" +
                "*m2* .\n" +
                "*m3* .\n" +
                "*o*\n" +
                "\t** *m3* ;\n" +
                "\t*field* *m1* ;\n" +
                "\t*field* *m2* .\n",writer.toString());
    }

}
