package de.derivo.neo4jconverter;

import de.derivo.neo4jconverter.rdf.Neo4jDBToTurtle;
import de.derivo.neo4jconverter.rdf.Neo4jStoreFactory;
import de.derivo.neo4jconverter.rdf.Neo4jToRDFConverter;
import org.eclipse.rdf4j.model.Statement;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.kernel.impl.store.NeoStores;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Neo4jDatatypeTests extends ConverterTestBase {

    private static NeoStores neoStores;
    private static String baseIRI = "https://www.example.org/";

    @BeforeAll
    public static void initStore() {
        File testStoreDir = getResource("neo4j-datatypes-example");
        neoStores = Neo4jStoreFactory.getNeo4jStore(testStoreDir);
    }

    @Test
    public void neo4jToRDFConverter() throws FileNotFoundException {
        Neo4jToRDFConverter neo4jToRDFConverter = new Neo4jToRDFConverter(neoStores, baseIRI) {
            @Override
            protected void processStatement(Statement s) {
                System.out.println(s);
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
}
