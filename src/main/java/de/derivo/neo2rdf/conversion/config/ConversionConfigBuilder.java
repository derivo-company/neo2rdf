package de.derivo.neo2rdf.conversion.config;

/*-
 * #%L
 * neo2rdf
 * %%
 * Copyright (C) 2026 Derivo Company
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import de.derivo.neo2rdf.util.ReificationVocabulary;
import de.derivo.neo2rdf.util.SequenceConversionType;
import de.derivo.neo2rdf.util.VectorConversionType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConversionConfigBuilder {
    private String basePrefix = "https://www.example.org#";
    private ReificationVocabulary reificationVocabulary = ReificationVocabulary.OWL_REIFICATION;
    SequenceConversionType sequenceConversionType = SequenceConversionType.RDF_COLLECTION;
    VectorConversionType vectorConversionType = VectorConversionType.COMMA_SEPARATED_STRING;
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

    public ConversionConfigBuilder setVectorConversionType(VectorConversionType vectorConversionType) {
        this.vectorConversionType = vectorConversionType;
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
        config.vectorConversionType = vectorConversionType;
        config.deriveClassHierarchyByLabelSubsetCheck = deriveClassHierarchyByLabelSubsetCheck;
        config.derivePropertyHierarchyByRelationshipSubsetCheck = derivePropertyHierarchyByRelationshipSubsetCheck;
        config.schemaOutputPath = schemaOutputPath;
        config.reifyRelationships = reifyRelationships;
        config.relationshipTypeReificationBlacklist = relationshipTypeReificationBlacklist;
        return config;
    }


}
