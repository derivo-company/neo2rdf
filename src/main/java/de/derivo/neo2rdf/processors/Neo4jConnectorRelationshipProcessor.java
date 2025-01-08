package de.derivo.neo2rdf.processors;

import de.derivo.neo2rdf.util.ConsoleUtil;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class Neo4jConnectorRelationshipProcessor implements RelationshipProcessor {

    private final Neo4jDBConnector connector;
    private final Logger log = ConsoleUtil.getLogger();
    private final long PROGRESS_MESSAGE_AFTER_X_RELATIONSHIPS = 100_000;

    public Neo4jConnectorRelationshipProcessor(Neo4jDBConnector connector) {
        this.connector = connector;
    }

    public void startProcessing() {
        processRelationships();
    }

    private void processRelationships() {
        AtomicLong relationshipCounter = new AtomicLong(0);
        Consumer<Stream<Record>> recordConsumer = records -> records.forEach(record -> {
            String nodeIdSource = record.get("nodeIdSource").asLong() + "";
            String nodeIdTarget = record.get("nodeIdTarget").asLong() + "";
            String relationshipType = record.get("relationshipType").asString();
            String relId = record.get("relationshipId").asLong() + "";
            Value properties = record.get("relationshipProperties");
            Map<String, Value> propertyValuePairs = properties.asMap(Function.identity());
            process(relId, nodeIdSource, nodeIdTarget, relationshipType,
                    propertyValuePairs);

            if (relationshipCounter.incrementAndGet() % PROGRESS_MESSAGE_AFTER_X_RELATIONSHIPS == 0) {
                log.info("Processed %s relationships and their assigned properties.".formatted(ConsoleUtil.formatDecimal(relationshipCounter.get())));
            }
        });

        this.connector.query("""
                        MATCH (n)-[r]->(m)
                        RETURN id(n) AS nodeIdSource,
                               id(m) AS nodeIdTarget,
                               type(r) AS relationshipType,
                               id(r) AS relationshipId,
                               properties(r) AS relationshipProperties;
                        """,
                recordConsumer);
    }


}
