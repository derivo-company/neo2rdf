package de.derivo.neo4jconverter.store;

import de.derivo.neo4jconverter.TestUtil;
import de.derivo.neo4jconverter.rdf.Neo4jDBToTurtle;
import de.derivo.neo4jconverter.rdf.Neo4jStoreFactory;
import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.neo4j.kernel.impl.store.NeoStores;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class RDFStoreTestExtension extends RDF4JInMemoryStore implements AfterEachCallback, BeforeEachCallback {

    private NeoStores neoStores;
    private final String neo4jDBDirectory;

    private String neo4jDumpPath = null;

    public RDFStoreTestExtension(String neo4jDBDirectory) {
        this.neo4jDBDirectory = neo4jDBDirectory;
    }

    public RDFStoreTestExtension(String neo4jDumpPath, String neo4jDBDirectory) {
        this.neo4jDumpPath = neo4jDumpPath;
        this.neo4jDBDirectory = neo4jDBDirectory;
    }

    public RDFStoreTestExtension(String neo4jDBDirectory, boolean rdfsReasoning) {
        this.rdfsReasoning = rdfsReasoning;
        this.neo4jDBDirectory = neo4jDBDirectory;
    }

    public void convertAndImportIntoStore(String outputFileName, ConversionConfig config) {
        convertAndImportIntoStore(outputFileName, config, false);
    }

    public void convertAndImportIntoStore(String outputFileName, ConversionConfig config, boolean rdfsReasoning) {
        clearStore();
        File testStoreDir = TestUtil.getResource(neo4jDBDirectory);

        if (neoStores != null) {
            neoStores.close();
        }

        if (neo4jDumpPath != null) {
            try {
                FileUtils.deleteDirectory(testStoreDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            File dumpFilePath = TestUtil.getResource(neo4jDumpPath);
            neoStores = Neo4jStoreFactory.getNeo4jStoreFromDump(dumpFilePath, testStoreDir);
        } else {
            neoStores = Neo4jStoreFactory.getNeo4jStore(testStoreDir);
        }

        File outputFile = TestUtil.getResource("temp/" + outputFileName);
        outputFile.getParentFile().mkdirs();
        Neo4jDBToTurtle neo4jDBToTurtle;
        try {
            neo4jDBToTurtle = new Neo4jDBToTurtle(neoStores, config, new FileOutputStream(outputFile));
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
        if (neoStores != null) {
            neoStores.close();
        }
    }
}