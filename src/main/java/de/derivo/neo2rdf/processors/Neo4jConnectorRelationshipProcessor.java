package de.derivo.neo2rdf.processors;

import de.derivo.neo2rdf.util.ConsoleUtil;
import org.neo4j.driver.Record;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
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
            String nodeIdSource = record.get("nodeIdSource").asString();
            String nodeIdTarget = record.get("nodeIdTarget").asString();
            String relationshipType = record.get("relationshipType").asString();
            String relId = record.get("relationshipId").asString();
            List<Object> relationshipProperties = record.get("relationshipProperties").asList();

            System.out.println(nodeIdSource);
            System.out.println(nodeIdTarget);
            System.out.println(relationshipType);
            System.out.println(relId);
            System.out.println(relationshipProperties);
            // TODO
            System.out.println("------------------");

            if (relationshipCounter.incrementAndGet() % PROGRESS_MESSAGE_AFTER_X_RELATIONSHIPS == 0) {
                log.info("Processed %s relationships.".formatted(ConsoleUtil.formatDecimal(relationshipCounter.get())));
            }
        });

        this.connector.query("""
                        MATCH (n)-[r]->(m)
                        RETURN elementId(n) AS nodeIdSource,
                               elementId(m) AS nodeIdTarget,
                               type(r) AS relationshipType,
                               elementId(r) AS relationshipId,
                               CASE
                                   WHEN size(keys(r)) > 0 THEN reduce(s = [], key IN keys(r) | s + {key: key, value: r[key]})
                                   ELSE []
                               END AS relationshipProperties;
                        """,
                recordConsumer);
    }


}
