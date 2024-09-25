package de.derivo.neo2rdf;

import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.config.ConversionConfigBuilder;
import de.derivo.neo2rdf.store.RDFStoreTestExtension;
import de.derivo.neo2rdf.util.ReificationVocabulary;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

public class Neo4jToRDFReificationTests {

    @RegisterExtension
    public static final RDFStoreTestExtension storeTestExtension = new RDFStoreTestExtension(TestUtil.getResource(
            "neo4j-multi-relationships"));


    @Test
    void testRDFReification() {
        ConversionConfig config = new ConversionConfigBuilder().setReificationVocabulary(ReificationVocabulary.RDF_REIFICATION).build();
        storeTestExtension.convertAndImportIntoStore("multi-relationship-reified-with-rdf.ttl", config);
        try (TupleQueryResult bindingSets = storeTestExtension.executeQuery("""
                PREFIX rdf:        <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                PREFIX rdfs:       <http://www.w3.org/2000/01/rdf-schema#>
                SELECT ?statement ?s ?p ?o WHERE {
                    ?statement rdf:subject ?s ; rdf:predicate ?p ; rdf:object ?o .
                }
                """)) {
            Assertions.assertEquals(12, bindingSets.stream().count());
        }
    }

    @Test
    void testOWLReification() {
        ConversionConfig config = new ConversionConfigBuilder().setReificationVocabulary(ReificationVocabulary.OWL_REIFICATION).build();
        storeTestExtension.convertAndImportIntoStore("multi-relationship-reified-with-owl.ttl", config);
        try (TupleQueryResult bindingSets = storeTestExtension.executeQuery("""
                PREFIX owl:        <http://www.w3.org/2002/07/owl#>
                SELECT ?statement ?s ?p ?o WHERE {
                    ?statement owl:annotatedSource ?s ; owl:annotatedProperty ?p ; owl:annotatedTarget ?o .
                }
                """)) {
            Assertions.assertEquals(12, bindingSets.stream().count());
        }
    }

    @Test
    void testReificationOnlyOfRelationshipsWithNeo4jProperties() {
        ConversionConfig config = new ConversionConfigBuilder().setReificationVocabulary(ReificationVocabulary.OWL_REIFICATION)
                .setReifyOnlyRelationshipsWithProperties(true)
                .build();
        storeTestExtension.convertAndImportIntoStore("reify-only-relationships-with-properties.ttl", config);
        try (TupleQueryResult bindingSets = storeTestExtension.executeQuery("""
                PREFIX owl:        <http://www.w3.org/2002/07/owl#>
                SELECT ?statement ?s ?p ?o WHERE {
                    ?statement owl:annotatedSource ?s ; owl:annotatedProperty ?p ; owl:annotatedTarget ?o .
                }
                """)) {
            Assertions.assertEquals(6, bindingSets.stream().count());
        }
    }

    @Test
    void testDiscardReification() {
        ConversionConfig config = new ConversionConfigBuilder().setReificationVocabulary(ReificationVocabulary.OWL_REIFICATION)
                .setBasePrefix("https://www.example.org#")
                .setReifyRelationships(false)
                .build();
        storeTestExtension.convertAndImportIntoStore("do-not-reify-relationships.ttl", config);
        try (TupleQueryResult bindingSets = storeTestExtension.executeQuery("""
                PREFIX owl:        <http://www.w3.org/2002/07/owl#>
                PREFIX :        <https://www.example.org#>
                SELECT ?statement ?s ?p ?o WHERE {
                    ?statement owl:annotatedSource ?s ; owl:annotatedProperty ?p ; owl:annotatedTarget ?o .
                }
                """)) {
            Assertions.assertEquals(0, bindingSets.stream().count());
        }

        try (TupleQueryResult bindingSets = storeTestExtension.executeQuery("""
                PREFIX owl:        <http://www.w3.org/2002/07/owl#>
                PREFIX :        <https://www.example.org#>
                SELECT ?s ?p ?o WHERE {
                    ?s :WATCHED ?o .
                }
                """)) {
            List<BindingSet> results = bindingSets.stream().toList();
            Assertions.assertEquals(7, results.size());
        }
    }
}
