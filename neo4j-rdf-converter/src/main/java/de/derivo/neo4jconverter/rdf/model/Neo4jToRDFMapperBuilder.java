package de.derivo.neo4jconverter.rdf.model;

import de.derivo.neo4jconverter.schema.IndexedNeo4jSchema;

public class Neo4jToRDFMapperBuilder {

    private String baseURI;
    private String nodePrefix = "node-";
    private String classPrefix = "label-";
    private String relationshipPrefix = "rel";
    private String pointPrefix = "point";

    private String listBNodePrefix = "list";
    private final IndexedNeo4jSchema schema;

    public Neo4jToRDFMapperBuilder(String baseURI, IndexedNeo4jSchema schema) {
        this.baseURI = baseURI;
        this.schema = schema;
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

    public Neo4jToRDFMapper build() {
        return new Neo4jToRDFMapper(baseURI,
                nodePrefix,
                classPrefix,
                relationshipPrefix,
                pointPrefix,
                listBNodePrefix,
                schema);
    }
}
