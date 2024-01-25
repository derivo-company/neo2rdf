package de.derivo.neo2rdf.conversion.cli;

import de.derivo.neo2rdf.conversion.Neo4jStoreFactory;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.config.ConversionConfigBuilder;
import de.derivo.neo2rdf.util.ReificationVocabulary;
import de.derivo.neo2rdf.util.SequenceConversionType;
import org.neo4j.kernel.impl.store.NeoStores;
import picocli.CommandLine;

import java.io.File;

public class ConversionOptions {

    @CommandLine.Option(names = {"-db", "--neo4jDBDirectory"}, required = true)
    private File neo4jDBDirectory = null;

    @CommandLine.Option(names = {"-d", "--neo4jDBDumpPath"},
            description = """
                    If a path to a Neo4j dump has been specified using this parameter, the DB dump is extracted to the appropriate target
                    Neo4j DB directory first, and subsequently, the conversion procedure gets executed as usual.
                    """)
    protected File neo4jDBDumpPath = null;


    @CommandLine.Option(names = {"-cfg", "--config"},
            description = """
                    Instead of specifying the configuration in terms of command line parameters, it is also possible to use an external YAML configuration.
                    """)
    private File conversionConfigFile = null;


    @CommandLine.Option(names = {"--basePrefix"},
            description = """
                    Prefix that is used for all converted nodes, property keys, relationship types, and relationships.
                    """)
    private String basePrefix = null;

    @CommandLine.Option(names = {"--reificationVocabulary"},
            description = """
                    The reification vocabulary defines how a quadruple (sbj, pred, obj, statementID) should be reified in RDF.
                    Options:
                     - RDF_COLLECTION: Neo4j sequences are converted into open lists in RDF.
                     - SEPARATE_LITERALS: Neo4j sequences are converted into separate assertions, e.g., (x { has: [1, 2] }) is converted to (:x, :has, 1) and (:x, :has, 2).
                    """)
    private ReificationVocabulary reificationVocabulary = null;

    @CommandLine.Option(names = {"--sequenceConversionType"},
            description = """
                    Options:
                    - RDF_COLLECTION: Neo4j sequences are converted into open lists in RDF.
                    - SEPARATE_LITERALS: Neo4j sequences are converted into separate assertions, e.g., (x { has: [1, 2] }) is converted to (:x, :has, 1) and (:x, :has, 2).
                    """)
    private SequenceConversionType sequenceConversionType = null;

    @CommandLine.Option(names = {"--includeDeletedNeo4jLabels"})
    private Boolean includeDeletedNeo4jLabels = null;

    @CommandLine.Option(names = {"--includeDeletedPropertyKeys"})
    private Boolean includeDeletedPropertyKeys = null;

    @CommandLine.Option(names = {"--includeDeletedRelationshipTypes"})
    private Boolean includeDeletedRelationshipTypes = null;

    @CommandLine.Option(names = {"--deriveClassHierarchyByLabelSubsetCheck"},
            description = """
                    Indicates whether the RDF class hierarchy should be derived.
                    For this purpose, it is examined which sets of Neo4j nodes with an assigned label are a subset of one another.
                    """)
    private Boolean deriveClassHierarchyByLabelSubsetCheck = null;

    @CommandLine.Option(names = {"--derivePropertyHierarchyByRelationshipSubsetCheck"},
            description = """
                    Indicates whether the RDF property hierarchy should be derived.
                    For this, the node-node combinations for each relationship type are initially collected in a set.
                    Subsequently, for every pair of sets, it is examined whether they are a subset of each other.
                    """)
    private Boolean derivePropertyHierarchyByRelationshipSubsetCheck = false;

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
            ConversionConfigBuilder builder = ConversionConfigBuilder.newBuilder();
            if (basePrefix != null) {
                builder.setBasePrefix(basePrefix);
            }
            if (reificationVocabulary != null) {
                builder.setReificationVocabulary(reificationVocabulary);
            }
            if (sequenceConversionType != null) {
                builder.setSequenceConversionType(sequenceConversionType);
            }
            if (includeDeletedNeo4jLabels != null) {
                builder.setIncludeDeletedNeo4jLabels(includeDeletedNeo4jLabels);
            }
            if (includeDeletedPropertyKeys != null) {
                builder.setIncludeDeletedPropertyKeys(includeDeletedPropertyKeys);
            }
            if (includeDeletedRelationshipTypes != null) {
                builder.setIncludeDeletedRelationshipTypes(includeDeletedRelationshipTypes);
            }
            if (derivePropertyHierarchyByRelationshipSubsetCheck != null) {
                builder.setDerivePropertyHierarchyByRelationshipSubsetCheck(derivePropertyHierarchyByRelationshipSubsetCheck);
            }
            if (deriveClassHierarchyByLabelSubsetCheck != null) {
                builder.setDeriveClassHierarchyByLabelSubsetCheck(deriveClassHierarchyByLabelSubsetCheck);
            }
            if (schemaOutputPath != null) {
                builder.setSchemaOutputPath(schemaOutputPath);
            }
            config = builder.build();
        }
        return config;
    }

    protected NeoStores getNeo4jStore() {
        if (neo4jDBDumpPath != null) {
            return Neo4jStoreFactory.getNeo4jStoreFromDump(neo4jDBDumpPath, getNeo4jDBDirectory());
        } else {
            return Neo4jStoreFactory.getNeo4jStore(getNeo4jDBDirectory());
        }
    }

    public File getNeo4jDBDirectory() {
        return neo4jDBDirectory;
    }
}
