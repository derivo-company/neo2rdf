package de.derivo.neo2rdf.conversion.model;

import com.google.common.collect.Streams;
import de.derivo.neo2rdf.conversion.Neo4jRDFSchema;
import de.derivo.neo2rdf.schema.IndexedNeo4jSchema;
import de.derivo.neo2rdf.util.ReificationVocabulary;
import de.derivo.neo2rdf.util.SequenceConversionType;
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

    private final ListBNodeFactory listBNodeFactory = new ListBNodeFactory();

    private final AtomicLong pointIDCounter = new AtomicLong(1);

    private Namespace baseNamespace;
    private final String baseURI;
    private final String nodePrefix;
    private final String classPrefix;
    private final String relationshipPrefix;
    private final String pointPrefix;
    private final String listBNodePrefix;
    private final IndexedNeo4jSchema schema;

    private Map<Long, IRI> relationshipTypeIDToIRI;
    private Map<Long, IRI> labelIDToIRI;
    private Map<Long, IRI> propertyKeyIDToIRI;
    private final boolean reifyRelationships;
    private final ReificationVocabulary reificationVocabulary;

    private final ValueFactory valueFactory = new Neo4jToRDFValueFactory();

    public Neo4jToRDFMapper(String baseURI,
                            String nodePrefix,
                            String classPrefix,
                            String relationshipPrefix,
                            String pointPrefix,
                            String listBNodePrefix,
                            IndexedNeo4jSchema schema,
                            boolean reifyRelationships,
                            ReificationVocabulary reificationVocabulary) {
        this.baseURI = baseURI;
        this.nodePrefix = nodePrefix;
        this.classPrefix = classPrefix;
        this.relationshipPrefix = relationshipPrefix;
        this.pointPrefix = pointPrefix;
        this.listBNodePrefix = listBNodePrefix;
        this.schema = schema;
        this.reifyRelationships = reifyRelationships;
        this.reificationVocabulary = reificationVocabulary;
        init();
    }

    private void init() {
        this.baseNamespace = Values.namespace("", baseURI);


        this.relationshipTypeIDToIRI = new HashMap<>(schema.getRelationshipTypeIDToStr().size());
        schema.getRelationshipTypeIDToStr()
                .forEach((relationshipTypeID, str) -> this.relationshipTypeIDToIRI.put(relationshipTypeID,
                        Values.iri(baseNamespace, uriEncode(str))));

        this.propertyKeyIDToIRI = new HashMap<>(schema.getPropertyKeyIDToStr().size());
        schema.getPropertyKeyIDToStr()
                .forEach((propertyKeyID, str) -> this.propertyKeyIDToIRI.put(propertyKeyID, Values.iri(baseNamespace, uriEncode(str))));

        labelIDToIRI = new HashMap<>(schema.getLabelIDToStr().size());
        schema.getLabelIDToStr().forEach((labelID, str) -> labelIDToIRI.put(labelID, Values.iri(baseNamespace, uriEncode(str))));
    }

    private String uriEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public IRI propertyKeyIDToResource(long propertyKeyID) {
        return this.propertyKeyIDToIRI.get(propertyKeyID);
    }

    public IRI relationshipTypeIDToIRI(long relationshipTypeID) {
        return this.relationshipTypeIDToIRI.get(relationshipTypeID);
    }

    public Resource labelIDToResource(long labelID) {
        return this.labelIDToIRI.get(labelID);
    }

    public Resource nodeIDToResource(long nodeID) {
        return Values.iri(baseNamespace, nodePrefix + nodeID);
    }

    public Resource relationshipIDToResource(long relationshipID) {
        return Values.iri(baseNamespace, relationshipPrefix + relationshipID);
    }


    private String getListHeadBlankNodeID() {
        return listBNodePrefix + listIDCounter.getAndIncrement();
    }

    public void sequenceValueToRDF(Resource resource,
                                   long propertyKeyID,
                                   SequenceValue sequenceValue,
                                   Consumer<Statement> rdfListStatementConsumer,
                                   SequenceConversionType sequenceConversionType) {
        switch (sequenceConversionType) {
            case RDF_COLLECTION -> sequenceValueToRDFListStatements(resource, propertyKeyID, sequenceValue, rdfListStatementConsumer);
            case SEPARATE_LITERALS -> sequenceValue.iterator().forEachRemaining(val -> {
                Statement statement = valueFactory.createStatement(
                        resource,
                        propertyKeyIDToResource(propertyKeyID),
                        Values.literal(valueFactory, ((Value) val).asObject(), true)
                );
                rdfListStatementConsumer.accept(statement);
            });
        }
    }

    private void sequenceValueToRDFListStatements(Resource resource,
                                                  long propertyKeyID,
                                                  SequenceValue sequenceValue,
                                                  Consumer<Statement> rdfListStatementConsumer) {
        String listBNodeID = getListHeadBlankNodeID();
        listBNodeFactory.setCurrentListHeadID(listBNodeID);
        listBNodeFactory.getCurrentListElementID().set(0);

        BNode listHeadBNode = Values.bnode(listBNodeID);
        Statement hasListStatement = valueFactory.createStatement(resource, propertyKeyIDToResource(propertyKeyID), listHeadBNode);
        rdfListStatementConsumer.accept(hasListStatement);

        Iterable<?> list = Streams.stream(sequenceValue.iterator())
                .map(anyValue -> ((Value) anyValue).asObject())
                .collect(Collectors.toList());

        List<Statement> listStatements = new ArrayList<>();
        RDFCollections.asRDF(list, listHeadBNode, listStatements, listBNodeFactory).forEach(rdfListStatementConsumer);
    }

    public void pointPropertyToRDFStatements(Resource resource,
                                             long propertyKeyID,
                                             PointValue value,
                                             Consumer<Statement> statementConsumer) {
        Resource pointIRI = Values.iri(baseNamespace, pointPrefix + pointIDCounter.getAndIncrement());
        Statement statement = valueFactory.createStatement(resource, propertyKeyIDToResource(propertyKeyID), pointIRI);
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
                            valueFactory.createLiteral(x)));
                    statementConsumer.accept(valueFactory.createStatement(pointIRI,
                            Neo4jRDFSchema.latitudePropertyKey,
                            valueFactory.createLiteral(y)));
                }
                break;
                case 3: {
                    pointType = Neo4jRDFSchema.geographicPoint3DClass;
                    double x = coordinates[0];
                    double y = coordinates[1];
                    double z = coordinates[2];
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
                break;
                default:
                    throw new IllegalArgumentException("Number of dimensions for point not supported: " +
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
                            valueFactory.createLiteral(x)));
                    statementConsumer.accept(valueFactory.createStatement(pointIRI,
                            Neo4jRDFSchema.yPropertyKey,
                            valueFactory.createLiteral(y)));
                }
                break;
                case 3: {
                    pointType = Neo4jRDFSchema.cartesianPoint3D;
                    double x = coordinates[0];
                    double y = coordinates[1];
                    double z = coordinates[2];
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
                break;
                default:
                    throw new IllegalArgumentException("Number of dimensions for point not supported: " +
                            referenceSystem.getDimension() +
                            ", " +
                            value);
            }
        }
        statement = valueFactory.createStatement(pointIRI, RDF.TYPE, pointType);
        statementConsumer.accept(statement);
    }

    public void statementToReificationTriples(Statement statement, Consumer<Statement> consumer) {
        if (!reifyRelationships) {
            return;
        }
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
