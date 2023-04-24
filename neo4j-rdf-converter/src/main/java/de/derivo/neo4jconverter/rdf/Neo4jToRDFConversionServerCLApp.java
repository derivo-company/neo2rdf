package de.derivo.neo4jconverter.rdf;

import org.neo4j.kernel.impl.store.NeoStores;
import picocli.CommandLine;

import java.io.File;

public class Neo4jToRDFConversionServerCLApp implements Runnable {
    @CommandLine.Option(names = {"-o", "--outputPath"},
            required = true)
    private File outputPath;

    @CommandLine.Option(names = {"-p", "--port"},
            required = true)
    private int port;

    @CommandLine.Option(names = {"-db", "--neo4jDBDirectory"},
            required = true)
    private File neo4jDBDirectory;

    @CommandLine.Option(names = {"-b", "--baseIRI"},
            required = false,
            defaultValue = "https://neo4j.com/")
    private String baseIRI;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Neo4jToRDFConversionServerCLApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        NeoStores neoStores = Neo4jStoreFactory.getNeo4jStore(neo4jDBDirectory);
        Neo4jToTurtleConversionServer server = new Neo4jToTurtleConversionServer(port, neoStores, baseIRI);
        server.startServer();
    }
}
