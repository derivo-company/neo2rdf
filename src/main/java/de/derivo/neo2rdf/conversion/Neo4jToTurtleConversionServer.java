package de.derivo.neo2rdf.conversion;

import com.sun.net.httpserver.HttpServer;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.processors.Neo4jDBServerConnector;
import de.derivo.neo2rdf.util.ConsoleUtil;
import org.tinylog.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Neo4jToTurtleConversionServer {

    private final Neo4jDBServerConnector neo4jDBConnector;
    private final int portNumber;
    private final int numberOfServerThreads;
    private final ConversionConfig config;

    public Neo4jToTurtleConversionServer(Neo4jDBServerConnector neo4jDBConnector,
                                         ConversionConfig config,
                                         int portNumber, int numberOfServerThreads) {
        this.neo4jDBConnector = neo4jDBConnector;
        this.portNumber = portNumber;
        this.numberOfServerThreads = numberOfServerThreads;
        this.config = config;
    }

    public void startServer() {
        HttpServer server = null;
        try {
            server = HttpServer.create();
            Logger.info("Starting neo4j to RDF conversion server on port %d with %d thread(s)..."
                    .formatted(portNumber, numberOfServerThreads));
            server.bind(new InetSocketAddress("localhost", this.portNumber), 0);
            server.createContext("/", exchange -> {
                Logger.info(ConsoleUtil.getSeparator());
                Logger.info("New client connected. Converting data...");
                exchange.getResponseHeaders().add("Content-Type", "text/ttl");
                exchange.getResponseHeaders().add("Transfer-Encoding", "chunked");

                String filename = "%s.ttl".formatted(neo4jDBConnector.getDatabase());
                String contentDisposition = "attachment; filename=%s".formatted(filename);
                exchange.getResponseHeaders().add("Content-Disposition", contentDisposition);
                exchange.sendResponseHeaders(200, 0);

                OutputStream responseBody = exchange.getResponseBody();
                Neo4jDBToTurtle neo4jDBToTurtle = new Neo4jDBToTurtle(neo4jDBConnector, config, responseBody);
                neo4jDBToTurtle.startProcessing();

                responseBody.flush();
                responseBody.close();
                Logger.info("Conversion finished.");
            });

            ExecutorService executor = Executors.newFixedThreadPool(numberOfServerThreads);
            server.setExecutor(executor);

            server.start();

            Logger.info("Server started. Press enter to stop.");
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
            // Stop the server and the executor
            server.stop(0);

            Logger.info("Server stopped.");
        } catch (IOException e) {
            throw new IllegalStateException("An error occurred. Terminating Neo4j to RDF conversion server.", e);
        } finally {
            if (server != null) {
                server.stop(0);
            }
        }
    }
}
