package de.derivo.neo2rdf;

/*-
 * #%L
 * neo2rdf
 * %%
 * Copyright (C) 2026 Derivo Company
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class TestUtil {
    private static final boolean WRITE_TO_TMP_WORKING_DIR = false;

    public static File getResource(String resource) {
        return Paths.get(getResourcesDir().toString(), resource).toFile();
    }

    public static List<String> getCypherCreateQueries(String cypherQueryFileName) {
        if (!cypherQueryFileName.endsWith(".cypher")) {
            throw new IllegalArgumentException("Query file name does not end with '.cypher'.");
        }
        Path p = Paths.get(getResourcesDir().toString(), cypherQueryFileName);
        try {
            String queriesStr = Files.readString(p);
            return Arrays.stream(queriesStr.split(";"))
                    .map(String::strip)
                    .filter(q -> !q.isEmpty())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getRootDir() {
        return Paths.get(System.getProperty("user.dir"), "src", "test").toFile();
    }

    public static File getResourcesDir() {
        return Paths.get(getRootDir().toString(), "resources").toFile();
    }

    public static File getTempFile(String filename) {
        try {
            if (TestUtil.WRITE_TO_TMP_WORKING_DIR) {
                File tmp = Path.of(".", "tmp").resolve(filename).toFile();
                tmp.getParentFile().mkdirs();
                return tmp;
            } else {
                return Files.createTempFile("neo2rdf", filename).toFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getTempDirectory(String dirName) {
        try {
            return Files.createTempDirectory("neo2rdf-" + dirName).toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
