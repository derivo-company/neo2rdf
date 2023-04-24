package de.derivo.neo4jconverter;


import de.derivo.neo4jconverter.utils.ConsoleUtils;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Paths;

public class ConverterTestBase {

    public Logger log = ConsoleUtils.getLogger();

    public static File getRootDir() {
        return Paths.get(System.getProperty("user.dir"), "src", "test").toFile();
    }

    public static File getResourcesDir() {
        return Paths.get(getRootDir().toString(), "resources").toFile();
    }

    public static File getResource(String resource) {
        return Paths.get(getResourcesDir().toString(), resource).toFile();
    }

}
