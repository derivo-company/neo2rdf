package de.derivo.neo2rdf.conversion.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.derivo.neo2rdf.util.ReificationVocabulary;
import de.derivo.neo2rdf.util.SequenceConversionType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ConversionConfig {
    String basePrefix = "https://www.example.org/";
    ReificationVocabulary reificationVocabulary = ReificationVocabulary.OWL_REIFICATION;
    boolean reifyOnlyRelationshipsWithProperties = false;
    SequenceConversionType sequenceConversionType = SequenceConversionType.RDF_COLLECTION;
    boolean includeDeletedNeo4jLabels = false;
    boolean includeDeletedPropertyKeys = false;
    boolean includeDeletedRelationshipTypes = false;
    boolean deriveClassHierarchyByLabelSubsetCheck = false;
    boolean derivePropertyHierarchyByRelationshipSubsetCheck = false;
    File schemaOutputPath = null;

    ConversionConfig() {
    }

    public ReificationVocabulary getReificationVocabulary() {
        return reificationVocabulary;
    }

    public boolean isIncludeDeletedNeo4jLabels() {
        return includeDeletedNeo4jLabels;
    }

    public boolean isIncludeDeletedPropertyKeys() {
        return includeDeletedPropertyKeys;
    }

    public boolean isIncludeDeletedRelationshipTypes() {
        return includeDeletedRelationshipTypes;
    }

    public boolean isDeriveClassHierarchyByLabelSubsetCheck() {
        return deriveClassHierarchyByLabelSubsetCheck;
    }

    public boolean isDerivePropertyHierarchyByRelationshipSubsetCheck() {
        return derivePropertyHierarchyByRelationshipSubsetCheck;
    }

    public String getBasePrefix() {
        return basePrefix;
    }

    public File getSchemaOutputPath() {
        return schemaOutputPath;
    }

    public boolean isReifyOnlyRelationshipsWithProperties() {
        return reifyOnlyRelationshipsWithProperties;
    }

    public SequenceConversionType getSequenceConversionType() {
        return sequenceConversionType;
    }

    public void write(File outputPath) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            FileOutputStream fos = new FileOutputStream(outputPath);
            mapper.writeValue(fos, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ConversionConfig read(File yamlPath) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            return mapper.readValue(yamlPath, ConversionConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
