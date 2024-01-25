package de.derivo.neo2rdf;

import java.io.File;
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
}
