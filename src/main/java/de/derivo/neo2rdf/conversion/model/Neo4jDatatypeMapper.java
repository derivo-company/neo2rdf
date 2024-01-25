package de.derivo.neo2rdf.conversion.model;

import org.neo4j.kernel.impl.store.PropertyType;
import org.neo4j.values.storable.ValueCategory;

public class Neo4jDatatypeMapper {

    public String getDatatype(PropertyType propertyType) {
        // TODO required?
        switch (propertyType) {
            case STRING:
            case INT:
            case BOOL:
            case BYTE:
            case CHAR:
            case LONG:
            case ARRAY:
            case FLOAT:
            case SHORT:
            case DOUBLE:
            case GEOMETRY:
            case TEMPORAL:
            case SHORT_ARRAY:
            case SHORT_STRING:
                break;
        }
        return null;
    }

    public String getXSDDatatype(ValueCategory valueCategory) {
        // TODO required?
        switch (valueCategory) {
            case UNKNOWN:
            case TEXT:
            case NUMBER:
            case BOOLEAN:
            case TEXT_ARRAY:
            case NO_CATEGORY:
            case NUMBER_ARRAY:
            case BOOLEAN_ARRAY:
            case GEOMETRY_ARRAY:
            case TEMPORAL_ARRAY:
            case GEOMETRY:
            case TEMPORAL:
                break;
        }
        return null;
    }
}
