package de.derivo.neo2rdf.store;

import de.derivo.neo2rdf.TestUtil;
import de.derivo.neo2rdf.conversion.Neo4jDBToTurtle;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

public class RDFStoreTestExtension extends RDF4JInMemoryStore implements AfterEachCallback, BeforeEachCallback {

    private String cypherCreateQuery;
    private Neo4jDBConnector neo4jDBConnector;
    // TODO adjust
    private String uri = "bolt://localhost:7687";
    private String user = "neo4j";
    private String password = "aaaaaaaa";
    private String database = "neo2rdf-test-db";

    public RDFStoreTestExtension(String cypherCreateQuery) {
        this.cypherCreateQuery = cypherCreateQuery;
        init();
    }

    public RDFStoreTestExtension(String cypherCreateQuery, boolean rdfsReasoning) {
        this.cypherCreateQuery = cypherCreateQuery;
        this.rdfsReasoning = rdfsReasoning;
        init();
    }

    public void convertAndImportIntoStore(String outputFileName, ConversionConfig config) {
        convertAndImportIntoStore(outputFileName, config, false);
    }

    private void init() {
        this.neo4jDBConnector = new Neo4jDBConnector(uri, user, password, database);
        this.neo4jDBConnector.clearDatabase();
        this.neo4jDBConnector.query(this.cypherCreateQuery, (r) -> {
        });
    }

    public void convertAndImportIntoStore(String outputFileName, ConversionConfig config, boolean rdfsReasoning) {
        clearStore();

        File outputFile = TestUtil.getTempFile(outputFileName);
        outputFile.getParentFile().mkdirs();
        Neo4jDBToTurtle neo4jDBToTurtle;
        try {
            neo4jDBToTurtle = new Neo4jDBToTurtle(neo4jDBConnector, config, new FileOutputStream(outputFile));
            neo4jDBToTurtle.startProcessing();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (rdfsReasoning) {
            materializeDataset(outputFile, outputFile);
        }

        this.importData(List.of(outputFile));
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        init(List.of());
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        terminate();
    }
}
