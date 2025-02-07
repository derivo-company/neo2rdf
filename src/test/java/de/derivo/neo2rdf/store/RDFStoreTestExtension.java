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
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RDFStoreTestExtension extends RDF4JInMemoryStore implements AfterEachCallback, BeforeEachCallback {

    private final List<String> cypherCreateQueries;
    private Neo4jTestDBStub neo4jDBConnector;

    private Set<String> collectedNeo4jLabels = Collections.emptySet();
    private Set<String> collectedNeo4jRelationshipTypes = Collections.emptySet();
    private Set<String> collectedNeo4jPropertyKeys = Collections.emptySet();

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
            this.collectedNeo4jLabels = neo4jDBToTurtle.getDeployedNeo4jLabels();
            this.collectedNeo4jPropertyKeys = neo4jDBToTurtle.getDeployedPropertyKeys();
            this.collectedNeo4jRelationshipTypes = neo4jDBToTurtle.getDeployedRelationshipTypes();
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

    public Set<String> getCollectedNeo4jLabels() {
        return collectedNeo4jLabels;
    }

    public Set<String> getCollectedNeo4jRelationshipTypes() {
        return collectedNeo4jRelationshipTypes;
    }

    public Set<String> getCollectedNeo4jPropertyKeys() {
        return collectedNeo4jPropertyKeys;
    }
}
