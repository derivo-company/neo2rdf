package de.derivo.neo2rdf.console;

import de.derivo.neo2rdf.TestUtil;
import de.derivo.neo2rdf.conversion.cli.Neo4jToRDFConversionCLI;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.config.ConversionConfigBuilder;
import de.derivo.neo2rdf.util.ReificationVocabulary;
import de.derivo.neo2rdf.util.SequenceConversionType;
import org.eclipse.rdf4j.model.vocabulary.FOAF;

import java.io.File;

public class TurtleConversionCLIWithParametersTest {

    private static final ConversionConfig config = ConversionConfigBuilder.newBuilder()
            .setSequenceConversionType(SequenceConversionType.SEPARATE_LITERALS)
            .setDerivePropertyHierarchyByRelationshipSubsetCheck(true)
            .setDeriveClassHierarchyByLabelSubsetCheck(true)
            .build();

    public static void main(String[] args) {
        File configPath = TestUtil.getTempFile("config.yaml");
        config.write(configPath);
        File outputPath = TestUtil.getTempFile("command-line-conversion-output.ttl");
        File schemaOutputPath = TestUtil.getTempFile("command-line-conversion-schema-output.ttl");

        args = new String[]{
                "dump",
                "--database=%s".formatted("neo2rdf-test-db"),
                "--uri=%s".formatted("bolt://localhost:7687"),
                "--user=%s".formatted("neo4j"),
                "--password=%s".formatted("aaaaaaaa"),
                "--basePrefix=%s".formatted(FOAF.NAMESPACE),
                "--reificationVocabulary=%s".formatted(ReificationVocabulary.OWL_REIFICATION),
                "--sequenceConversionType=%s".formatted(SequenceConversionType.SEPARATE_LITERALS),
                "--deriveClassHierarchyByLabelSubsetCheck=%s".formatted(true),
                "--derivePropertyHierarchyByRelationshipSubsetCheck=%s".formatted(true),
                "--reifyOnlyRelationshipsWithProperties=%s".formatted(true),
                "--reifyRelationships=%s".formatted(true),
                "--relationshipTypeReificationBlacklist=FRIENDS_WITH,NON_EXISTENT,KNOWS",
                "--schemaOutputPath=%s".formatted(schemaOutputPath),
                "--outputPath=%s".formatted(outputPath),
        };
        Neo4jToRDFConversionCLI.main(args);
    }
}
