package de.derivo.neo2rdf.conversion.model;

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
