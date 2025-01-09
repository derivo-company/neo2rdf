package de.derivo.neo2rdf;

import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.config.ConversionConfigBuilder;
import de.derivo.neo2rdf.store.RDFStoreTestExtension;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Set;

public class Neo4jDatatypeTests {

    @RegisterExtension
    public static final RDFStoreTestExtension storeTestExtension = new RDFStoreTestExtension(TestUtil.getCypherCreateQueries(
            "neo4j-datatypes.cypher"));
    private static final ConversionConfig config = ConversionConfigBuilder.newBuilder().build();

    @Test
    public void neo4jDatatypes() {
        storeTestExtension.convertAndImportIntoStore("neo4j-datatypes-test.ttl", config);

        String q = """
                PREFIX : <%s>
                SELECT ?time ?localTime ?date ?integer ?float ?double
                WHERE {
                    ?node :time ?time ;
                        :localTime ?localTime ;
                        :date ?date ;
                        :integer ?integer ;
                        :float ?float ;
                        :double ?double .
                }
                """.formatted(config.getBasePrefix());
        try (TupleQueryResult bindingSets = storeTestExtension.executeQuery(q)) {
            if (!bindingSets.hasNext()) {
                Assertions.fail("No statements could be fetched.");
            }
            for (BindingSet bindingSet : bindingSets) {
                Literal timeLit = ((Literal) bindingSet.getValue("time"));
                Literal localTimeLit = ((Literal) bindingSet.getValue("localTime"));
                Literal dateLit = ((Literal) bindingSet.getValue("date"));
                Literal integerLit = ((Literal) bindingSet.getValue("integer"));
                Literal floatLit = ((Literal) bindingSet.getValue("float"));
                Literal doubleLit = ((Literal) bindingSet.getValue("double"));

                Assertions.assertEquals(CoreDatatype.XSD.TIME, timeLit.getCoreDatatype());
                Assertions.assertEquals("12:50:35.556+01:00", timeLit.stringValue());

                Assertions.assertEquals(CoreDatatype.XSD.TIME, localTimeLit.getCoreDatatype());
                Assertions.assertEquals("12:50:35.556", localTimeLit.stringValue());

                Assertions.assertEquals(CoreDatatype.XSD.DATE, dateLit.getCoreDatatype());
                Assertions.assertEquals("1967-01-21", dateLit.stringValue());

                Assertions.assertEquals(CoreDatatype.XSD.LONG, integerLit.getCoreDatatype());
                Assertions.assertEquals("195", integerLit.stringValue());

                Assertions.assertEquals(CoreDatatype.XSD.DOUBLE, floatLit.getCoreDatatype());
                Assertions.assertEquals("4.2222222E0", floatLit.stringValue());

                Assertions.assertEquals(CoreDatatype.XSD.DOUBLE, doubleLit.getCoreDatatype());
                Assertions.assertEquals("-1.797693134862315E300", doubleLit.stringValue());
            }
        }
    }

    @Test
    public void derivedPropertyTypes() {
        storeTestExtension.convertAndImportIntoStore("neo4j-datatypes-test.ttl", config);

        Set<String> objectProperties = storeTestExtension.getInstances(OWL.OBJECTPROPERTY.toString());
        Set<String> annotationProperties = storeTestExtension.getInstances(OWL.ANNOTATIONPROPERTY.toString());
        Set<String> dataProperties = storeTestExtension.getInstances(OWL.DATATYPEPROPERTY.toString());

        String b = config.getBasePrefix();

        // data properties
        System.out.println("Data properties:");
        for (String dataProperty : dataProperties) {
            System.out.println(dataProperty);
        }
        Assertions.assertTrue(dataProperties.contains(b + "name"));
        Assertions.assertTrue(dataProperties.contains(b + "time"));
        Assertions.assertTrue(dataProperties.contains(b + "localTime"));
        Assertions.assertTrue(dataProperties.contains(b + "date"));
        Assertions.assertTrue(dataProperties.contains(b + "weekDateFormat"));
        Assertions.assertTrue(dataProperties.contains(b + "localDateTime"));
        Assertions.assertTrue(dataProperties.contains(b + "dateTime"));
        Assertions.assertTrue(dataProperties.contains(b + "integer"));
        Assertions.assertTrue(dataProperties.contains(b + "float"));
        Assertions.assertTrue(dataProperties.contains(b + "double"));
        Assertions.assertTrue(dataProperties.contains(b + "relationshipInteger"));
        Assertions.assertEquals(21, dataProperties.size());

        // object properties
        System.out.println("Object properties:");
        for (String objectProperty : objectProperties) {
            System.out.println(objectProperty);
        }
        Assertions.assertTrue(objectProperties.contains(b + "cartesian3d"));
        Assertions.assertTrue(objectProperties.contains(b + "geo3d"));
        Assertions.assertTrue(objectProperties.contains(b + "intList"));
        Assertions.assertTrue(objectProperties.contains(b + "floatList"));
        Assertions.assertTrue(objectProperties.contains(b + "RELATION"));
        Assertions.assertEquals(5, objectProperties.size());

        // annotation properties
        System.out.println("Annotation properties:");
        for (String annotationProperty : annotationProperties) {
            System.out.println(annotationProperty);
        }
        Assertions.assertTrue(annotationProperties.contains(b + "name"));
        Assertions.assertTrue(annotationProperties.contains(b + "relationshipInteger"));
        Assertions.assertEquals(2, annotationProperties.size());
    }
}
