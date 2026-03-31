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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.derivo.neo2rdf.util.ReificationVocabulary;
import de.derivo.neo2rdf.util.SequenceConversionType;
import de.derivo.neo2rdf.util.VectorConversionType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ConversionConfig {
    public List<String> relationshipTypeReificationBlacklist;
    String basePrefix = "https://www.example.org/";
    ReificationVocabulary reificationVocabulary = ReificationVocabulary.OWL_REIFICATION;
    boolean reifyOnlyRelationshipsWithProperties = false;
    SequenceConversionType sequenceConversionType = SequenceConversionType.RDF_COLLECTION;
    VectorConversionType vectorConversionType = VectorConversionType.COMMA_SEPARATED_STRING;
    boolean deriveClassHierarchyByLabelSubsetCheck = false;
    boolean derivePropertyHierarchyByRelationshipSubsetCheck = false;
    public boolean reifyRelationships;
    File schemaOutputPath = null;

    ConversionConfig() {
    }

    public ReificationVocabulary getReificationVocabulary() {
        return reificationVocabulary;
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

    public VectorConversionType getVectorConversionType() {
        return vectorConversionType;
    }

    public boolean isReifyRelationships() {
        return reifyRelationships;
    }

    public List<String> getRelationshipTypeReificationBlacklist() {
        return relationshipTypeReificationBlacklist;
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
