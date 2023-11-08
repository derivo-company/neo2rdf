package de.derivo.neo4jconverter;

import de.derivo.neo4jconverter.rdf.Neo4jDBToTurtle;
import de.derivo.neo4jconverter.rdf.Neo4jStoreFactory;
import de.derivo.neo4jconverter.rdf.config.ConversionConfigBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.neo4j.kernel.impl.store.NeoStores;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Neo4jConvertDumpTests {

    private final File outputDir = TestUtil.getResource("temp/neo4j-db-example");
    private final File conversionOutputPath = TestUtil.getResource("temp/dump-output.ttl");
    private final File dumpPath = TestUtil.getResource("neo4j-db-example.dump");

    @Test
    public void loadArchiveAndConvertDBTest() throws IOException {
        FileUtils.deleteDirectory(outputDir);
        NeoStores neoStores = Neo4jStoreFactory.getNeo4jStoreFromDump(dumpPath, outputDir);
        Neo4jDBToTurtle neo4jDBToTurtle;
        neo4jDBToTurtle = new Neo4jDBToTurtle(neoStores,
                ConversionConfigBuilder.newBuilder().build(),
                new FileOutputStream(conversionOutputPath));
        neo4jDBToTurtle.startProcessing();
        neoStores.close();
    }
}
