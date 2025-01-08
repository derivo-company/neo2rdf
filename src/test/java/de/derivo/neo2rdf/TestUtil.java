package de.derivo.neo2rdf;

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
