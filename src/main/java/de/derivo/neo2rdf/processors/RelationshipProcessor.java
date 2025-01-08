package de.derivo.neo2rdf.processors;

import org.neo4j.driver.Value;

import java.util.Map;

public interface RelationshipProcessor {

    void process(String relationshipID,
                 String sourceID,
                 String targetID,
                 String typeID,
                 Map<String, Value> propertyValuePairs);

    void startProcessing();
}
