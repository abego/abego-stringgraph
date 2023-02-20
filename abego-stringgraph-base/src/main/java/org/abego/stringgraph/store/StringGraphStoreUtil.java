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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class StringGraphStoreUtil {

    static void readAndCheckDataFormat(
            ObjectInputStream objectInputStream,
            String dataFormatName,
            DataFormatVersion supportedVersion) {
        DataFormatVersion version = readDataFormatVersion(objectInputStream, dataFormatName);
        // We can only read files that have the same major number
        // (see JavaDoc DataFormatVersion)
        if (version.majorNumber != supportedVersion.majorNumber) {
            throw new StringGraphStoreException(
                    String.format("Incompatible data format version. Expected '%d', got '%d'", //NON-NLS
                            supportedVersion.majorNumber, version.majorNumber));
        }
    }

    public static void writeDataFormat(ObjectOutputStream objectOutputStream,
                                       String dataFormatName,
                                       DataFormatVersion dataFormatVersion) {
        try {
            objectOutputStream.writeObject(dataFormatName);
            objectOutputStream.writeInt(dataFormatVersion.majorNumber);
            objectOutputStream.writeInt(dataFormatVersion.minorNumber);
        } catch (IOException e) {
            throw new StringGraphStoreException(
                    "Error when writing data format", e);
        }
    }

    private static DataFormatVersion readDataFormatVersion(
            ObjectInputStream objectInputStream, String dataFormatName) {
        String formatName = readFormatName(objectInputStream);
        //noinspection CallToSuspiciousStringMethod
        if (!formatName.equals(dataFormatName)) {
            throw new StringGraphStoreException(
                    String.format("Invalid file format. Expected header '%s', got '%s'", //NON-NLS
                            dataFormatName, formatName));
        }
        try {

            int major = objectInputStream.readInt();
            int minor = objectInputStream.readInt();
            return DataFormatVersion.createDataFormatVersion(major, minor);

        } catch (Exception e) {
            throw new StringGraphStoreException("Error when reading data format", e);
        }
    }

    private static String readFormatName(ObjectInputStream objectInputStream) {
        try {
            return (String) objectInputStream.readObject();
        } catch (Exception e) {
            throw new StringGraphStoreException("Error when reading data format name", e);
        }
    }
}
