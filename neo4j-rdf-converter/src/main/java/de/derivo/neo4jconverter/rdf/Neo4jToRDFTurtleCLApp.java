package de.derivo.neo4jconverter.rdf;

import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import org.neo4j.kernel.impl.store.NeoStores;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Neo4jToRDFTurtleCLApp extends Neo4jToRDFConversionCLApp implements Runnable {

    @CommandLine.Option(names = {"-o", "--outputPath"},
            required = true)
    private File outputPath;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Neo4jToRDFTurtleCLApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        try {
            ConversionConfig config = getConversionConfig();
            NeoStores neoStores = Neo4jStoreFactory.getNeo4jStore(neo4jDBDirectory);
            Neo4jDBToTurtle neo4jDBToTurtle;
            neo4jDBToTurtle = new Neo4jDBToTurtle(neoStores, config, new FileOutputStream(outputPath));
            neo4jDBToTurtle.startProcessing();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
