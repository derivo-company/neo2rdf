package de.derivo.neo2rdf.store;

import de.derivo.neo2rdf.TestUtil;
import de.derivo.neo2rdf.conversion.Neo4jDBToTurtle;
import de.derivo.neo2rdf.conversion.Neo4jStoreFactory;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
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
    private final File neo4jDBDirectory;

    private File neo4jDumpPath = null;

    public RDFStoreTestExtension(File neo4jDBDirectory) {
        this.neo4jDBDirectory = neo4jDBDirectory;
    }

    public RDFStoreTestExtension(File neo4jDumpPath, File neo4jDBDirectory) {
        this.neo4jDumpPath = neo4jDumpPath;
        this.neo4jDBDirectory = neo4jDBDirectory;
    }

    public RDFStoreTestExtension(File neo4jDBDirectory, boolean rdfsReasoning) {
        this.rdfsReasoning = rdfsReasoning;
        this.neo4jDBDirectory = neo4jDBDirectory;
    }

    public void convertAndImportIntoStore(String outputFileName, ConversionConfig config) {
        convertAndImportIntoStore(outputFileName, config, false);
    }

    public void convertAndImportIntoStore(String outputFileName, ConversionConfig config, boolean rdfsReasoning) {
        clearStore();

        if (neoStores != null) {
            neoStores.close();
        }

        if (neo4jDumpPath != null) {
            try {
                FileUtils.deleteDirectory(neo4jDBDirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            neoStores = Neo4jStoreFactory.getNeo4jStoreFromDump(neo4jDumpPath, neo4jDBDirectory);
        } else {
            neoStores = Neo4jStoreFactory.getNeo4jStore(neo4jDBDirectory);
        }

        File outputFile = TestUtil.getTempFile(outputFileName);
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
