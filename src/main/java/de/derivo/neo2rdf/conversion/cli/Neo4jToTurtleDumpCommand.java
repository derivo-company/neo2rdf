package de.derivo.neo2rdf.conversion.cli;

import de.derivo.neo2rdf.conversion.Neo4jDBToTurtle;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import org.neo4j.kernel.impl.store.NeoStores;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

@CommandLine.Command(name = "dump",
        description = """
                The Neo4j database is converted into an RDF file in Turtle format, which is written to the specified location on disk.
                Exemplary usage: dump -db=./path/to/neo4jdb -o=output/path/data.ttl
                """)
public class Neo4jToTurtleDumpCommand implements Runnable {

    @CommandLine.Mixin
    private ConversionOptions options;

    @CommandLine.Option(names = {"-o", "--outputPath"},
            required = true)
    private File outputPath;


    @Override
    public void run() {
        try {
            ConversionConfig config = options.getConversionConfig();
            NeoStores neoStores = options.getNeo4jStore();
            Neo4jDBToTurtle neo4jDBToTurtle;
            neo4jDBToTurtle = new Neo4jDBToTurtle(neoStores, config, new FileOutputStream(outputPath));
            neo4jDBToTurtle.startProcessing();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
