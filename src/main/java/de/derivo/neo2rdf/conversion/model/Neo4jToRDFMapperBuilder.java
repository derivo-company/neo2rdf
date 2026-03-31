package de.derivo.neo2rdf.conversion.model;

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

public class Neo4jToRDFMapperBuilder {

    private String baseURI;
    private String nodePrefix = "node-";
    private String classPrefix = "label-";
    private String relationshipPrefix = "rel-";
    private String pointPrefix = "point-";

    private String listBNodePrefix = "list-";
    private ReificationVocabulary reificationVocabulary = ReificationVocabulary.RDF_REIFICATION;

    public Neo4jToRDFMapperBuilder(String baseURI) {
        this.baseURI = baseURI;
    }

    public Neo4jToRDFMapperBuilder setBaseURI(String baseURI) {
        this.baseURI = baseURI;
        return this;
    }

    public Neo4jToRDFMapperBuilder setNodePrefix(String nodePrefix) {
        this.nodePrefix = nodePrefix;
        return this;
    }

    public Neo4jToRDFMapperBuilder setClassPrefix(String classPrefix) {
        this.classPrefix = classPrefix;
        return this;
    }

    public Neo4jToRDFMapperBuilder setRelationshipPrefix(String relationshipPrefix) {
        this.relationshipPrefix = relationshipPrefix;
        return this;
    }

    public Neo4jToRDFMapperBuilder setPointPrefix(String pointPrefix) {
        this.pointPrefix = pointPrefix;
        return this;
    }

    public Neo4jToRDFMapperBuilder setListBNodePrefix(String listBNodePrefix) {
        this.listBNodePrefix = listBNodePrefix;
        return this;
    }

    public void setReificationVocabulary(ReificationVocabulary reificationVocabulary) {
        this.reificationVocabulary = reificationVocabulary;
    }

    public Neo4jToRDFMapper build() {
        return new Neo4jToRDFMapper(baseURI,
                nodePrefix,
                classPrefix,
                relationshipPrefix,
                pointPrefix,
                listBNodePrefix,
                reificationVocabulary);
    }
}
