package de.derivo.neo4jconverter.rdf;

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

    @CommandLine.Option(names = {"-b", "--baseIRI"},
            required = false,
            defaultValue = "https://neo4j.com/")
    private String baseIRI;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Neo4jToRDFTurtleCLApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        NeoStores neoStores = Neo4jStoreFactory.getNeo4jStore(neo4jDBDirectory);
        try {
            Neo4jDBToTurtle neo4jDBToTurtle = new Neo4jDBToTurtle(neoStores, baseIRI, new FileOutputStream(outputPath));
            neo4jDBToTurtle.startProcessing();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
