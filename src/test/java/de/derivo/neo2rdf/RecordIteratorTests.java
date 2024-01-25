package de.derivo.neo2rdf;

import de.derivo.neo2rdf.conversion.Neo4jDBToTurtle;
import de.derivo.neo2rdf.conversion.Neo4jStoreFactory;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.config.ConversionConfigBuilder;
import de.derivo.neo2rdf.processors.NodeProcessor;
import de.derivo.neo2rdf.processors.PropertyProcessor;
import de.derivo.neo2rdf.processors.RelationshipProcessor;
import de.derivo.neo2rdf.schema.IndexedNeo4jSchema;
import de.derivo.neo2rdf.schema.IndexedNeo4jSchemaGenerator;
import de.derivo.neo2rdf.store.RDF4JInMemoryStore;
import de.derivo.neo2rdf.util.ConsoleUtil;
import de.derivo.neo2rdf.util.SequenceConversionType;
import org.apache.shiro.util.Assert;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.kernel.impl.store.NeoStores;
import org.neo4j.kernel.impl.store.PropertyType;
import org.neo4j.values.storable.Value;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecordIteratorTests {
    public Logger log = ConsoleUtil.getLogger();

    private static NeoStores neoStores;
    private static IndexedNeo4jSchema schema;
    private static final ConversionConfig config = ConversionConfigBuilder.newBuilder()
            .setSequenceConversionType(SequenceConversionType.SEPARATE_LITERALS)
            .build();


    @BeforeAll
    public static void initStore() {
        File testStoreDir = TestUtil.getResource("neo4j-db-example");
        neoStores = Neo4jStoreFactory.getNeo4jStore(testStoreDir);
        IndexedNeo4jSchemaGenerator gen = new IndexedNeo4jSchemaGenerator(neoStores);
        schema = gen.generate();
    }


    @Test
    public void indexNeo4jSchema() {
        IndexedNeo4jSchemaGenerator gen = new IndexedNeo4jSchemaGenerator(neoStores);
        IndexedNeo4jSchema indexedSchema = gen.generate();
        log.info(ConsoleUtil.getSeparator());
        log.info("Labels:");
        log.info(ConsoleUtil.getSeparator());
        log.info(indexedSchema.getLabelIDToStr().toString());
        Assertions.assertEquals(Set.of("Movie", "Person"), new UnifiedSet<>(indexedSchema.getLabelIDToStr().values()));

        log.info(ConsoleUtil.getSeparator());
        log.info("Property Keys:");
        log.info(ConsoleUtil.getSeparator());
        log.info(indexedSchema.getPropertyKeyIDToStr().toString());
        Assertions.assertTrue(indexedSchema.getPropertyKeyIDToStr().containsValue("title"));
        Assertions.assertTrue(indexedSchema.getPropertyKeyIDToStr().containsValue("released"));
        Assertions.assertTrue(indexedSchema.getPropertyKeyIDToStr().containsValue("name"));
        Assertions.assertTrue(indexedSchema.getPropertyKeyIDToStr().containsValue("born"));

        log.info(ConsoleUtil.getSeparator());
        log.info("Relationship Types:");
        log.info(ConsoleUtil.getSeparator());
        log.info(indexedSchema.getRelationshipTypeIDToStr().toString());
        Assertions.assertEquals(Set.of("ACTED_IN", "DIRECTED", "PRODUCED", "WROTE", "FOLLOWS", "REVIEWED"),
                new UnifiedSet<>(indexedSchema.getRelationshipTypeIDToStr().values()));

    }


    @Test
    public void convertMovieDBToRDF() throws FileNotFoundException {
        File outputFile = TestUtil.getResource("temp/movie-db-test.ttl");
        Neo4jDBToTurtle neo4jDBToTurtle = new Neo4jDBToTurtle(neoStores, config, new FileOutputStream(outputFile));
        neo4jDBToTurtle.startProcessing();
        RDF4JInMemoryStore store = new RDF4JInMemoryStore(List.of(outputFile));
        Set<String> assignedClasses = store.getAssignedClasses(config.getBasePrefix() + "node-1");
        Assertions.assertEquals(Set.of(config.getBasePrefix() + "Person"), assignedClasses);

        Set<String> allClasses = store.getInstances(OWL.CLASS.toString());
        Assertions.assertTrue(allClasses.contains(config.getBasePrefix() + "Person"));
        Assertions.assertTrue(allClasses.contains(config.getBasePrefix() + "Movie"));

    }

    @Test
    public void nodeProcessor() {
        Map<Long, String> propertyKeyMap = schema.getPropertyKeyIDToStr();
        NodeProcessor nodeProcessor = new NodeProcessor(neoStores) {
            @Override
            protected void process(long nodeID, long assignedLabelID) {
                String label = schema.getLabelIDToStr().get(assignedLabelID);
                Assert.isTrue(label != null);
                log.info(nodeID + " " + label);
            }

            @Override
            protected void process(long nodeID, long propertyKeyID, Value value) {
                log.info(nodeID + " " + propertyKeyMap.get(propertyKeyID) + " " + value);
            }
        };
        nodeProcessor.startProcessing();
    }

    @Test
    public void relationshipProcessor() {
        Map<Long, String> relTypeMap = schema.getRelationshipTypeIDToStr();
        Map<Long, String> propertyKeyMap = schema.getPropertyKeyIDToStr();
        RelationshipProcessor relationshipProcessor = new RelationshipProcessor(neoStores) {
            @Override
            protected void process(long relationshipID, long sourceID, long targetID, long typeID, boolean statementHasAnnotations) {
                log.info(relationshipID + " " + sourceID + " ==" + relTypeMap.get(typeID) + "==> " + targetID);
            }

            @Override
            protected void process(long relationshipID, long propertyKeyID, Value value) {
                log.info(relationshipID + " " + propertyKeyMap.get(propertyKeyID) + " " + value);
            }
        };
        relationshipProcessor.startProcessing();
    }


    @Test
    public void propertyProcessor() {
        PropertyProcessor propertyProcessor = new PropertyProcessor(neoStores) {
            @Override
            protected void processNodeProperty(long nodeID, long propertyKeyID, PropertyType propertyType, Value value) {
                log.info("Node: " + nodeID);
                log.info(propertyType.toString());
                log.info(value.toString());
                log.info(ConsoleUtil.getSeparator());
            }

            @Override
            protected void processRelationshipProperty(long relationshipID, long propertyKeyID, PropertyType propertyType, Value value) {
                log.info("Relationship: " + relationshipID);
                log.info(propertyType.toString());
                log.info(value.toString());
                log.info(ConsoleUtil.getSeparator());
            }

            @Override
            protected void processSchemaRuleProperty(long schemaEntityID, long propertyKeyID, PropertyType propertyType, Value value) {
                log.info("Schema Entity: " + schemaEntityID);
                log.info(propertyType.toString());
                log.info(value.toString());
                log.info(ConsoleUtil.getSeparator());
            }
        };
        propertyProcessor.startProcessing();
    }
}
