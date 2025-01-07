package de.derivo.neo2rdf.conversion.cli;

import de.derivo.neo2rdf.conversion.Neo4jToTurtleConversionServer;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import picocli.CommandLine;

@CommandLine.Command(name = "server",
        header = "Starts an HTTP server that serves the conversion result as RDF Turtle stream",
        showDefaultValues = true,
        usageHelpWidth = 95,
        description = """
                The application starts an HTTP server for the provided Neo4j database. When a GET request is sent to the server, the conversion procedure is initiated and the response returns an RDF Turtle stream to the client.
                Exemplary usage: `server -db=./path/to/neo4jdb -p=8080`
                """)
public class Neo4jToRDFConversionServerCommand implements Runnable {

    @CommandLine.Mixin
    private ConversionOptions options;

    @CommandLine.Option(names = {"-p", "--port"},
            required = true)
    private int port = 8080;

    @CommandLine.Option(names = {"-t", "--numberOfServerThreads"})
    private int numberOfServerThreads = 2;


    @Override
    public void run() {
        ConversionConfig config = options.getConversionConfig();
        Neo4jToTurtleConversionServer server = new Neo4jToTurtleConversionServer(options.getNeo4jDBConnector(),
                config,
                port, numberOfServerThreads);
        server.startServer();
    }
}
