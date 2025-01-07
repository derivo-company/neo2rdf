package de.derivo.neo2rdf.processors;

import org.neo4j.driver.Value;

import java.util.Map;
import java.util.stream.Stream;

public interface RelationshipProcessor {

    void process(String relationshipID,
                 String sourceID,
                 String targetID,
                 String typeID,
                 Stream<Map.Entry<String, Value>> propertyValuePairs);

    void startProcessing();
}
