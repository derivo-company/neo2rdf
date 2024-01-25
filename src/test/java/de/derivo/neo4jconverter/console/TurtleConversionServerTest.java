package de.derivo.neo4jconverter.console;

import de.derivo.neo4jconverter.TestUtil;
import de.derivo.neo4jconverter.rdf.Neo4jToRDFConversionServerCLApp;
import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import de.derivo.neo4jconverter.rdf.config.ConversionConfigBuilder;
import de.derivo.neo4jconverter.util.SequenceConversionType;

import java.io.File;

public class TurtleConversionServerTest {
    private static final ConversionConfig config = ConversionConfigBuilder.newBuilder()
            .setSequenceConversionType(SequenceConversionType.SEPARATE_LITERALS)
            .build();

    public static void main(String[] args) {
        File configPath = TestUtil.getResource("temp/configTest.yaml");
        config.write(configPath);

        args = new String[]{"--neo4jDBDirectory=%s".formatted(TestUtil.getResource("neo4j-db-example").toString()),
                "--config=%s".formatted(configPath.toString()),
                "--port=8080"};

        Neo4jToRDFConversionServerCLApp.main(args);
    }
}
