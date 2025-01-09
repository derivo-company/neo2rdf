package de.derivo.neo2rdf.console;

import de.derivo.neo2rdf.TestUtil;
import de.derivo.neo2rdf.conversion.cli.Neo4jToRDFConversionCLI;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.config.ConversionConfigBuilder;
import de.derivo.neo2rdf.util.SequenceConversionType;

import java.io.File;

public class TurtleConversionCLIServerTest {
    private static final ConversionConfig config = ConversionConfigBuilder.newBuilder()
            .setSequenceConversionType(SequenceConversionType.SEPARATE_LITERALS)
            .build();

    public static void main(String[] args) {
        File configPath = TestUtil.getTempFile("configTest.yaml");
        config.write(configPath);

        args = new String[]{
                "server",
                "--database=%s".formatted("neo2rdf-test-db"),
                "--uri=%s".formatted("bolt://localhost:7687"),
                "--user=%s".formatted("neo4j"),
                "--password=%s".formatted("aaaaaaaa"),
                "--config=%s".formatted(configPath.toString()),
                "--port=8080"};

        Neo4jToRDFConversionCLI.main(args);
    }
}
