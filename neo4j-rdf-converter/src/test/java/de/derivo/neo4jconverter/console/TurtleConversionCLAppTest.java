package de.derivo.neo4jconverter.console;

import de.derivo.neo4jconverter.TestUtil;
import de.derivo.neo4jconverter.rdf.Neo4jToRDFTurtleCLApp;
import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import de.derivo.neo4jconverter.rdf.config.ConversionConfigBuilder;
import de.derivo.neo4jconverter.util.SequenceConversionType;

import java.io.File;

public class TurtleConversionCLAppTest {

    private static final ConversionConfig config = ConversionConfigBuilder.newBuilder()
            .setSequenceConversionType(SequenceConversionType.SEPARATE_LITERALS)
            .setDerivePropertyHierarchyByRelationshipSubsetCheck(true)
            .setDeriveClassHierarchyByLabelSubsetCheck(true)
            .setSchemaOutputPath(TestUtil.getResource("temp/test-schema.ttl"))
            .build();

    public static void main(String[] args) {
        File configPath = TestUtil.getResource("temp/config.yaml");
        config.write(configPath);
        File outputPath = TestUtil.getResource("temp/command-line-conversion-output.ttl");

        args = new String[]{"--neo4jDBDirectory=%s".formatted(TestUtil.getResource("neo4j-derived-type-hierarchy").toString()),
                "--config=%s".formatted(configPath.toString()),
                "--outputPath=%s".formatted(outputPath)};
        Neo4jToRDFTurtleCLApp.main(args);
    }
}
