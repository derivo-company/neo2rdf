package de.derivo.neo2rdf.conversion.model;

import com.google.common.collect.Streams;
import de.derivo.neo2rdf.conversion.Neo4jRDFSchema;
import de.derivo.neo2rdf.util.Neo4jPoint;
import de.derivo.neo2rdf.util.ReificationVocabulary;
import de.derivo.neo2rdf.util.SequenceConversionType;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.neo4j.driver.types.Point;

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

    private final ListBNodeFactory listBNodeFactory = new ListBNodeFactory();

    private final AtomicLong pointIDCounter = new AtomicLong(1);

    private Namespace baseNamespace;
    private final String baseURI;
    private final String nodePrefix;
    private final String classPrefix;
    private final String relationshipPrefix;
    private final String pointPrefix;
    private final String listBNodePrefix;

    private Map<String, IRI> relationshipTypeToIRI;
    private Map<String, IRI> labelToIRI;
    private Map<String, IRI> propertyKeyToIRI;
    private final ReificationVocabulary reificationVocabulary;

    private final ValueFactory valueFactory = new Neo4jToRDFValueFactory();

    public Neo4jToRDFMapper(String baseURI,
                            String nodePrefix,
                            String classPrefix,
                            String relationshipPrefix,
                            String pointPrefix,
                            String listBNodePrefix,
                            ReificationVocabulary reificationVocabulary) {
        this.baseURI = baseURI;
        this.nodePrefix = nodePrefix;
        this.classPrefix = classPrefix;
        this.relationshipPrefix = relationshipPrefix;
        this.pointPrefix = pointPrefix;
        this.listBNodePrefix = listBNodePrefix;
        this.reificationVocabulary = reificationVocabulary;
        init();
    }

    private void init() {
        this.baseNamespace = Values.namespace("", baseURI);

        this.relationshipTypeToIRI = new HashMap<>(1_000);
        this.propertyKeyToIRI = new HashMap<>(1_000);
        labelToIRI = new HashMap<>(10_000);
    }

    private String uriEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public IRI propertyKeyToResource(String propertyKey) {
        return relationshipTypeToIRI.computeIfAbsent(propertyKey, v -> Values.iri(baseNamespace, uriEncode(v)));
    }

    public IRI relationshipTypeToIRI(String relationshipTypeID) {
        return relationshipTypeToIRI.computeIfAbsent(relationshipTypeID, v -> Values.iri(baseNamespace, uriEncode(v)));
    }

    public Resource labelToResource(String labelID) {
        return labelToIRI.computeIfAbsent(labelID, v -> Values.iri(baseNamespace, uriEncode(v)));
    }

    public Resource nodeIDToResource(String nodeID) {
        return Values.iri(baseNamespace, nodePrefix + normalizeNeo4jId(nodeID));
    }

    private String normalizeNeo4jId(String id) {
        return id;
    }

    public Resource relationshipIDToResource(String relationshipID) {
        return Values.iri(baseNamespace, relationshipPrefix + relationshipID);
    }


    private String getListHeadBlankNodeID() {
        return listBNodePrefix + listIDCounter.getAndIncrement();
    }

    public void sequenceValueToRDF(Resource resource,
                                   String propertyKey,
                                   List<Object> sequenceValue,
                                   Consumer<Statement> rdfListStatementConsumer,
                                   SequenceConversionType sequenceConversionType) {
        switch (sequenceConversionType) {
            case RDF_COLLECTION -> sequenceValueToRDFListStatements(resource, propertyKey, sequenceValue, rdfListStatementConsumer);
            case SEPARATE_LITERALS -> sequenceValue.iterator().forEachRemaining(obj -> {
                Statement statement = valueFactory.createStatement(
                        resource,
                        propertyKeyToResource(propertyKey),
                        Values.literal(valueFactory, obj, true)
                );
                rdfListStatementConsumer.accept(statement);
            });
        }
    }

    private void sequenceValueToRDFListStatements(Resource resource,
                                                  String propertyKey,
                                                  List<Object> sequenceValue,
                                                  Consumer<Statement> rdfListStatementConsumer) {
        String listBNodeID = getListHeadBlankNodeID();
        listBNodeFactory.setCurrentListHeadID(listBNodeID);
        listBNodeFactory.getCurrentListElementID().set(0);

        BNode listHeadBNode = Values.bnode(listBNodeID);
        Statement hasListStatement = valueFactory.createStatement(resource, propertyKeyToResource(propertyKey), listHeadBNode);
        rdfListStatementConsumer.accept(hasListStatement);
        Iterable<?> list = Streams.stream(sequenceValue.iterator())
                .collect(Collectors.toList());

        List<Statement> listStatements = new ArrayList<>();
        RDFCollections.asRDF(list, listHeadBNode, listStatements, listBNodeFactory).forEach(rdfListStatementConsumer);
    }

    public void pointPropertyToRDFStatements(Resource resource,
                                             String propertyKey,
                                             Point value,
                                             Consumer<Statement> statementConsumer) {
        Resource pointIRI = Values.iri(baseNamespace, pointPrefix + pointIDCounter.getAndIncrement());
        Statement statement = valueFactory.createStatement(resource, propertyKeyToResource(propertyKey), pointIRI);
        statementConsumer.accept(statement);
        IRI pointTypeIRI;
        Neo4jPoint pointType = Neo4jPoint.getPointType(value.srid());
        switch (pointType) {
            case POINT_2D_WGS_84 -> {
                pointTypeIRI = Neo4jRDFSchema.geographicPoint2DClass;
                double x = value.x();
                double y = value.y();
                statementConsumer.accept(valueFactory.createStatement(pointIRI,
                        Neo4jRDFSchema.longitudePropertyKey,
                        valueFactory.createLiteral(x)));
                statementConsumer.accept(valueFactory.createStatement(pointIRI,
                        Neo4jRDFSchema.latitudePropertyKey,
                        valueFactory.createLiteral(y)));
            }
            case POINT_3D_WGS_84 -> {
                pointTypeIRI = Neo4jRDFSchema.geographicPoint3DClass;
                double x = value.x();
                double y = value.y();
                double z = value.z();
                statementConsumer.accept(valueFactory.createStatement(pointIRI,
                        Neo4jRDFSchema.longitudePropertyKey,
                        valueFactory.createLiteral(x)));
                statementConsumer.accept(valueFactory.createStatement(pointIRI,
                        Neo4jRDFSchema.latitudePropertyKey,
                        valueFactory.createLiteral(y)));
                statementConsumer.accept(valueFactory.createStatement(pointIRI,
                        Neo4jRDFSchema.heightPropertyKey,
                        valueFactory.createLiteral(z)));
            }
            case POINT_2D_CARTESIAN -> {
                pointTypeIRI = Neo4jRDFSchema.cartesianPoint2D;
                double x = value.x();
                double y = value.y();
                statementConsumer.accept(valueFactory.createStatement(pointIRI,
                        Neo4jRDFSchema.xPropertyKey,
                        valueFactory.createLiteral(x)));
                statementConsumer.accept(valueFactory.createStatement(pointIRI,
                        Neo4jRDFSchema.yPropertyKey,
                        valueFactory.createLiteral(y)));
            }
            case POINT_3D_CARTESIAN -> {
                pointTypeIRI = Neo4jRDFSchema.cartesianPoint3D;
                double x = value.x();
                double y = value.y();
                double z = value.z();
                statementConsumer.accept(valueFactory.createStatement(pointIRI,
                        Neo4jRDFSchema.xPropertyKey,
                        valueFactory.createLiteral(x)));
                statementConsumer.accept(valueFactory.createStatement(pointIRI,
                        Neo4jRDFSchema.yPropertyKey,
                        valueFactory.createLiteral(y)));
                statementConsumer.accept(valueFactory.createStatement(pointIRI,
                        Neo4jRDFSchema.zPropertyKey,
                        valueFactory.createLiteral(z)));
            }
            default -> throw new IllegalArgumentException("Point type not supported: " + pointType);
        }
        statement = valueFactory.createStatement(pointIRI, RDF.TYPE, pointTypeIRI);
        statementConsumer.accept(statement);
    }

    public void statementToReificationTriples(Statement statement, Consumer<Statement> consumer) {
        Statement toProcess = valueFactory.createStatement(statement.getContext(), RDF.TYPE, reificationVocabulary.getStatementClassIRI());
        consumer.accept(toProcess);

        toProcess = valueFactory.createStatement(statement.getContext(),
                reificationVocabulary.getPropertyForReifiedSubject(),
                statement.getSubject());
        consumer.accept(toProcess);

        toProcess = valueFactory.createStatement(statement.getContext(),
                reificationVocabulary.getPropertyForReifiedPredicate(),
                statement.getPredicate());
        consumer.accept(toProcess);

        toProcess = valueFactory.createStatement(statement.getContext(),
                reificationVocabulary.getPropertyForReifiedObject(),
                statement.getObject());
        consumer.accept(toProcess);
    }

    public Namespace getBaseNamespace() {
        return baseNamespace;
    }
}
