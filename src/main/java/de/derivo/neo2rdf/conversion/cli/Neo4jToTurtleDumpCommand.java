package de.derivo.neo2rdf.conversion.cli;

import de.derivo.neo2rdf.conversion.Neo4jDBToTurtle;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import picocli.CommandLine;

import java.io.File;
import java.io.FileOutputStream;

@CommandLine.Command(name = "dump",
        header = "Converts a Neo4j database into an RDF file in Turtle format",
        showDefaultValues = true,
        usageHelpWidth = 95,
        description = """
                The Neo4j database is converted into an RDF file in Turtle format, which is written to the specified location on disk.
                Exemplary usage: `dump --database="someDBName" \\
                                  --uri="bolt://localhost:7687" \\
                                  --user="neo4j" \\
                                  --password="PASSWORD123" \\
                                  --outputPath=output/path/data.ttl`
                """)
public class Neo4jToTurtleDumpCommand implements Runnable {

    @CommandLine.Mixin
    private ConversionOptions options;

    @CommandLine.Option(names = {"-o", "--outputPath"},
            required = true)
    private File outputPath;


    @Override
    public void run() {
        try (Neo4jDBConnector neo4jDBConnector = options.getNeo4jDBConnector()) {
            ConversionConfig config = options.getConversionConfig();

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                Neo4jDBToTurtle neo4jDBToTurtle = new Neo4jDBToTurtle(neo4jDBConnector, config, fos);
                neo4jDBToTurtle.startProcessing();
            }
        } catch (Exception e) {
            throw new RuntimeException("Export failed", e);
        }
    }
}
