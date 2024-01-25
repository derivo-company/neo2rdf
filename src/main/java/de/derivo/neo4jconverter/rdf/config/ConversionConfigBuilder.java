package de.derivo.neo4jconverter.rdf.config;

import de.derivo.neo4jconverter.util.ReificationVocabulary;
import de.derivo.neo4jconverter.util.SequenceConversionType;

import java.io.File;

public class ConversionConfigBuilder {
    private String basePrefix = "https://www.example.org#";
    private ReificationVocabulary reificationVocabulary = ReificationVocabulary.OWL_REIFICATION;
    SequenceConversionType sequenceConversionType = SequenceConversionType.RDF_COLLECTION;
    private boolean includeDeletedNeo4jLabels = false;
    private boolean includeDeletedPropertyKeys = false;
    private boolean includeDeletedRelationshipTypes = false;
    private boolean deriveClassHierarchyByLabelSubsetCheck = false;

    private boolean derivePropertyHierarchyByRelationshipSubsetCheck = false;
    private File schemaOutputPath = null;

    public ConversionConfigBuilder() {
    }

    public static ConversionConfigBuilder newBuilder() {
        return new ConversionConfigBuilder();
    }

    public ConversionConfigBuilder setReificationVocabulary(ReificationVocabulary reificationVocabulary) {
        this.reificationVocabulary = reificationVocabulary;
        return this;
    }

    public ConversionConfigBuilder setDeriveClassHierarchyByLabelSubsetCheck(boolean deriveClassHierarchyByLabelSubsetCheck) {
        this.deriveClassHierarchyByLabelSubsetCheck = deriveClassHierarchyByLabelSubsetCheck;
        return this;
    }

    public ConversionConfigBuilder setDerivePropertyHierarchyByRelationshipSubsetCheck(boolean derivePropertyHierarchyByRelationshipSubsetCheck) {
        this.derivePropertyHierarchyByRelationshipSubsetCheck = derivePropertyHierarchyByRelationshipSubsetCheck;
        return this;
    }

    public ConversionConfigBuilder setIncludeDeletedNeo4jLabels(boolean includeDeletedNeo4jLabels) {
        this.includeDeletedNeo4jLabels = includeDeletedNeo4jLabels;
        return this;
    }

    public ConversionConfigBuilder setIncludeDeletedPropertyKeys(boolean includeDeletedPropertyKeys) {
        this.includeDeletedPropertyKeys = includeDeletedPropertyKeys;
        return this;
    }

    public ConversionConfigBuilder setIncludeDeletedRelationshipTypes(boolean includeDeletedRelationshipTypes) {
        this.includeDeletedRelationshipTypes = includeDeletedRelationshipTypes;
        return this;
    }

    public ConversionConfigBuilder setBasePrefix(String basePrefix) {
        this.basePrefix = basePrefix;
        return this;
    }

    public ConversionConfigBuilder setSequenceConversionType(SequenceConversionType sequenceConversionType) {
        this.sequenceConversionType = sequenceConversionType;
        return this;
    }

    public ConversionConfigBuilder setSchemaOutputPath(File schemaOutputPath) {
        this.schemaOutputPath = schemaOutputPath;
        return this;
    }

    public ConversionConfig build() {
        ConversionConfig config = new ConversionConfig();
        config.reificationVocabulary = reificationVocabulary;
        config.includeDeletedPropertyKeys = includeDeletedPropertyKeys;
        config.includeDeletedNeo4jLabels = includeDeletedNeo4jLabels;
        config.includeDeletedRelationshipTypes = includeDeletedRelationshipTypes;
        config.basePrefix = basePrefix;
        config.sequenceConversionType = sequenceConversionType;
        config.deriveClassHierarchyByLabelSubsetCheck = deriveClassHierarchyByLabelSubsetCheck;
        config.derivePropertyHierarchyByRelationshipSubsetCheck = derivePropertyHierarchyByRelationshipSubsetCheck;
        config.schemaOutputPath = schemaOutputPath;
        return config;
    }
}
