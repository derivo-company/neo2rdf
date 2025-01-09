package de.derivo.neo2rdf.store;

import de.derivo.neo2rdf.Neo4jTestDBStub;
import de.derivo.neo2rdf.TestUtil;
import de.derivo.neo2rdf.conversion.Neo4jDBToTurtle;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

public class RDFStoreTestExtension extends RDF4JInMemoryStore implements AfterEachCallback, BeforeEachCallback {

    private final List<String> cypherCreateQueries;
    private Neo4jTestDBStub neo4jDBConnector;

    public RDFStoreTestExtension(List<String> cypherCreateQueries) {
        this.cypherCreateQueries = cypherCreateQueries;
        init();
    }

    public RDFStoreTestExtension(List<String> cypherCreateQueries, boolean rdfsReasoning) {
        this.cypherCreateQueries = cypherCreateQueries;
        this.rdfsReasoning = rdfsReasoning;
        init();
    }

    public void convertAndImportIntoStore(String outputFileName, ConversionConfig config) {
        convertAndImportIntoStore(outputFileName, config, false);
    }

    private void init() {
        this.neo4jDBConnector = new Neo4jTestDBStub();
        this.neo4jDBConnector.clearDatabase();
        this.neo4jDBConnector.updateQuery(cypherCreateQueries);
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

    public Neo4jTestDBStub getNeo4jDBConnector() {
        return neo4jDBConnector;
    }
}
