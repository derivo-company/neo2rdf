package de.derivo.neo2rdf.store;

import de.derivo.neo2rdf.Neo4jTestDBStub;
import de.derivo.neo2rdf.TestUtil;
import de.derivo.neo2rdf.conversion.Neo4jDBToTurtle;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RDFStoreTestExtension extends RDF4JInMemoryStore
        implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback {

    @SuppressWarnings("resource")
    private static final Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>(DockerImageName.parse("neo4j:2026.01.4"))
            .withAdminPassword("password")
            .withReuse(true);

    static {
        neo4jContainer.start();
    }

    private final List<String> cypherCreateQueries;
    private Neo4jTestDBStub neo4jDBConnector;
    private Driver driver;

    private Set<String> collectedNeo4jLabels = Collections.emptySet();
    private Set<String> collectedNeo4jRelationshipTypes = Collections.emptySet();
    private Set<String> collectedNeo4jPropertyKeys = Collections.emptySet();

    public RDFStoreTestExtension(List<String> cypherCreateQueries) {
        super(Collections.emptyList());
        this.cypherCreateQueries = cypherCreateQueries;
    }

    public RDFStoreTestExtension(List<String> cypherCreateQueries, boolean rdfsReasoning) {
        super(Collections.emptyList(), rdfsReasoning);
        this.cypherCreateQueries = cypherCreateQueries;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        this.driver = GraphDatabase.driver(
                neo4jContainer.getBoltUrl(),
                AuthTokens.basic("neo4j", neo4jContainer.getAdminPassword())
        );

        this.neo4jDBConnector = new Neo4jTestDBStub(this.driver);

        neo4jDBConnector.clearDatabase();
        neo4jDBConnector.updateQuery(cypherCreateQueries);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        this.init(Collections.emptyList());
    }

    @Override
    public void afterAll(ExtensionContext context) {
        this.neo4jDBConnector.close();
        this.terminate();
    }

    public void convertAndImportIntoStore(String outputFileName, ConversionConfig config) {
        convertAndImportIntoStore(outputFileName, config, false);
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
            throw new RuntimeException("Could not create output file for Turtle export", e);
        }

        if (rdfsReasoning) {
            materializeDataset(outputFile, outputFile);
        }

        this.importData(List.of(outputFile));
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