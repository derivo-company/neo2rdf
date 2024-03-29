package de.derivo.neo2rdf.conversion;

import com.sun.net.httpserver.HttpServer;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.util.ConsoleUtil;
import org.neo4j.kernel.impl.store.NeoStores;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Neo4jToTurtleConversionServer {

    private final int portNumber;
    private final int numberOfServerThreads;
    private final NeoStores neoStores;
    private final Logger log = ConsoleUtil.getLogger();
    private final ConversionConfig config;
    private final File neo4jDBDirectory;

    public Neo4jToTurtleConversionServer(File neo4jDBDirectory,
                                         NeoStores neoStores,
                                         ConversionConfig config,
                                         int portNumber, int numberOfServerThreads) {
        this.portNumber = portNumber;
        this.numberOfServerThreads = numberOfServerThreads;
        this.neoStores = neoStores;
        this.config = config;
        this.neo4jDBDirectory = neo4jDBDirectory;
    }

    public void startServer() {
        HttpServer server = null;
        try {
            server = HttpServer.create();
            log.info("Starting neo4j to RDF conversion server on port %d with %d thread(s)..."
                    .formatted(portNumber, numberOfServerThreads));
            server.bind(new InetSocketAddress("localhost", this.portNumber), 0);
            server.createContext("/", exchange -> {
                log.info(ConsoleUtil.getSeparator());
                log.info("New client connected. Converting data...");
                exchange.getResponseHeaders().add("Content-Type", "text/ttl");
                exchange.getResponseHeaders().add("Transfer-Encoding", "chunked");

                String filename = "%s_neo2rdf.ttl".formatted(neo4jDBDirectory.getName());
                String contentDisposition = "attachment; filename=%s".formatted(filename);
                exchange.getResponseHeaders().add("Content-Disposition", contentDisposition);
                exchange.sendResponseHeaders(200, 0);

                OutputStream responseBody = exchange.getResponseBody();
                Neo4jDBToTurtle neo4jDBToTurtle = new Neo4jDBToTurtle(neoStores, config, responseBody);
                neo4jDBToTurtle.startProcessing();

                responseBody.flush();
                responseBody.close();
                log.info("Conversion finished.");
            });

            ExecutorService executor = Executors.newFixedThreadPool(numberOfServerThreads);
            server.setExecutor(executor);

            server.start();

            log.info("Server started. Press enter to stop.");
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
            // Stop the server and the executor
            server.stop(0);

            log.info("Server stopped.");
        } catch (IOException e) {
            throw new IllegalStateException("An error occurred. Terminating Neo4j to RDF conversion server.", e);
        } finally {
            if (server != null) {
                server.stop(0);
            }
        }
    }
}
