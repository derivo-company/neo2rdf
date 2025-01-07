package de.derivo.neo2rdf;

import de.derivo.neo2rdf.conversion.IndexedNeo4jSchema;
import de.derivo.neo2rdf.conversion.Neo4jDBToTurtle;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.config.ConversionConfigBuilder;
import de.derivo.neo2rdf.processors.Neo4jConnectorNodeProcessor;
import de.derivo.neo2rdf.processors.Neo4jConnectorRelationshipProcessor;
import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import de.derivo.neo2rdf.processors.NodeProcessor;
import de.derivo.neo2rdf.store.RDF4JInMemoryStore;
import de.derivo.neo2rdf.util.ConsoleUtil;
import de.derivo.neo2rdf.util.SequenceConversionType;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.neo4j.values.storable.Value;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class RecordIteratorTests {
    public Logger log = ConsoleUtil.getLogger();

    private static Neo4jDBConnector neo4jDBConnector;
    private static final ConversionConfig config = ConversionConfigBuilder.newBuilder()
            .setSequenceConversionType(SequenceConversionType.SEPARATE_LITERALS)
            .build();


    @BeforeAll
    public static void initStore() {
        neo4jDBConnector = new Neo4jDBConnector(
                "bolt://localhost:7687",
                "neo4j",
                "aaaaaaaa",
                "database1"
        ); // TODO
    }


    @Test
    public void indexNeo4jSchema() {
        IndexedNeo4jSchema indexedSchema = new IndexedNeo4jSchema(neo4jDBConnector);
        log.info(ConsoleUtil.getSeparator());
        log.info("Labels:");
        log.info(ConsoleUtil.getSeparator());
        log.info(indexedSchema.getNeo4jLabels().toString());
        Assertions.assertEquals(Set.of("Movie", "Person"), new UnifiedSet<>(indexedSchema.getNeo4jLabels()));

        log.info(ConsoleUtil.getSeparator());
        log.info("Property Keys:");
        log.info(ConsoleUtil.getSeparator());
        Set<String> propertyKeys = indexedSchema.getPropertyKeys();
        log.info(propertyKeys.toString());
        Assertions.assertTrue(propertyKeys.contains("title"));
        Assertions.assertTrue(propertyKeys.contains("released"));
        Assertions.assertTrue(propertyKeys.contains("name"));
        Assertions.assertTrue(propertyKeys.contains("born"));

        log.info(ConsoleUtil.getSeparator());
        log.info("Relationship Types:");
        log.info(ConsoleUtil.getSeparator());
        log.info(indexedSchema.getRelationshipTypes().toString());
        Assertions.assertEquals(Set.of("ACTED_IN", "DIRECTED", "PRODUCED", "WROTE", "FOLLOWS", "REVIEWED"),
                new UnifiedSet<>(indexedSchema.getRelationshipTypes()));

    }


    @Test
    public void convertMovieDBToRDF(@TempDir File tempDir) throws IOException {
        File outputFile = Paths.get(tempDir.toString(), "movie-db-test.ttl").toFile();
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            Neo4jDBToTurtle neo4jDBToTurtle = new Neo4jDBToTurtle(neo4jDBConnector, config, outputStream); // TODO
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
        NodeProcessor nodeProcessor = new Neo4jConnectorNodeProcessor(neo4jDBConnector) {
            @Override
            public void process(String nodeID, String assignedLabel) {
                log.info(nodeID + " " + assignedLabel);
            }

            @Override
            public void process(String nodeID, String propertyKey, Value value) {
                log.info(nodeID + " " + propertyKey + " " + value);
            }
        };
        nodeProcessor.startProcessing();
    }

    @Test
    public void relationshipProcessor() {
        Neo4jConnectorRelationshipProcessor relationshipProcessor = new Neo4jConnectorRelationshipProcessor(neo4jDBConnector) {

            @Override
            public void process(String relationshipID,
                                String sourceID,
                                String targetID,
                                String typeID,
                                Stream<Map.Entry<String, org.neo4j.driver.Value>> propertyValuePairs) {
                log.info(relationshipID + " " + sourceID + " ==" + typeID + "==> " + targetID);

            }
        };
        relationshipProcessor.startProcessing();
    }

}
