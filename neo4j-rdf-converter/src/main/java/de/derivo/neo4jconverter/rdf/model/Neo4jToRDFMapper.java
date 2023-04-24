package de.derivo.neo4jconverter.rdf.model;

import com.google.common.collect.Streams;
import de.derivo.neo4jconverter.rdf.Neo4jRDFSchema;
import de.derivo.neo4jconverter.schema.IndexedNeo4jSchema;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.neo4j.values.SequenceValue;
import org.neo4j.values.storable.CoordinateReferenceSystem;
import org.neo4j.values.storable.PointValue;
import org.neo4j.values.storable.Value;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class Neo4jToRDFMapper {
    private final AtomicLong listIDCounter = new AtomicLong(1);
    private final AtomicLong pointIDCounter = new AtomicLong(1);

    private Namespace baseNamespace;
    private final String baseURI;
    private final String nodePrefix;
    private final String classPrefix;
    private final String relationshipPrefix;
    private final String pointPrefix;
    private final String listBNodePrefix;
    private final IndexedNeo4jSchema schema;

    private Map<Long, IRI> relationshipIDToIRI;
    private Map<Long, IRI> propertyKeyIDToIRI;

    private final ValueFactory valueFactory = Values.getValueFactory();

    public Neo4jToRDFMapper(String baseURI,
                            String nodePrefix,
                            String classPrefix,
                            String relationshipPrefix,
                            String pointPrefix,
                            String listBNodePrefix,
                            IndexedNeo4jSchema schema) {
        this.baseURI = baseURI;
        this.nodePrefix = nodePrefix;
        this.classPrefix = classPrefix;
        this.relationshipPrefix = relationshipPrefix;
        this.pointPrefix = pointPrefix;
        this.listBNodePrefix = listBNodePrefix;
        this.schema = schema;
        init();
    }

    private void init() {
        this.baseNamespace = Values.namespace("", baseURI);


        this.relationshipIDToIRI = new HashMap<>(schema.getRelationshipTypeIDToStr().size());
        schema.getRelationshipTypeIDToStr().forEach((relationshipTypeID, str) -> {
            this.relationshipIDToIRI.put(relationshipTypeID, Values.iri(baseNamespace, uriEncode(str)));
        });

        this.propertyKeyIDToIRI = new HashMap<>(schema.getPropertyKeyIDToStr().size());
        schema.getPropertyKeyIDToStr().forEach((propertyKeyID, str) -> {
            this.propertyKeyIDToIRI.put(propertyKeyID, Values.iri(baseNamespace, uriEncode(str)));
        });
    }

    private String uriEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public IRI propertyKeyIDToResource(long propertyKeyID) {
        return this.propertyKeyIDToIRI.get(propertyKeyID);
    }

    public IRI relationshipTypeIDToIRI(long relationshipTypeID) {
        return this.relationshipIDToIRI.get(relationshipTypeID);
    }

    public Resource labelIDToResource(long labelID) {
        return Values.iri(baseNamespace, classPrefix + labelID);
    }

    public Resource nodeIDToResource(long nodeID) {
        return Values.iri(baseNamespace, nodePrefix + nodeID);
    }

    public Resource relationshipIDToResource(long relationshipID) {
        return Values.iri(baseNamespace, relationshipPrefix + relationshipID);
    }


    public void sequenceValueToRDFListStatements(Resource resource,
                                                 long propertyKeyID,
                                                 SequenceValue sequenceValue,
                                                 Consumer<Statement> rdfListStatementConsumer) {
        BNode listHeadBNode = Values.bnode(listBNodePrefix + listIDCounter.getAndIncrement());
        Statement hasListStatement = Values.getValueFactory().createStatement(
                resource,
                propertyKeyIDToResource(propertyKeyID),
                listHeadBNode
        );
        rdfListStatementConsumer.accept(hasListStatement);

        Iterable<?> list = Streams.stream(sequenceValue.iterator())
                .map(anyValue -> ((Value) anyValue).asObject())
                .collect(Collectors.toList());
        List<Statement> listStatements = new ArrayList<>();
        RDFCollections.asRDF(
                list,
                listHeadBNode,
                listStatements
        ).forEach(rdfListStatementConsumer);
    }

    public void pointPropertyToRDFStatements(Resource resource,
                                             long propertyKeyID,
                                             PointValue value,
                                             Consumer<Statement> statementConsumer) {
        Resource pointIRI = Values.iri(baseNamespace, pointPrefix + pointIDCounter.getAndIncrement());
        Statement statement = valueFactory.createStatement(
                resource,
                propertyKeyIDToResource(propertyKeyID),
                pointIRI
        );
        statementConsumer.accept(statement);

        CoordinateReferenceSystem referenceSystem = value.getCoordinateReferenceSystem();
        IRI pointType;

        double[] coordinates = value.getCoordinates().get(0).getCoordinate();
        if (referenceSystem.isGeographic()) {
            switch (referenceSystem.getDimension()) {
                case 2: {
                    pointType = Neo4jRDFSchema.geographicPoint2DClass;
                    double x = coordinates[0];
                    double y = coordinates[1];
                    statementConsumer.accept(valueFactory.createStatement(pointIRI,
                            Neo4jRDFSchema.longitudePropertyKey,
                            Values.literal(x)));
                    statementConsumer.accept(valueFactory.createStatement(pointIRI,
                            Neo4jRDFSchema.latitudePropertyKey,
                            Values.literal(y)));
                }
                break;
                case 3: {
                    pointType = Neo4jRDFSchema.geographicPoint3DClass;
                    double x = coordinates[0];
                    double y = coordinates[1];
                    double z = coordinates[2];
                    statementConsumer.accept(valueFactory.createStatement(pointIRI,
                            Neo4jRDFSchema.longitudePropertyKey,
                            Values.literal(x)));
                    statementConsumer.accept(valueFactory.createStatement(pointIRI,
                            Neo4jRDFSchema.latitudePropertyKey,
                            Values.literal(y)));
                    statementConsumer.accept(valueFactory.createStatement(pointIRI,
                            Neo4jRDFSchema.heightPropertyKey,
                            Values.literal(z)));
                }
                break;
                default:
                    throw new IllegalArgumentException(
                            "Number of dimensions for point not supported: " +
                                    referenceSystem.getDimension() +
                                    ", " +
                                    value);
            }

        } else {
            switch (referenceSystem.getDimension()) {
                case 2: {
                    pointType = Neo4jRDFSchema.cartesianPoint2D;
                    double x = coordinates[0];
                    double y = coordinates[1];
                    statementConsumer.accept(valueFactory.createStatement(pointIRI,
                            Neo4jRDFSchema.xPropertyKey,
                            Values.literal(x)));
                    statementConsumer.accept(valueFactory.createStatement(pointIRI,
                            Neo4jRDFSchema.yPropertyKey,
                            Values.literal(y)));
                }
                break;
                case 3: {
                    pointType = Neo4jRDFSchema.cartesianPoint3D;
                    double x = coordinates[0];
                    double y = coordinates[1];
                    double z = coordinates[2];
                    statementConsumer.accept(valueFactory.createStatement(pointIRI,
                            Neo4jRDFSchema.xPropertyKey,
                            Values.literal(x)));
                    statementConsumer.accept(valueFactory.createStatement(pointIRI,
                            Neo4jRDFSchema.yPropertyKey,
                            Values.literal(y)));
                    statementConsumer.accept(valueFactory.createStatement(pointIRI,
                            Neo4jRDFSchema.zPropertyKey,
                            Values.literal(z)));
                }
                break;
                default:
                    throw new IllegalArgumentException(
                            "Number of dimensions for point not supported: " +
                                    referenceSystem.getDimension() +
                                    ", " +
                                    value);
            }
        }
        statement = valueFactory.createStatement(pointIRI, RDF.TYPE, pointType);
        statementConsumer.accept(statement);
    }

    public void statementToReificationTriples(Statement statement, Consumer<Statement> consumer) {
        Statement toProcess = valueFactory.createStatement(
                statement.getContext(),
                RDF.TYPE,
                RDF.STATEMENT
        );
        consumer.accept(toProcess);

        toProcess = valueFactory.createStatement(
                statement.getContext(),
                RDF.SUBJECT,
                statement.getSubject()
        );
        consumer.accept(toProcess);

        toProcess = valueFactory.createStatement(
                statement.getContext(),
                RDF.PREDICATE,
                statement.getPredicate()
        );
        consumer.accept(toProcess);

        toProcess = valueFactory.createStatement(
                statement.getContext(),
                RDF.OBJECT,
                statement.getObject()
        );
        consumer.accept(toProcess);
    }

    public Namespace getBaseNamespace() {
        return baseNamespace;
    }
}
