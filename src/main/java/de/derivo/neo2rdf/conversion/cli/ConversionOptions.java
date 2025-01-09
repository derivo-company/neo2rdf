package de.derivo.neo2rdf.conversion.cli;

import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.config.ConversionConfigBuilder;
import de.derivo.neo2rdf.processors.Neo4jDBServerConnector;
import de.derivo.neo2rdf.util.ReificationVocabulary;
import de.derivo.neo2rdf.util.SequenceConversionType;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConversionOptions {

    @CommandLine.Option(names = {"-db", "--database"},
            required = true,
            description = "The name of the Neo4j database to connect to."
    )
    private String neo4jDatabase = null;

    @CommandLine.Option(names = {"-u", "--user"},
            required = true,
            description = "The username for the Neo4j instance.")
    protected String neo4jUser = null;

    @CommandLine.Option(names = {"--uri"},
            required = true,
            description = "The URI for the Neo4j instance. Example: bolt://localhost:7687")
    protected String neo4jURI = null;


    @CommandLine.Option(names = {"--password"},
            required = true,
            description = "The password for the Neo4j user.")
    protected String neo4jPassword = null;

    @CommandLine.Option(names = {"-cfg", "--config"},
            description = """
                    Instead of specifying non-mandatory options in terms of command line parameters, it is also possible to use an external YAML configuration.
                    The YAML keys must have the same identifiers as the long option names, e.g., 'basePrefix: https://www.example.org/other-prefix#'.
                    """)
    private File conversionConfigFile = null;


    @CommandLine.Option(names = {"--basePrefix"},
            description = """
                    Prefix that is used for all converted nodes, property keys, relationship types, and relationships.
                    """)
    private String basePrefix = "https://www.example.org/";

    @CommandLine.Option(names = {"--reificationVocabulary"},
            description = """
                    The reification vocabulary defines how a quadruple (sbj, pred, obj, statementID) should be reified.
                    Options:
                     - `RDF_REIFICATION`: uses the RDF reification vocabulary, i.e.,  rdf:Statement, rdf:subject, rdf:predicate, and rdf:object
                        (cf. https://www.w3.org/TR/rdf11-mt/#reification)
                     - `OWL_REIFICATION`: uses the OWL vocabulary, i.e.,  owl:Axiom, rdf:annotatedSource, owl:annotatedProperty, and owl:annotatedTarget
                     (cf. https://www.w3.org/TR/owl2-quick-reference/#Annotations)
                    """)
    private ReificationVocabulary reificationVocabulary = ReificationVocabulary.OWL_REIFICATION;

    @CommandLine.Option(names = {"--reifyRelationships"},
            description = """
                    By default, each Neo4j relationship is reified in RDF by a distinct blank node.
                    If this option is set to false, no Neo4j relationships will be reified in RDF.
                    """)
    private Boolean reifyRelationships = true;

    @CommandLine.Option(names = {"--relationshipTypeReificationBlacklist"},
            description = """
                    By default, each Neo4j relationship is reified in RDF by a distinct blank node.
                    Using this option, the blacklisted Neo4j relationship types will not be reified in RDF.
                    """,
            split = ",")
    private List<String> relationshipTypeReificationBlacklist = new ArrayList<>();

    @CommandLine.Option(names = {"--reifyOnlyRelationshipsWithProperties"},
            description = """
                    By default, each Neo4j relationship is reified in RDF by a distinct blank node.
                    If this option is set, only Neo4j relationships with properties will be reified in RDF.
                    """)
    private Boolean reifyOnlyRelationshipsWithProperties = false;

    @CommandLine.Option(names = {"--sequenceConversionType"},
            description = """
                    Options:
                    - `RDF_COLLECTION`: Neo4j sequences are converted into open lists in RDF.
                    - `SEPARATE_LITERALS`: Neo4j sequences are converted into separate assertions, e.g., (x { has: [1, 2] }) is converted to (:x, :has, 1) and (:x, :has, 2).
                    """)
    private SequenceConversionType sequenceConversionType = SequenceConversionType.RDF_COLLECTION;

    @CommandLine.Option(names = {"--deriveClassHierarchyByLabelSubsetCheck"},
            description = """
                    Indicates whether the RDF class hierarchy should be derived.
                    For this purpose, it is examined which sets of Neo4j nodes with an assigned label are a subset of one another.
                    """)
    private Boolean deriveClassHierarchyByLabelSubsetCheck = false;

    @CommandLine.Option(names = {"--derivePropertyHierarchyByRelationshipSubsetCheck"},
            description = """
                    Indicates whether the RDF property hierarchy should be derived.
                    For this, the node-node combinations for each relationship type are initially collected in a set.
                    Subsequently, for every pair of sets, it is examined whether they are a subset of each other.
                    """)
    private Boolean derivePropertyHierarchyByRelationshipSubsetCheck = false;


    @CommandLine.Option(names = {"--schemaOutputPath"},
            description = """
                    If the RDF schema is derived from the Neo4j dataset, e.g., the class or property hierarchy, an additional path can be specified to store it separately on disk.
                    If the value is null or left out, the derived schema is stored along with the data.
                    """)
    private File schemaOutputPath = null;

    private ConversionConfig config = null;


    protected ConversionConfig getConversionConfig() {
        if (config != null) {
            return config;
        }
        if (conversionConfigFile != null) {
            config = ConversionConfig.read(conversionConfigFile);
        } else {
            ConversionConfigBuilder builder = ConversionConfigBuilder.newBuilder();
            builder.setBasePrefix(basePrefix);
            builder.setReificationVocabulary(reificationVocabulary);
            builder.setReifyOnlyRelationshipsWithProperties(reifyOnlyRelationshipsWithProperties);
            builder.setReifyRelationships(reifyRelationships);
            builder.setRelationshipTypeReificationBlacklist(relationshipTypeReificationBlacklist);
            builder.setSequenceConversionType(sequenceConversionType);
            builder.setDerivePropertyHierarchyByRelationshipSubsetCheck(derivePropertyHierarchyByRelationshipSubsetCheck);
            builder.setDeriveClassHierarchyByLabelSubsetCheck(deriveClassHierarchyByLabelSubsetCheck);
            builder.setSchemaOutputPath(schemaOutputPath);
            config = builder.build();
        }
        return config;
    }

    protected Neo4jDBServerConnector getNeo4jDBConnector() {
        return new Neo4jDBServerConnector(neo4jURI, neo4jUser, neo4jPassword, neo4jDatabase);
    }

    public String getNeo4jDatabase() {
        return neo4jDatabase;
    }
}
