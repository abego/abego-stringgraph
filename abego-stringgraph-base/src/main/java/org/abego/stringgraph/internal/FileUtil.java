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

import org.eclipse.jdt.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;

// Original: https://github.com/abego/commons
public final class FileUtil {

    private FileUtil() {
    }

    /**
     * Ensure the {@code directory} exists.
     *
     * <p>When the directory is missing, create it (and all its missing
     * parents.</p>
     *
     * <p>Throw an {@link UncheckedIOException} when the directory could not be
     * created.</p>
     *
     * <p>Do nothing when {@code directory} is {@code null}.</p>
     */
    public static void ensureDirectoryExists(@Nullable File directory) {
        if (directory == null) {
            return;
        }

        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (!success) {
                throw new UncheckedIOException(new FileNotFoundException(String.format(
                        "Directory does not exist and could not be created: %s", // NON-NLS
                        directory.getAbsolutePath())));
            }
        } else if (!directory.isDirectory()) {
            throw new UncheckedIOException(new IOException(
                    String.format("File exists but is not a directory: %s", // NON-NLS
                            directory.getAbsolutePath())));
        }
    }


    public static void ensureFileExists(File file) {
        if (!file.exists()) {
            ensureDirectoryExists(file.getParentFile());

            try {
                boolean wasCreate = file.createNewFile();
                if (!wasCreate) {
                    throw new IllegalStateException(
                            "Trying to create file that already exists: "+file.getAbsolutePath());
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }


}
