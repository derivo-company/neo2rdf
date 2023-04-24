package de.derivo.neo4jconverter;

import de.derivo.neo4jconverter.rdf.*;
import de.derivo.neo4jconverter.utils.ConsoleUtils;
import org.apache.shiro.util.Assert;
import org.eclipse.rdf4j.model.Statement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.io.pagecache.context.CursorContext;
import org.neo4j.kernel.impl.store.NeoStores;
import org.neo4j.kernel.impl.store.PropertyType;
import org.neo4j.kernel.impl.store.cursor.CachedStoreCursors;
import org.neo4j.values.storable.Value;
import de.derivo.neo4jconverter.processors.NodeProcessor;
import de.derivo.neo4jconverter.processors.PropertyProcessor;
import de.derivo.neo4jconverter.processors.RelationshipProcessor;
import de.derivo.neo4jconverter.schema.IndexedNeo4jSchema;
import de.derivo.neo4jconverter.schema.IndexedNeo4jSchemaGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;

public class ConversionTests extends ConverterTestBase {

    private static NeoStores neoStores;
    private static IndexedNeo4jSchema schema;
    private static CachedStoreCursors storeCursors;
    private static String baseIRI = "https://www.example.org/";


    @BeforeAll
    public static void initStore() {
        File testStoreDir = getResource("neo4j-db-example");
        neoStores = Neo4jStoreFactory.getNeo4jStore(testStoreDir);
        storeCursors = new CachedStoreCursors(neoStores, CursorContext.NULL_CONTEXT);

        IndexedNeo4jSchemaGenerator gen = new IndexedNeo4jSchemaGenerator(neoStores);
        schema = gen.generate();
    }


    @Test
    public void indexNeo4jSchema() {
        IndexedNeo4jSchemaGenerator gen = new IndexedNeo4jSchemaGenerator(neoStores);
        IndexedNeo4jSchema indexedSchema = gen.generate();
        log.info(ConsoleUtils.getSeparator());
        log.info("Labels:");
        log.info(ConsoleUtils.getSeparator());
        log.info(indexedSchema.getLabelIDToStr().toString());

        log.info(ConsoleUtils.getSeparator());
        log.info("Property Keys:");
        log.info(ConsoleUtils.getSeparator());
        log.info(indexedSchema.getPropertyKeyIDToStr().toString());

        log.info(ConsoleUtils.getSeparator());
        log.info("Relationship Types:");
        log.info(ConsoleUtils.getSeparator());
        log.info(indexedSchema.getRelationshipTypeIDToStr().toString());
    }

    public void tcpNeo4jDBToTurtleConverterServer() {
        Neo4jToTurtleConversionServer server = new Neo4jToTurtleConversionServer(8080, neoStores, baseIRI);
        server.startServer();
    }

    public void turtleConversionCLApp() {
        String[] args = {"--neo4jDBDirectory=" + getResource("neo4j-db-example").toPath(),
                "--baseIRI=http://www.example.org/",
                "--outputPath=conversion-result.ttl"};
        Neo4jToRDFTurtleCLApp.main(args);
    }

    @Test
    public void neo4jToRDFConverter() throws FileNotFoundException {
        Neo4jToRDFConverter neo4jToRDFConverter = new Neo4jToRDFConverter(neoStores, baseIRI) {
            @Override
            protected void processStatement(Statement s) {
                log.info(s.toString());
            }

            @Override
            protected void onStart() {
            }

            @Override
            protected void onFinish() {
            }
        };
        neo4jToRDFConverter.startProcessing();

        File outputFile = getResource("temp/test.ttl");
        Neo4jDBToTurtle neo4jDBToTurtle = new Neo4jDBToTurtle(neoStores, baseIRI, new FileOutputStream(outputFile));
        neo4jDBToTurtle.startProcessing();
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
                log.info(ConsoleUtils.getSeparator());
            }

            @Override
            protected void processRelationshipProperty(long relationshipID, long propertyKeyID, PropertyType propertyType, Value value) {
                log.info("Relationship: " + relationshipID);
                log.info(propertyType.toString());
                log.info(value.toString());
                log.info(ConsoleUtils.getSeparator());
            }

            @Override
            protected void processSchemaRuleProperty(long schemaEntityID, long propertyKeyID, PropertyType propertyType, Value value) {
                log.info("Schema Entity: " + schemaEntityID);
                log.info(propertyType.toString());
                log.info(value.toString());
                log.info(ConsoleUtils.getSeparator());
            }
        };
        propertyProcessor.startProcessing();
    }
}
