package de.derivo.neo4jconverter.rdf;

import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import org.neo4j.kernel.impl.store.NeoStores;
import picocli.CommandLine;

public class Neo4jToRDFConversionServerCLApp extends Neo4jToRDFConversionCLApp implements Runnable {
    @CommandLine.Option(names = {"-p", "--port"},
            required = true)
    private int port;


    public static void main(String[] args) {
        int exitCode = new CommandLine(new Neo4jToRDFConversionServerCLApp()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        NeoStores neoStores = getNeo4jStore();
        ConversionConfig config = getConversionConfig();
        Neo4jToTurtleConversionServer server = new Neo4jToTurtleConversionServer(port, neoStores, config);
        server.startServer();
    }
}
