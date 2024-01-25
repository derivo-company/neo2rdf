package de.derivo.neo2rdf.console;

import de.derivo.neo2rdf.TestUtil;
import de.derivo.neo2rdf.conversion.cli.Neo4jToRDFConversionCLI;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.config.ConversionConfigBuilder;
import de.derivo.neo2rdf.util.ReificationVocabulary;
import de.derivo.neo2rdf.util.SequenceConversionType;
import org.eclipse.rdf4j.model.vocabulary.FOAF;

import java.io.File;

public class TurtleConversionCLAppWithParametersTest {

    private static final ConversionConfig config = ConversionConfigBuilder.newBuilder()
            .setSequenceConversionType(SequenceConversionType.SEPARATE_LITERALS)
            .setDerivePropertyHierarchyByRelationshipSubsetCheck(true)
            .setDeriveClassHierarchyByLabelSubsetCheck(true)
            .setSchemaOutputPath(TestUtil.getTempFile("test-schema.ttl"))
            .build();

    public static void main(String[] args) {
        File configPath = TestUtil.getTempFile("config.yaml");
        config.write(configPath);
        File outputPath = TestUtil.getTempFile("command-line-conversion-output.ttl");
        File schemaOutputPath = TestUtil.getTempFile("command-line-conversion-schema-output.ttl");

        args = new String[]{"--neo4jDBDirectory=%s".formatted(TestUtil.getResource("neo4j-derived-type-hierarchy").toString()),
                "--basePrefix=%s".formatted(FOAF.NAMESPACE),
                "--reificationVocabulary=%s".formatted(ReificationVocabulary.OWL_REIFICATION),
                "--sequenceConversionType=%s".formatted(SequenceConversionType.SEPARATE_LITERALS),
                "--includeDeletedNeo4jLabels=%s".formatted(true),
                "--includeDeletedPropertyKeys=%s".formatted(true),
                "--includeDeletedRelationshipTypes=%s".formatted(true),
                "--deriveClassHierarchyByLabelSubsetCheck=%s".formatted(true),
                "--derivePropertyHierarchyByRelationshipSubsetCheck=%s".formatted(true),
                "--schemaOutputPath=%s".formatted(schemaOutputPath),
                "dump --outputPath=%s".formatted(outputPath),
        };
        Neo4jToRDFConversionCLI.main(args);
    }
}
