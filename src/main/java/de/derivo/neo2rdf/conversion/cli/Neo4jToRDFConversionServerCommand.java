package de.derivo.neo2rdf.conversion.cli;

import de.derivo.neo2rdf.conversion.Neo4jToTurtleConversionServer;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import org.neo4j.kernel.impl.store.NeoStores;
import picocli.CommandLine;

@CommandLine.Command(name = "server",
        description = "Example for conversion server:    server -db=./path/to/neo4jdb -p=8080")
public class Neo4jToRDFConversionServerCommand implements Runnable {

    @CommandLine.Mixin
    private ConversionOptions options;

    @CommandLine.Option(names = {"-p", "--port"},
            required = true)
    private int port;

    @CommandLine.Option(names = {"-t", "--numberOfServerThreads"})
    private int numberOfServerThreads = 2;


    @Override
    public void run() {
        NeoStores neoStores = options.getNeo4jStore();
        ConversionConfig config = options.getConversionConfig();
        Neo4jToTurtleConversionServer server = new Neo4jToTurtleConversionServer(options.getNeo4jDBDirectory(),
                neoStores,
                config,
                port, numberOfServerThreads);
        server.startServer();
    }
}
