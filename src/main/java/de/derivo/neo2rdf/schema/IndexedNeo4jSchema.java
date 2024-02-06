package de.derivo.neo2rdf.schema;

import java.util.Collections;
import java.util.Map;

public class IndexedNeo4jSchema {


    private final Map<Long, String> labelIDToStr;
    private final Map<Long, String> propertyKeyIDToStr;
    private final Map<Long, String> relationshipTypeIDToStr;

    public IndexedNeo4jSchema(Map<Long, String> labelIDToStr, Map<Long, String> propertyKeyIDToStr,
                              Map<Long, String> relationshipTypeIDToStr) {
        this.labelIDToStr = Collections.unmodifiableMap(labelIDToStr);
        this.propertyKeyIDToStr = Collections.unmodifiableMap(propertyKeyIDToStr);
        this.relationshipTypeIDToStr = Collections.unmodifiableMap(relationshipTypeIDToStr);
    }

    public Map<Long, String> getLabelIDToStr() {
        return labelIDToStr;
    }

    public Map<Long, String> getPropertyKeyIDToStr() {
        return propertyKeyIDToStr;
    }

    public Map<Long, String> getRelationshipTypeIDToStr() {
        return relationshipTypeIDToStr;
    }
}
