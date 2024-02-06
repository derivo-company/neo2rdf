package de.derivo.neo2rdf.conversion;

import de.derivo.neo2rdf.conversion.model.Neo4jToRDFValueFactory;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import java.util.*;

public class Neo4jRDFSchema {
    public static Namespace neo4jNamespace = Values.namespace("neo4j", "https://neo4j.com/");
    public static IRI pointClass = Values.iri(
            "https://neo4j.com/docs/graphql-manual/current/type-definitions/types/#type-definitions-types-point");
    public static IRI cartesianPoint = Values.iri(
            "https://neo4j.com/docs/cypher-manual/current/syntax/spatial/#cypher-spatial-crs-cartesian");
    public static IRI geographicPoint = Values.iri(
            "https://neo4j.com/docs/cypher-manual/current/syntax/spatial/#cypher-spatial-crs-geographic");
    public static IRI geographicPoint2DClass = Values.iri("https://spatialreference.org/ref/epsg/4326/");
    public static IRI geographicPoint3DClass = Values.iri("https://spatialreference.org/ref/epsg/4979/");
    public static IRI cartesianPoint2D = Values.iri("https://spatialreference.org/ref/sr-org/7203/");
    public static IRI cartesianPoint3D = Values.iri("https://spatialreference.org/ref/sr-org/9157/");
    public static IRI xPropertyKey = Values.iri(neo4jNamespace, "x-coordinate");
    public static IRI yPropertyKey = Values.iri(neo4jNamespace, "y-coordinate");
    public static IRI zPropertyKey = Values.iri(neo4jNamespace, "z-coordinate");
    public static IRI longitudePropertyKey = Values.iri(neo4jNamespace, "longitude");
    public static IRI latitudePropertyKey = Values.iri(neo4jNamespace, "latitude");
    public static IRI heightPropertyKey = Values.iri(neo4jNamespace, "height");

    public static final List<Statement> AXIOMATIC_TRIPLES;

    private static final ValueFactory valueFactory = new Neo4jToRDFValueFactory();

    static {
        List<Statement> axiomaticTriples = new ArrayList<>();
        axiomaticTriples.add(valueFactory.createStatement(pointClass, RDF.TYPE, OWL.CLASS));
        axiomaticTriples.add(valueFactory.createStatement(cartesianPoint, RDF.TYPE, OWL.CLASS));
        axiomaticTriples.add(valueFactory.createStatement(geographicPoint, RDF.TYPE, OWL.CLASS));
        axiomaticTriples.add(valueFactory.createStatement(geographicPoint2DClass, RDF.TYPE, OWL.CLASS));
        axiomaticTriples.add(valueFactory.createStatement(geographicPoint3DClass, RDF.TYPE, OWL.CLASS));
        axiomaticTriples.add(valueFactory.createStatement(cartesianPoint2D, RDF.TYPE, OWL.CLASS));
        axiomaticTriples.add(valueFactory.createStatement(cartesianPoint3D, RDF.TYPE, OWL.CLASS));

        axiomaticTriples.addAll(getStatementsForClass(pointClass, "Point", Set.of()));
        axiomaticTriples.addAll(getStatementsForClass(cartesianPoint, "Cartesian Point", Set.of(pointClass)));
        axiomaticTriples.addAll(getStatementsForClass(geographicPoint, "Geographic Point", Set.of(pointClass)));
        axiomaticTriples.addAll(getStatementsForClass(geographicPoint2DClass,
                "Geographic Point 2D",
                Set.of(geographicPoint)));
        axiomaticTriples.addAll(getStatementsForClass(geographicPoint3DClass,
                "Geographic Point 3D",
                Set.of(geographicPoint)));
        axiomaticTriples.addAll(getStatementsForClass(cartesianPoint2D, "Cartesian Point 2D", Set.of(cartesianPoint)));
        axiomaticTriples.addAll(getStatementsForClass(cartesianPoint3D, "Cartesian Point 3D", Set.of(cartesianPoint)));

        axiomaticTriples.addAll(getStatementsForProperty(xPropertyKey,
                OWL.DATATYPEPROPERTY,
                "x-coordinate",
                Set.of(),
                Set.of(pointClass),
                Set.of()));
        axiomaticTriples.addAll(getStatementsForProperty(yPropertyKey,
                OWL.DATATYPEPROPERTY,
                "y-coordinate",
                Set.of(),
                Set.of(pointClass),
                Set.of()));
        axiomaticTriples.addAll(getStatementsForProperty(zPropertyKey,
                OWL.DATATYPEPROPERTY,
                "z-coordinate",
                Set.of(),
                Set.of(pointClass),
                Set.of()));
        axiomaticTriples.addAll(getStatementsForProperty(longitudePropertyKey,
                OWL.DATATYPEPROPERTY,
                "longitude",
                Set.of(xPropertyKey),
                Set.of(geographicPoint),
                Set.of()));
        axiomaticTriples.addAll(getStatementsForProperty(latitudePropertyKey,
                OWL.DATATYPEPROPERTY,
                "latitude",
                Set.of(yPropertyKey),
                Set.of(geographicPoint),
                Set.of()));
        axiomaticTriples.addAll(getStatementsForProperty(heightPropertyKey,
                OWL.DATATYPEPROPERTY,
                "height",
                Set.of(zPropertyKey),
                Set.of(geographicPoint),
                Set.of()));

        for (IRI iri : List.of(longitudePropertyKey,
                latitudePropertyKey,
                heightPropertyKey)) {
            axiomaticTriples.add(valueFactory.createStatement(iri, RDFS.DOMAIN, geographicPoint));
        }

        for (IRI iri : List.of(xPropertyKey, yPropertyKey, zPropertyKey)) {
            axiomaticTriples.add(valueFactory.createStatement(iri, RDFS.DOMAIN, pointClass));
        }
        AXIOMATIC_TRIPLES = Collections.unmodifiableList(axiomaticTriples);
    }

    private static List<Statement> getStatementsForClass(IRI classIRI, String label, Collection<Resource> subClassOf) {
        List<Statement> statements = new ArrayList<>();
        statements.add(valueFactory.createStatement(classIRI, RDFS.LABEL, valueFactory.createLiteral(label)));
        statements.add(valueFactory.createStatement(classIRI, RDF.TYPE, OWL.CLASS));
        for (Resource superClass : subClassOf) {
            statements.add(valueFactory.createStatement(classIRI, RDFS.SUBCLASSOF, superClass));
        }
        return statements;
    }

    private static List<Statement> getStatementsForProperty(IRI propertyIRI,
                                                            Resource propertyType,
                                                            String label,
                                                            Collection<Resource> subPropertyOf,
                                                            Collection<Resource> domainClasses,
                                                            Collection<Resource> rangeClasses) {
        List<Statement> statements = new ArrayList<>();
        statements.add(valueFactory.createStatement(propertyIRI, RDFS.LABEL, valueFactory.createLiteral(label)));
        statements.add(valueFactory.createStatement(propertyIRI, RDF.TYPE, propertyType));
        for (Resource superClass : subPropertyOf) {
            statements.add(valueFactory.createStatement(propertyIRI, RDFS.SUBPROPERTYOF, superClass));
        }
        for (Resource domainClass : domainClasses) {
            statements.add(valueFactory.createStatement(propertyIRI, RDFS.DOMAIN, domainClass));
        }
        for (Resource rangeClass : rangeClasses) {
            statements.add(valueFactory.createStatement(propertyIRI, RDFS.RANGE, rangeClass));
        }
        return statements;
    }
}
