package de.derivo.neo4jconverter;

import de.derivo.neo4jconverter.rdf.Neo4jDBToTurtle;
import de.derivo.neo4jconverter.rdf.Neo4jStoreFactory;
import de.derivo.neo4jconverter.schema.IndexedNeo4jSchema;
import de.derivo.neo4jconverter.schema.IndexedNeo4jSchemaGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.io.pagecache.context.CursorContext;
import org.neo4j.kernel.impl.store.NeoStores;
import org.neo4j.kernel.impl.store.cursor.CachedStoreCursors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Neo4jLabelHistoryTests extends ConverterTestBase {

    private static NeoStores neoStores;
    private static IndexedNeo4jSchema schema;
    private static CachedStoreCursors storeCursors;
    private static String baseIRI = "https://www.example.org/";


    @BeforeAll
    public static void initStore() {
        File testStoreDir = getResource("neo4j-label-history");
        neoStores = Neo4jStoreFactory.getNeo4jStore(testStoreDir);
        storeCursors = new CachedStoreCursors(neoStores, CursorContext.NULL_CONTEXT);

        IndexedNeo4jSchemaGenerator gen = new IndexedNeo4jSchemaGenerator(neoStores);
        schema = gen.generate();
    }

    @Test
    public void neo4jToRDFConverter() throws FileNotFoundException {
        File outputFile = getResource("temp/neo4j-label-history.ttl");
        Neo4jDBToTurtle neo4jDBToTurtle = new Neo4jDBToTurtle(neoStores, baseIRI, new FileOutputStream(outputFile));
        neo4jDBToTurtle.startProcessing();
    }
}
