package de.derivo.neo2rdf.util;

import org.neo4j.driver.Value;
import org.neo4j.driver.types.TypeSystem;

public class Neo4jValueUtil {

    public static boolean isList(Value value) {
        return value.type().equals(TypeSystem.getDefault().LIST());
    }

    public static boolean isPoint(Value value) {
        return value.type().equals(TypeSystem.getDefault().POINT());
    }

    public static boolean isVector(Value value) {
        return value.type().equals(TypeSystem.getDefault().VECTOR());
    }
}
