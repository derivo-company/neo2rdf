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

    @CommandLine.Option(names = {"--basePrefix"})
    private String basePrefix;
    @CommandLine.Option(names = {"--reificationVocabulary"})
    private ReificationVocabulary reificationVocabulary = ReificationVocabulary.OWL_REIFICATION;
    @CommandLine.Option(names = {"--sequenceConversionType"})
    private SequenceConversionType sequenceConversionType = SequenceConversionType.RDF_COLLECTION;
    @CommandLine.Option(names = {"--includeDeletedNeo4jLabels"})
    private boolean includeDeletedNeo4jLabels = false;
    @CommandLine.Option(names = {"--includeDeletedPropertyKeys"})
    private boolean includeDeletedPropertyKeys = false;
    @CommandLine.Option(names = {"--includeDeletedRelationshipTypes"})
    private boolean includeDeletedRelationshipTypes = false;
    @CommandLine.Option(names = {"--deriveClassHierarchyByLabelSubsetCheck"})
    private boolean deriveClassHierarchyByLabelSubsetCheck = false;
    @CommandLine.Option(names = {"--derivePropertyHierarchyByRelationshipSubsetCheck"})
    private boolean derivePropertyHierarchyByRelationshipSubsetCheck = false;
    @CommandLine.Option(names = {"--schemaOutputPath"})
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
