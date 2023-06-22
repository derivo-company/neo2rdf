package de.derivo.neo4jconverter;

import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import de.derivo.neo4jconverter.rdf.config.ConversionConfigBuilder;
import de.derivo.neo4jconverter.store.RDFStoreTestExtension;
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
    private static final RDFStoreTestExtension storeTestExtension = new RDFStoreTestExtension("neo4j-datatypes-example");
    private static final ConversionConfig config = ConversionConfigBuilder.newBuilder().build();

    @Test
    public void testNeo4jDatatypes() {
        storeTestExtension.convertAndImportIntoStore("neo4j-datatypes-test.ttl", config);

        String q = """
                PREFIX : <%s>
                SELECT ?time ?localTime ?date ?integer ?float 
                WHERE {
                    ?node :time ?time ;
                        :localTime ?localTime ;
                        :date ?date ;
                        :integer ?integer ;
                        :float ?float .
                }
                """.formatted(config.getBasePrefix());
        try (TupleQueryResult bindingSets = storeTestExtension.executeQuery(q)) {
            for (BindingSet bindingSet : bindingSets) {
                Literal timeLit = ((Literal) bindingSet.getValue("time"));
                Literal localTimeLit = ((Literal) bindingSet.getValue("localTime"));
                Literal dateLit = ((Literal) bindingSet.getValue("date"));
                Literal integerLit = ((Literal) bindingSet.getValue("integer"));
                Literal floatLit = ((Literal) bindingSet.getValue("float"));

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
            }
        }
    }

    @Test
    public void testDerivedPropertyTypes() {
        storeTestExtension.convertAndImportIntoStore("neo4j-datatypes-test.ttl", config);

        Set<String> objectProperties = storeTestExtension.getInstances(OWL.OBJECTPROPERTY.toString());
        Set<String> annotationProperties = storeTestExtension.getInstances(OWL.ANNOTATIONPROPERTY.toString());
        Set<String> dataProperties = storeTestExtension.getInstances(OWL.DATATYPEPROPERTY.toString());

        String b = config.getBasePrefix();

        // data properties
        Assertions.assertTrue(dataProperties.contains(b + "name"));
        Assertions.assertTrue(dataProperties.contains(b + "time"));
        Assertions.assertTrue(dataProperties.contains(b + "localTime"));
        Assertions.assertTrue(dataProperties.contains(b + "date"));
        Assertions.assertTrue(dataProperties.contains(b + "weekDateFormat"));
        Assertions.assertTrue(dataProperties.contains(b + "localDateTime"));
        Assertions.assertTrue(dataProperties.contains(b + "dateTime"));
        Assertions.assertTrue(dataProperties.contains(b + "integer"));
        Assertions.assertTrue(dataProperties.contains(b + "float"));
        Assertions.assertEquals(16, dataProperties.size());

        // object properties
        Assertions.assertTrue(objectProperties.contains(b + "cartesian3d"));
        Assertions.assertTrue(objectProperties.contains(b + "geo3d"));
        Assertions.assertTrue(objectProperties.contains(b + "intList"));
        Assertions.assertTrue(objectProperties.contains(b + "floatList"));
        Assertions.assertTrue(objectProperties.contains(b + "RELATION"));
        Assertions.assertEquals(5, objectProperties.size());

        // annotation properties
        Assertions.assertTrue(annotationProperties.contains(b + "name"));
        Assertions.assertEquals(1, annotationProperties.size());
    }
}
