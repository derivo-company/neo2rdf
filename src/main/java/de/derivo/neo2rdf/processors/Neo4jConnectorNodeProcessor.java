package de.derivo.neo2rdf.processors;

import de.derivo.neo2rdf.util.ConsoleUtil;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class Neo4jConnectorNodeProcessor implements NodeProcessor {

    private final Neo4jDBConnector connector;

    private final Logger log = ConsoleUtil.getLogger();
    private final long PROGRESS_MESSAGE_AFTER_X_NODES = 100_000;

    public Neo4jConnectorNodeProcessor(Neo4jDBConnector connector) {
        this.connector = connector;
    }

    public void startProcessing() {
        processNodeLabels();
        processNodePropertyKeyValues();
    }

    private void processNodePropertyKeyValues() {
        AtomicLong processedNodesCounter = new AtomicLong(0);
        Consumer<Stream<Record>> recordConsumer = records -> records.forEach(record -> {
            String nodeId = record.get("nodeId").asString();
            String propertyKey = record.get("propertyKey").asString();
            Value value = record.get("value");

            System.out.println(nodeId);
            System.out.println(propertyKey);
            System.out.println(value);
            // TODO
            System.out.println("------------------");

            if (processedNodesCounter.incrementAndGet() % PROGRESS_MESSAGE_AFTER_X_NODES == 0) {
                log.info("Processed properties of %s nodes.".formatted(ConsoleUtil.formatDecimal(processedNodesCounter.get())));
            }
        });

        this.connector.query("""
                        MATCH (n) UNWIND keys(n) AS propertyKey
                        RETURN elementId(n) AS nodeId, propertyKey, n[propertyKey] AS value;
                        """,
                recordConsumer);
    }

    private void processNodeLabels() {
        AtomicLong processedNodesCounter = new AtomicLong(0);
        Consumer<Stream<Record>> recordConsumer = records -> records.forEach(record -> {
            Value nodeIdValue = record.get("nodeId");
            String nodeId = nodeIdValue.asString();
            List<Object> nodeLabels = record.get("nodeLabels").asList();
            nodeLabels.forEach(nodeLabel -> {
                System.out.println(nodeId);
                System.out.println(nodeLabel);
                // TODO
            });

            if (processedNodesCounter.incrementAndGet() % PROGRESS_MESSAGE_AFTER_X_NODES == 0) {
                log.info("Processed assigned labels of %s nodes.".formatted(ConsoleUtil.formatDecimal(processedNodesCounter.get())));
            }
        });

        this.connector.query("""
                        MATCH (n) RETURN elementId(n) AS nodeId, labels(n) AS nodeLabels;
                        """,
                recordConsumer);
    }


}
