/*
 * MIT License
 *
 * Copyright (c) 2022 Udo Borkowski, (ub@abego.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.abego.stringgraph.store;

import org.abego.stringgraph.internal.FileUtil;
import org.abego.stringgraph.internal.StringGraphState;
import org.abego.stringgraph.internal.StringGraphStateImpl;
import org.abego.stringgraph.internal.StringGraphConstructingUtil;
import org.abego.stringgraph.internal.StringGraphImpl;
import org.abego.stringgraph.internal.VLQUtil;
import org.abego.stringpool.StringPool;
import org.abego.stringpool.StringPoolBuilder;
import org.abego.stringpool.StringPools;
import org.abego.stringgraph.core.Edge;
import org.abego.stringgraph.core.Edges;
import org.abego.stringgraph.core.Node;
import org.abego.stringgraph.core.Nodes;
import org.abego.stringgraph.core.Properties;
import org.abego.stringgraph.core.Property;
import org.abego.stringgraph.core.StringGraph;
import org.abego.stringgraph.core.StringGraphConstructing;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

class StringGraphStoreDefault implements StringGraphStore {
    //region FieldsState
    private final URI uri;
    //endregion
    
    //region Factories
    private StringGraphStoreDefault(URI uri) {
        this.uri = uri;
    }

    static StringGraphStoreDefault createStringGraphStoreDefault(URI uri) {
        return new StringGraphStoreDefault(uri);
    }
    //endregion

    //region StringGraphStore API
    @Override
    public void writeStringGraph(StringGraph stringGraph) {
        File file = new File(uri);
        FileUtil.ensureDirectoryExists(file.getParentFile());
        try (ObjectOutputStream objectOutputStream =
                     new ObjectOutputStream(Files.newOutputStream(file.toPath()))) {

            writeGraphToStream(objectOutputStream, stringGraph);

        } catch (Exception e) {
            throw new StringGraphStoreException(
                    String.format("Error when writing graph to %s: %s", //NON-NLS
                            uri, e.getMessage()), e);
        }
    }

    @Override
    public void readStringGraph(StringGraphConstructing graphConstructing) {
        StringGraphState state = readStringGraphState();
        StringGraphConstructingUtil.constructGraph(state, graphConstructing);        
    }

    @Override
    public StringGraph readStringGraph() {
        StringGraphState state = readStringGraphState();
        return StringGraphImpl.createStringGraph(state);
    }

    private StringGraphState readStringGraphState() {
        try (ObjectInputStream objectInputStream =
                     new ObjectInputStream(uri.toURL().openStream())) {

            return readStringGraphStateFromStream(objectInputStream);

        } catch (Exception e) {
            throw new StringGraphStoreException(
                    String.format("Error when reading graph from %s: %s", //NON-NLS
                            uri, e.getMessage()), e);
        }
    }

    //endregion

    //region DataFormat API
    private static final String DATA_FORMAT_NAME =
            "org.abego.stringgraph.store.StringGraphStoreDefault";
    private static final DataFormatVersion DATA_FORMAT_VERSION =
            DataFormatVersion.createDataFormatVersion(1, 0);

    public static String getDataFormatName() {
        return DATA_FORMAT_NAME;
    }

    public static DataFormatVersion getDataFormatVersion() {
        return DATA_FORMAT_VERSION;
    }
    //endregion

    //region Read/Write Graph
    private static final String NODES_TAG = "nodes"; //NON-NLS
    private static final String EDGES_TAG = "edges"; //NON-NLS
    private static final String NODE_PROPERTIES_TAG = "node-properties"; //NON-NLS
    private static final String END_TAG = "end"; //NON-NLS

    // package-private, not private, for white-box tests
    void writeGraphToStream(ObjectOutputStream objectOutputStream, StringGraph stringGraph) {
        GraphWriter graphWriter = new GraphWriter(objectOutputStream, stringGraph);
        graphWriter.write();
    }

    // package-private, not private, for white-box tests
    StringGraphState readStringGraphStateFromStream(ObjectInputStream objectInputStream) {
        StringGraphStoreUtil.readAndCheckDataFormat(
                objectInputStream, getDataFormatName(), DATA_FORMAT_VERSION);

        Map<Integer, int[]> props = new HashMap<>();
        int[] nodesIDs = new int[0];
        int[] edgesIDs = new int[0];
        do {
            String tag = readTag(objectInputStream);
            switch (tag) {
                case NODES_TAG:
                    nodesIDs = readNodesBlock(objectInputStream);
                    break;
                case EDGES_TAG:
                    edgesIDs = readEdgesBlock(objectInputStream);
                    break;
                case NODE_PROPERTIES_TAG:
                    props = readNodePropertiesBlock(objectInputStream);
                    break;
                case END_TAG:
                    StringPool strings = readEndBlock(objectInputStream);
                    return new StringGraphStateImpl(
                            props, nodesIDs, edgesIDs, strings);
                default:
                    // to be able to read future file formats
                    // we ignore any tag we don't know.
                    // Future versions of this class will ensure to increase the
                    // major version number when the file format changes in a
                    // way that are incompatible to this approach.
                    break;
            }
        } while (true);
    }

    private int[] readNodesBlock(ObjectInputStream objectInputStream) {
        int n = readInt(objectInputStream);
        int[] nodesIDs = new int[n];
        for (int i = 0; i < n; i++) {
            nodesIDs[i] = readInt(objectInputStream);
        }
        return nodesIDs;
    }

    private int[] readEdgesBlock(ObjectInputStream objectInputStream) {
        int n = readInt(objectInputStream);
        int size = n * 3;
        int[] edgesIDs = new int[size];
        for (int i = 0; i < size; i += 3) {
            edgesIDs[i] = readInt(objectInputStream);
            edgesIDs[i + 1] = readInt(objectInputStream);
            edgesIDs[i + 2] = readInt(objectInputStream);
        }
        return edgesIDs;
    }

    private Map<Integer, int[]> readNodePropertiesBlock(ObjectInputStream objectInputStream) {
        Map<Integer, int[]> props = new HashMap<>();
        int countOfNodesWithProps = readInt(objectInputStream);
        for (int iNode = 0; iNode < countOfNodesWithProps; iNode++) {
            int nodeID = readInt(objectInputStream);
            int nProps = readInt(objectInputStream);
            int[] propsIDs = new int[nProps * 2];
            for (int i = 0; i < nProps; i++) {
                propsIDs[2 * i] = readInt(objectInputStream);
                propsIDs[2 * i + 1] = readInt(objectInputStream);
            }
            props.put(nodeID, propsIDs);
        }
        return props;
    }

    // package-private, not private, for white-box tests
    private StringPool readEndBlock(ObjectInputStream objectInputStream) {
        // read the strings
        int len = readInt(objectInputStream);
        byte[] bytes = readBytes(objectInputStream, len);
        return StringPools.newStringPool(bytes);
    }

    //endregion

    //region Writing Blocks
    private class GraphWriter {
        /**
         * Collects all Strings used in the StringGraph, to later reference them by
         * their ids in the data written to the store
         */
        private final StringPoolBuilder builder = StringPools.builder();
        private final ObjectOutputStream objectOutputStream;
        private final StringGraph stringGraph;

        private GraphWriter(ObjectOutputStream objectOutputStream, StringGraph stringGraph) {
            this.objectOutputStream = objectOutputStream;
            this.stringGraph = stringGraph;
        }

        public void write() {
            StringGraphStoreUtil.writeDataFormat(
                    objectOutputStream, getDataFormatName(), DATA_FORMAT_VERSION);
            writeTag(EDGES_TAG);
            writeEdgesBlock();
            writeTag(NODES_TAG);
            writeNodesBlock();
            writeTag(NODE_PROPERTIES_TAG);
            writeNodePropertiesBlock();
            writeTag(END_TAG);
            writeEndBlock();
        }

        private void writeEdgesBlock() {
            Edges allEdges = stringGraph.edges();
            writeInt(allEdges.getSize());
            for (Edge e : allEdges) {
                writeString(e.getFromNode().id());
                writeString(e.getToNode().id());
                writeString(e.getLabel());
            }
        }

        private void writeNodesBlock() {
            Nodes allNodes = stringGraph.nodes();
            writeInt(allNodes.getSize());
            for (Node s : allNodes) {
                writeString(s.id());
            }
        }

        private void writeNodePropertiesBlock() {
            writeInt(calcCountOfNodesWithProps(stringGraph));
            Nodes allNodes = stringGraph.nodes();
            for (Node node : allNodes) {
                Properties properties = stringGraph.getNodeProperties(node.id());
                int n = properties.getSize();
                if (n > 0) {
                    writeString(node.id());
                    writeInt(n);
                    for (Property p : properties) {
                        writeString(p.getName());
                        writeString(p.getValue());
                    }
                }
            }
        }

        // package-private, not private, for white-box tests
        void writeEndBlock() {
            StringPool allStrings = builder.build();
            byte[] bytes = allStrings.getBytes();
            writeVLQInt(bytes.length);
            try {
                for (byte b : bytes) {
                    objectOutputStream.writeByte(b);
                }
            } catch (Exception e) {
                throw new StringGraphStoreException("Error when writing end block", e);
            }
        }

        private void writeInt(int i) {
            writeVLQInt(i);
        }

        // package-private, not private, for white-box tests
        void writeTag(String tag) {
            try {
                objectOutputStream.writeObject(tag);
            } catch (Exception e) {
                throw new StringGraphStoreException(
                        "Error when writing block tag", e);
            }
        }

        private void writeString(String s) {
            writeVLQInt(builder.add(s));
        }

        private void writeVLQInt(int i) {
            VLQUtil.encodeUnsignedIntAsVLQ(i, val -> {
                try {
                    objectOutputStream.writeByte(val);
                } catch (Exception e) {
                    throw new StringGraphStoreException(
                            "Error when writing VLQ 'int'", e);
                }
            });
        }
    }
    //endregion

    //endregion


    //region Primitive IO Operations

    // package-private, not private, for white-box tests
    String readTag(ObjectInputStream objectInputStream) {
        try {
            return (String) objectInputStream.readObject();
        } catch (Exception e) {
            throw new StringGraphStoreException("Error when reading block tag", e);
        }
    }

    // package-private, not private, for white-box tests
    int readInt(ObjectInputStream objectInputStream) {
        return readVLQInt(objectInputStream);
    }

    private static byte[] readBytes(ObjectInputStream objectInputStream, int len) {
        try {
            byte[] bytes = new byte[len];
            objectInputStream.readFully(bytes);
            return bytes;
        } catch (Exception e) {
            throw new StringGraphStoreException("Error when reading bytes", e);
        }
    }

    private static int readVLQInt(ObjectInputStream objectInputStream) {
        return VLQUtil.decodeUnsignedIntFromVLQ(() -> {
            try {
                return objectInputStream.readByte();
            } catch (Exception e) {
                throw new StringGraphStoreException(
                        "Error when reading VLQ 'int'", e);
            }
        });
    }

    //endregion

    //region Helpers
    private int calcCountOfNodesWithProps(StringGraph stringGraph) {
        int result = 0;
        for (Node node : stringGraph.nodes()) {
            if (stringGraph.getNodeProperties(node.id()).getSize() > 0) {
                result++;
            }
        }
        return result;
    }
    //endregion
}
