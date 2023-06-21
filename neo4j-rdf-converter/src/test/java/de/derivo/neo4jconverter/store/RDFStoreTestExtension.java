package de.derivo.neo4jconverter.store;

import de.derivo.neo4jconverter.TestUtil;
import de.derivo.neo4jconverter.rdf.Neo4jDBToTurtle;
import de.derivo.neo4jconverter.rdf.Neo4jStoreFactory;
import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.neo4j.kernel.impl.store.NeoStores;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

public class RDFStoreTestExtension extends RDF4JInMemoryStore implements AfterEachCallback, BeforeEachCallback {
    private RDF4JInMemoryStore rdf4JInMemoryStore;

    private NeoStores neoStores;
    private final String neo4jDBDirectory;

    public RDFStoreTestExtension(String neo4jDBDirectory) {
        this.neo4jDBDirectory = neo4jDBDirectory;
    }

    public void convertAndImportIntoStore(String outputFileName, ConversionConfig config) {
        clearStore();

        File testStoreDir = TestUtil.getResource(neo4jDBDirectory);
        neoStores = Neo4jStoreFactory.getNeo4jStore(testStoreDir);

        File outputFile = TestUtil.getResource("temp/" + outputFileName);
        Neo4jDBToTurtle neo4jDBToTurtle = null;
        try {
            neo4jDBToTurtle = new Neo4jDBToTurtle(neoStores, config, new FileOutputStream(outputFile));
            neo4jDBToTurtle.startProcessing();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.importData(List.of(outputFile));
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        rdf4JInMemoryStore = new RDF4JInMemoryStore();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        rdf4JInMemoryStore.terminate();
    }
}
