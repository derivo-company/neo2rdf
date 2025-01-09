package de.derivo.neo2rdf.conversion.config;

import de.derivo.neo2rdf.util.ReificationVocabulary;
import de.derivo.neo2rdf.util.SequenceConversionType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConversionConfigBuilder {
    private String basePrefix = "https://www.example.org#";
    private ReificationVocabulary reificationVocabulary = ReificationVocabulary.OWL_REIFICATION;
    SequenceConversionType sequenceConversionType = SequenceConversionType.RDF_COLLECTION;
    private boolean deriveClassHierarchyByLabelSubsetCheck = false;
    private boolean derivePropertyHierarchyByRelationshipSubsetCheck = false;
    private boolean reifyRelationships = true;
    private boolean reifyOnlyRelationshipsWithProperties = false;
    private List<String> relationshipTypeReificationBlacklist = new ArrayList<>();
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

    public ConversionConfigBuilder setBasePrefix(String basePrefix) {
        this.basePrefix = basePrefix;
        return this;
    }

    public ConversionConfigBuilder setSequenceConversionType(SequenceConversionType sequenceConversionType) {
        this.sequenceConversionType = sequenceConversionType;
        return this;
    }

    public boolean isReifyOnlyRelationshipsWithProperties() {
        return reifyOnlyRelationshipsWithProperties;
    }

    public ConversionConfigBuilder setReifyOnlyRelationshipsWithProperties(boolean reifyOnlyRelationshipsWithProperties) {
        this.reifyOnlyRelationshipsWithProperties = reifyOnlyRelationshipsWithProperties;
        return this;
    }

    public ConversionConfigBuilder setSchemaOutputPath(File schemaOutputPath) {
        this.schemaOutputPath = schemaOutputPath;
        return this;
    }

    public boolean isReifyRelationships() {
        return reifyRelationships;
    }

    public ConversionConfigBuilder setReifyRelationships(boolean reifyRelationships) {
        this.reifyRelationships = reifyRelationships;
        return this;
    }

    public List<String> getRelationshipTypeReificationBlacklist() {
        return relationshipTypeReificationBlacklist;
    }

    public ConversionConfigBuilder setRelationshipTypeReificationBlacklist(List<String> relationshipTypeReificationBlacklist) {
        this.relationshipTypeReificationBlacklist = relationshipTypeReificationBlacklist;
        return this;
    }

    public ConversionConfig build() {
        ConversionConfig config = new ConversionConfig();
        config.reificationVocabulary = reificationVocabulary;
        config.reifyOnlyRelationshipsWithProperties = reifyOnlyRelationshipsWithProperties;
        config.basePrefix = basePrefix;
        config.sequenceConversionType = sequenceConversionType;
        config.deriveClassHierarchyByLabelSubsetCheck = deriveClassHierarchyByLabelSubsetCheck;
        config.derivePropertyHierarchyByRelationshipSubsetCheck = derivePropertyHierarchyByRelationshipSubsetCheck;
        config.schemaOutputPath = schemaOutputPath;
        config.reifyRelationships = reifyRelationships;
        config.relationshipTypeReificationBlacklist = relationshipTypeReificationBlacklist;
        return config;
    }


}
