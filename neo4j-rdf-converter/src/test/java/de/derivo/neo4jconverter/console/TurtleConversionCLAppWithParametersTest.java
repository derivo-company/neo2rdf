package de.derivo.neo4jconverter.console;

import de.derivo.neo4jconverter.TestUtil;
import de.derivo.neo4jconverter.rdf.Neo4jToRDFTurtleCLApp;
import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import de.derivo.neo4jconverter.rdf.config.ConversionConfigBuilder;
import de.derivo.neo4jconverter.util.ReificationVocabulary;
import de.derivo.neo4jconverter.util.SequenceConversionType;
import org.eclipse.rdf4j.model.vocabulary.FOAF;

import java.io.File;

public class TurtleConversionCLAppWithParametersTest {

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
        File schemaOutputPath = TestUtil.getResource("temp/command-line-conversion-schema-output.ttl");

        args = new String[]{"--neo4jDBDirectory=%s".formatted(TestUtil.getResource("neo4j-derived-type-hierarchy").toString()),
                "--outputPath=%s".formatted(outputPath),
                "--basePrefix=%s".formatted(FOAF.NAMESPACE),
                "--reificationVocabulary=%s".formatted(ReificationVocabulary.OWL_REIFICATION),
                "--sequenceConversionType=%s".formatted(SequenceConversionType.SEPARATE_LITERALS),
                "--includeDeletedNeo4jLabels=%s".formatted(true),
                "--includeDeletedPropertyKeys=%s".formatted(true),
                "--includeDeletedRelationshipTypes=%s".formatted(true),
                "--deriveClassHierarchyByLabelSubsetCheck=%s".formatted(true),
                "--derivePropertyHierarchyByRelationshipSubsetCheck=%s".formatted(true),
                "--schemaOutputPath=%s".formatted(schemaOutputPath),
        };
        Neo4jToRDFTurtleCLApp.main(args);
    }
}
