package de.derivo.neo4jconverter.rdf;

import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import de.derivo.neo4jconverter.rdf.config.ConversionConfigBuilder;
import de.derivo.neo4jconverter.util.ReificationVocabulary;
import de.derivo.neo4jconverter.util.SequenceConversionType;
import picocli.CommandLine;

import java.io.File;

public class Neo4jToRDFConversionCLApp {

    @CommandLine.Option(names = {"-db", "--neo4jDBDirectory"}, required = true)
    protected File neo4jDBDirectory;

    @CommandLine.Option(names = {"-cfg", "--config"})
    private File conversionConfigFile;

    @CommandLine.Option(names = {"--basePrefix"},
            description = """
                    Prefix that is used for all converted nodes, property keys, relationship types, and relationships.
                    """)
    private String basePrefix;
    @CommandLine.Option(names = {"--reificationVocabulary"},
            description = """
                    The reification vocabulary defines how a quadruple (sbj, pred, obj, statementID) should be reified in RDF.
                    Options:
                     - RDF_COLLECTION: Neo4j sequences are converted into open lists in RDF.
                     - SEPARATE_LITERALS: Neo4j sequences are converted into separate assertions, e.g., (x { has: [1, 2] }) is converted to (:x, :has, 1) and (:x, :has, 2).
                    """)
    private ReificationVocabulary reificationVocabulary = ReificationVocabulary.OWL_REIFICATION;
    @CommandLine.Option(names = {"--sequenceConversionType"},
            description = """
                    Options:
                    - RDF_COLLECTION: Neo4j sequences are converted into open lists in RDF.
                    - SEPARATE_LITERALS: Neo4j sequences are converted into separate assertions, e.g., (x { has: [1, 2] }) is converted to (:x, :has, 1) and (:x, :has, 2).
                    """)
    private SequenceConversionType sequenceConversionType = SequenceConversionType.RDF_COLLECTION;
    @CommandLine.Option(names = {"--includeDeletedNeo4jLabels"})
    private boolean includeDeletedNeo4jLabels = false;
    @CommandLine.Option(names = {"--includeDeletedPropertyKeys"})
    private boolean includeDeletedPropertyKeys = false;
    @CommandLine.Option(names = {"--includeDeletedRelationshipTypes"})
    private boolean includeDeletedRelationshipTypes = false;
    @CommandLine.Option(names = {"--deriveClassHierarchyByLabelSubsetCheck"},
            description = """
                    Indicates whether the RDF class hierarchy should be derived.
                    For this purpose, it is examined which sets of Neo4j nodes with an assigned label are a subset of one another.
                    """)
    private boolean deriveClassHierarchyByLabelSubsetCheck = false;
    @CommandLine.Option(names = {"--derivePropertyHierarchyByRelationshipSubsetCheck"},
            description = """
                    Indicates whether the RDF property hierarchy should be derived.
                    For this, the node-node combinations for each relationship type are initially collected in a set.
                    Subsequently, for every pair of sets, it is examined whether they are a subset of each other.
                    """)
    private boolean derivePropertyHierarchyByRelationshipSubsetCheck = false;
    @CommandLine.Option(names = {"--schemaOutputPath"},
            description = """
                    If the RDF schema is derived from the Neo4j dataset, e.g., the class or property hierarchy,
                    an additional path can be specified to store it separately on disk.
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
            config = ConversionConfigBuilder.newBuilder()
                    .setBasePrefix(basePrefix)
                    .setReificationVocabulary(reificationVocabulary)
                    .setSequenceConversionType(sequenceConversionType)
                    .setIncludeDeletedNeo4jLabels(includeDeletedNeo4jLabels)
                    .setIncludeDeletedPropertyKeys(includeDeletedPropertyKeys)
                    .setIncludeDeletedRelationshipTypes(includeDeletedRelationshipTypes)
                    .setDerivePropertyHierarchyByRelationshipSubsetCheck(derivePropertyHierarchyByRelationshipSubsetCheck)
                    .setDeriveClassHierarchyByLabelSubsetCheck(deriveClassHierarchyByLabelSubsetCheck)
                    .setSchemaOutputPath(schemaOutputPath)
                    .build();
        }
        return config;
    }
}
