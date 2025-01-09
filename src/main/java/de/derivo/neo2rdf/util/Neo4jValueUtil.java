package de.derivo.neo2rdf.util;

import org.neo4j.driver.Value;

public class Neo4jValueUtil {

    public static boolean isList(Value value) {
        return isOfType(value, "LIST OF ANY?");
    }

    public static boolean isPoint(Value value) {
        return isOfType(value, "POINT");
    }

    private static boolean isOfType(Value value, String type) {
        return value.type().name().equals(type);
    }
}
