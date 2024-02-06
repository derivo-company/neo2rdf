package de.derivo.neo2rdf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestUtil {
    public static File getResource(String resource) {
        return Paths.get(getResourcesDir().toString(), resource).toFile();
    }

    public static File getRootDir() {
        return Paths.get(System.getProperty("user.dir"), "src", "test").toFile();
    }

    public static File getResourcesDir() {
        return Paths.get(getRootDir().toString(), "resources").toFile();
    }

    public static File getTempFile(String filename) {
        try {
            return Files.createTempFile("neo2rdf", filename).toFile();
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
