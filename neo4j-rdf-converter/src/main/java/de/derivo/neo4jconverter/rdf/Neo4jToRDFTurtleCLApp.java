package de.derivo.neo4jconverter.rdf;

import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import de.derivo.neo4jconverter.rdf.config.ConversionConfigBuilder;
import org.neo4j.kernel.impl.store.NeoStores;
import picocli.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Neo4jToRDFTurtleCLApp implements Runnable {

    @CommandLine.Option(names = {"-o", "--outputPath"},
            required = true)
    private File outputPath;

    @CommandLine.Option(names = {"-db", "--neo4jDBDirectory"},
            required = true)
    private File neo4jDBDirectory;

    @CommandLine.Option(names = {"-cfg", "--config"},
            required = false)
    private File conversionConfigFile;


    public static void main(String[] args) {
        int exitCode = new CommandLine(new Neo4jToRDFTurtleCLApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        NeoStores neoStores = Neo4jStoreFactory.getNeo4jStore(neo4jDBDirectory);
        try {
            ConversionConfig config = conversionConfigFile != null ?
                    ConversionConfig.read(conversionConfigFile) :
                    ConversionConfigBuilder.newBuilder().build();
            Neo4jDBToTurtle neo4jDBToTurtle = new Neo4jDBToTurtle(neoStores, config, new FileOutputStream(outputPath));
            neo4jDBToTurtle.startProcessing();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
