package de.derivo.neo2rdf.conversion.cli;

/*-
 * #%L
 * neo2rdf
 * %%
 * Copyright (C) 2026 Derivo Company
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import de.derivo.neo2rdf.conversion.Neo4jToTurtleConversionServer;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.processors.Neo4jDBServerConnector;
import picocli.CommandLine;

@SuppressWarnings("CanBeFinal")
@CommandLine.Command(name = "server",
        header = "Starts an HTTP server that serves the conversion result as RDF Turtle stream",
        showDefaultValues = true,
        usageHelpWidth = 95,
        description = """
                The application starts an HTTP server for the provided Neo4j database. When a GET request is sent to the server, the conversion procedure is initiated and the response returns an RDF Turtle stream to the client.
                Exemplary usage: `server --database="neo4j" \\
                                    --uri="neo4j+s://867928679.databases.neo4j.io" \\
                                    --user="neo4j" \\
                                    --password="eBWczH5dRt2VR1C1eYKvk5jRt2VR1C1eY72NUCk" \\
                                    --port=8080`
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
        try (Neo4jDBServerConnector neo4jDBConnector = options.getNeo4jDBConnector()) {
            Neo4jToTurtleConversionServer server = new Neo4jToTurtleConversionServer(neo4jDBConnector,
                    config,
                    port, numberOfServerThreads);
            server.startServer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
