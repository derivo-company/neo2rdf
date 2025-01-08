package de.derivo.neo2rdf;

import de.derivo.neo2rdf.conversion.IndexedNeo4jSchema;
import de.derivo.neo2rdf.conversion.Neo4jDBToTurtle;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.config.ConversionConfigBuilder;
import de.derivo.neo2rdf.processors.Neo4jConnectorNodeProcessor;
import de.derivo.neo2rdf.processors.Neo4jConnectorRelationshipProcessor;
import de.derivo.neo2rdf.processors.NodeProcessor;
import de.derivo.neo2rdf.store.RDF4JInMemoryStore;
import de.derivo.neo2rdf.store.RDFStoreTestExtension;
import de.derivo.neo2rdf.util.ConsoleUtil;
import de.derivo.neo2rdf.util.SequenceConversionType;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.neo4j.driver.Value;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecordIteratorTests {

    @RegisterExtension
    public static final RDFStoreTestExtension storeTestExtension = new RDFStoreTestExtension(TestUtil.getCypherCreateQueries(
            "neo4j-movie-db.cypher"));

    private static final ConversionConfig config = ConversionConfigBuilder.newBuilder()
            .setSequenceConversionType(SequenceConversionType.SEPARATE_LITERALS)
            .build();


    @BeforeAll
    public static void initStore() {
    }


    @Test
    public void indexNeo4jSchema() {
        IndexedNeo4jSchema indexedSchema = new IndexedNeo4jSchema(storeTestExtension.getNeo4jDBConnector());
        Logger.info(ConsoleUtil.getSeparator());
        Logger.info("Labels:");
        Logger.info(ConsoleUtil.getSeparator());
        Logger.info(indexedSchema.getNeo4jLabels().toString());
        Assertions.assertEquals(Set.of("Movie", "Person"), new UnifiedSet<>(indexedSchema.getNeo4jLabels()));

        Logger.info(ConsoleUtil.getSeparator());
        Logger.info("Property Keys:");
        Logger.info(ConsoleUtil.getSeparator());
        Set<String> propertyKeys = indexedSchema.getPropertyKeys();
        Logger.info(propertyKeys.toString());
        Assertions.assertTrue(propertyKeys.contains("title"));
        Assertions.assertTrue(propertyKeys.contains("released"));
        Assertions.assertTrue(propertyKeys.contains("name"));
        Assertions.assertTrue(propertyKeys.contains("born"));

        Logger.info(ConsoleUtil.getSeparator());
        Logger.info("Relationship Types:");
        Logger.info(ConsoleUtil.getSeparator());
        Logger.info(indexedSchema.getRelationshipTypes().toString());
        Assertions.assertEquals(Set.of("ACTED_IN", "DIRECTED", "PRODUCED", "WROTE", "FOLLOWS", "REVIEWED"),
                new UnifiedSet<>(indexedSchema.getRelationshipTypes()));

    }


    @Test
    public void convertMovieDBToRDF() throws IOException {
        File outputFile = TestUtil.getTempFile("movie-db-test.ttl");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            Neo4jDBToTurtle neo4jDBToTurtle = new Neo4jDBToTurtle(storeTestExtension.getNeo4jDBConnector(), config, outputStream);
            neo4jDBToTurtle.startProcessing();
            RDF4JInMemoryStore store = new RDF4JInMemoryStore(List.of(outputFile));
            Set<String> assignedClasses = store.getAssignedClasses(config.getBasePrefix() + "node-1");
            Assertions.assertEquals(Set.of(config.getBasePrefix() + "Person"), assignedClasses);

            Set<String> allClasses = store.getInstances(OWL.CLASS.toString());
            Assertions.assertTrue(allClasses.contains(config.getBasePrefix() + "Person"));
            Assertions.assertTrue(allClasses.contains(config.getBasePrefix() + "Movie"));
        }
    }

    @Test
    public void nodeProcessor() {
        NodeProcessor nodeProcessor = new Neo4jConnectorNodeProcessor(storeTestExtension.getNeo4jDBConnector()) {
            @Override
            public void process(String nodeID, String assignedLabel) {
                Logger.info("{} {}", nodeID, assignedLabel);
            }

            @Override
            public void process(String nodeID, String propertyKey, Value value) {
                Logger.info("{} {} {}", nodeID, propertyKey, value);
            }
        };
        nodeProcessor.startProcessing();
    }

    @Test
    public void relationshipProcessor() {
        Neo4jConnectorRelationshipProcessor relationshipProcessor = new Neo4jConnectorRelationshipProcessor(storeTestExtension.getNeo4jDBConnector()) {

            @Override
            public void process(String relationshipID,
                                String sourceID,
                                String targetID,
                                String typeID,
                                Map<String, Value> propertyValuePairs) {
                Logger.info("{} {} =={}==> {}", relationshipID, sourceID, typeID, targetID);

            }
        };
        relationshipProcessor.startProcessing();
    }

}
