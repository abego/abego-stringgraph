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

import org.abego.commons.stringpool.StringPool;
import org.abego.commons.stringpool.StringPoolBuilder;
import org.abego.commons.stringpool.StringPools;
import org.abego.commons.vlq.VLQUtil;
import org.abego.stringgraph.core.Edge;
import org.abego.stringgraph.core.Edges;
import org.abego.stringgraph.core.Node;
import org.abego.stringgraph.core.Nodes;
import org.abego.stringgraph.core.Properties;
import org.abego.stringgraph.core.Property;
import org.abego.stringgraph.core.StringGraph;
import org.abego.stringgraph.core.StringGraphBuilder;
import org.abego.stringgraph.core.StringGraphConstructing;
import org.abego.stringgraph.core.StringGraphException;
import org.abego.stringgraph.core.StringGraphs;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

class StringGraphStoreDefault implements StringGraphStore {
    //region FieldsState
    /**
     * Collects all Strings used in the StringGraph, to later reference them by
     * their ids in the data written to the store
     */
    private final StringPoolBuilder builder = StringPools.builder();
    private final Map<Integer, int[]> props = new HashMap<>();
    private int[] nodesIDs = new int[0];
    private int[] edgesIDs = new int[0];
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
        try {
            File file = new File(uri);
            ObjectOutputStream objectOutputStream =
                    new ObjectOutputStream(Files.newOutputStream(file.toPath()));

            writeGraph(objectOutputStream, stringGraph);
            objectOutputStream.close();
        } catch (Exception e) {
            throw new StringGraphStoreException(
                    String.format("Error when writing graph to %s: %s", //NON-NLS
                            uri, e.getMessage()), e);
        }
    }

    @Override
    public void readStringGraph(StringGraphConstructing graphConstructing) {
        try {
            ObjectInputStream objectInputStream =
                    new ObjectInputStream(uri.toURL().openStream());
            readStringGraph(objectInputStream, graphConstructing);

            objectInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StringGraph readStringGraph() {
        StringGraphBuilder graphBuilder = StringGraphs.createStringGraphBuilder();
        try {
            readStringGraph(graphBuilder);

            return graphBuilder.build();
        } catch (StringGraphException e) {
            throw e;
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
    void writeGraph(ObjectOutputStream objectOutputStream, StringGraph stringGraph) {
        StringGraphStoreUtil.writeDataFormat(
                objectOutputStream, getDataFormatName(), DATA_FORMAT_VERSION);
        writeTag(objectOutputStream, EDGES_TAG);
        writeEdgesBlock(stringGraph, objectOutputStream);
        writeTag(objectOutputStream, NODES_TAG);
        writeNodesBlock(stringGraph, objectOutputStream);
        writeTag(objectOutputStream, NODE_PROPERTIES_TAG);
        writeNodePropertiesBlock(stringGraph, objectOutputStream);
        writeTag(objectOutputStream, END_TAG);
        writeEndBlock(objectOutputStream);
    }

    // package-private, not private, for white-box tests
    void readStringGraph(
            ObjectInputStream objectInputStream,
            StringGraphConstructing graphConstructing) {
        StringGraphStoreUtil.readAndCheckDataFormat(
                objectInputStream, getDataFormatName(), DATA_FORMAT_VERSION);

        boolean fileEndReached = false;
        do {
            String tag = readTag(objectInputStream);
            switch (tag) {
                case NODES_TAG:
                    readNodesBlock(objectInputStream);
                    break;
                case EDGES_TAG:
                    readEdgesBlock(objectInputStream);
                    break;
                case NODE_PROPERTIES_TAG:
                    readNodePropertiesBlock(objectInputStream);
                    break;
                case END_TAG:
                    readEndBlock(objectInputStream, graphConstructing);
                    fileEndReached = true;
                    break;
                default:
                    // to be able to read future file formats
                    // we ignore any tag we don't know.
                    // Future versions of this class will ensure to increase the
                    // major version number when the file format changes in a
                    // way that are incompatible to this approach.
                    break;
            }
        } while (!fileEndReached);
    }

    //region Reading Blocks
    private void readNodesBlock(ObjectInputStream objectInputStream) {
        int n = readInt(objectInputStream);
        nodesIDs = new int[n];
        for (int i = 0; i < n; i++) {
            nodesIDs[i] = readInt(objectInputStream);
        }
    }

    private void readEdgesBlock(ObjectInputStream objectInputStream) {
        int n = readInt(objectInputStream);
        int size = n * 3;
        edgesIDs = new int[size];
        for (int i = 0; i < size; i += 3) {
            edgesIDs[i] = readInt(objectInputStream);
            edgesIDs[i + 1] = readInt(objectInputStream);
            edgesIDs[i + 2] = readInt(objectInputStream);
        }
    }

    private void readNodePropertiesBlock(ObjectInputStream objectInputStream) {
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
    }

    // package-private, not private, for white-box tests
    void readEndBlock(
            ObjectInputStream objectInputStream, StringGraphConstructing graphConstructing) {
        // read the strings
        int len = readInt(objectInputStream);
        byte[] bytes = readBytes(objectInputStream, len);
        StringPool strings = StringPools.newStringPool(bytes);

        // Now that we know the actual Strings we can finally add
        // the items in the StringGraph
        addNodes(graphConstructing, strings);
        addEdges(graphConstructing, strings);
        addProps(graphConstructing, strings);
    }

    private void addNodes(StringGraphConstructing graphConstructing, StringPool strings) {
        for (int nodesID : nodesIDs) {
            graphConstructing.addNode(strings.getString(nodesID));
        }
    }

    private void addEdges(StringGraphConstructing graphConstructing, StringPool strings) {
        int n = edgesIDs.length;
        for (int i = 0; i < n; i += 3) {
            graphConstructing.addEdge(
                    strings.getString(edgesIDs[i]),
                    strings.getString(edgesIDs[i + 2]), strings.getString(edgesIDs[i + 1])
            );
        }
    }

    private void addProps(StringGraphConstructing graphConstructing, StringPool strings) {
        for (Map.Entry<Integer, int[]> e : props.entrySet()) {
            int nodeID = e.getKey();
            int[] propsIDs = e.getValue();
            int n = propsIDs.length / 2;
            for (int i = 0; i < n; i++) {
                graphConstructing.setNodeProperty(
                        strings.getString(nodeID),
                        strings.getString(propsIDs[2 * i]),
                        strings.getString(propsIDs[2 * i + 1]));
            }
        }
    }
    //endregion

    //region Writing Blocks
    private void writeEdgesBlock(StringGraph stringGraph, ObjectOutputStream objectOutputStream) {
        Edges allEdges = stringGraph.edges();
        writeInt(objectOutputStream, allEdges.getSize());
        for (Edge e : allEdges) {
            writeString(objectOutputStream, e.getFromNode().id());
            writeString(objectOutputStream, e.getToNode().id());
            writeString(objectOutputStream, e.getLabel());
        }
    }

    private void writeNodesBlock(StringGraph stringGraph, ObjectOutputStream objectOutputStream) {
        Nodes allNodes = stringGraph.nodes();
        writeInt(objectOutputStream, allNodes.getSize());
        for (Node s : allNodes) {
            writeString(objectOutputStream, s.id());
        }
    }

    private void writeNodePropertiesBlock(StringGraph stringGraph, ObjectOutputStream objectOutputStream) {
        writeInt(objectOutputStream, calcCountOfNodesWithProps(stringGraph));
        Nodes allNodes = stringGraph.nodes();
        for (Node node : allNodes) {
            Properties props = stringGraph.getNodeProperties(node.id());
            int n = props.getSize();
            if (n > 0) {
                writeString(objectOutputStream, node.id());
                writeInt(objectOutputStream, n);
                for (Property p : props) {
                    writeString(objectOutputStream, p.getName());
                    writeString(objectOutputStream, p.getValue());
                }
            }
        }
    }

    // package-private, not private, for white-box tests
    void writeEndBlock(ObjectOutputStream objectOutputStream) {
        StringPool allStrings = builder.build();
        byte[] bytes = allStrings.getBytes();
        writeVLQInt(objectOutputStream, bytes.length);
        try {
            for (byte b : bytes) {
                objectOutputStream.writeByte(b);
            }
        } catch (Exception e) {
            throw new StringGraphStoreException("Error when writing end block", e);
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

    private void writeInt(ObjectOutputStream objectOutputStream, int i) {
        writeVLQInt(objectOutputStream, i);
    }

    // package-private, not private, for white-box tests
    void writeTag(ObjectOutputStream objectOutputStream, String tag) {
        try {
            objectOutputStream.writeObject(tag);
        } catch (Exception e) {
            throw new StringGraphStoreException(
                    "Error when writing block tag", e);
        }
    }

    private void writeString(ObjectOutputStream objectOutputStream, String s) {
        writeVLQInt(objectOutputStream, builder.add(s));
    }

    private static void writeVLQInt(ObjectOutputStream objectOutputStream, int i) {
        VLQUtil.encodeUnsignedIntAsVLQ(i, val -> {
            try {
                objectOutputStream.writeByte(val);
            } catch (Exception e) {
                throw new StringGraphStoreException(
                        "Error when writing VLQ 'int'", e);
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
