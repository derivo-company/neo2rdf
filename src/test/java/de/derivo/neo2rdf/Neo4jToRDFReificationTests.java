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

    private static final String basePrefix = "https://www.example.org#";

    @RegisterExtension
    public static final RDFStoreTestExtension storeTestExtension = new RDFStoreTestExtension(TestUtil.getCypherQuery(
            "neo4j-multi-relationships.cypher"));


    @Test
    void testRDFReification() {
        ConversionConfig config = new ConversionConfigBuilder()
                .setBasePrefix(basePrefix)
                .setReificationVocabulary(ReificationVocabulary.RDF_REIFICATION).build();
        storeTestExtension.convertAndImportIntoStore("multi-relationship-reified-with-rdf.ttl", config);
        try (TupleQueryResult bindingSets = storeTestExtension.executeQuery("""
                PREFIX rdf:        <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                PREFIX :        <https://www.example.org#>
                SELECT ?statement WHERE {
                    ?statement rdf:subject ?s ; rdf:predicate :WATCHED ; rdf:object ?o .
                }
                """)) {
            Assertions.assertEquals(12, bindingSets.stream().count());
        }
    }

    @Test
    void testOWLReification() {
        ConversionConfig config = new ConversionConfigBuilder()
                .setBasePrefix(basePrefix)
                .setReificationVocabulary(ReificationVocabulary.OWL_REIFICATION).build();
        storeTestExtension.convertAndImportIntoStore("multi-relationship-reified-with-owl.ttl", config);
        try (TupleQueryResult bindingSets = storeTestExtension.executeQuery("""
                PREFIX owl:        <http://www.w3.org/2002/07/owl#>
                PREFIX :        <https://www.example.org#>
                SELECT ?statement WHERE {
                    ?statement owl:annotatedSource ?s ; owl:annotatedProperty :WATCHED ; owl:annotatedTarget ?o .
                }
                """)) {
            Assertions.assertEquals(12, bindingSets.stream().count());
        }
    }

    @Test
    void testReificationOnlyOfRelationshipsWithNeo4jProperties() {
        ConversionConfig config = new ConversionConfigBuilder().setReificationVocabulary(ReificationVocabulary.OWL_REIFICATION)
                .setBasePrefix(basePrefix)
                .setReifyOnlyRelationshipsWithProperties(true)
                .build();
        storeTestExtension.convertAndImportIntoStore("reify-only-relationships-with-properties.ttl", config);
        try (TupleQueryResult bindingSets = storeTestExtension.executeQuery("""
                PREFIX owl:        <http://www.w3.org/2002/07/owl#>
                PREFIX :        <https://www.example.org#>
                SELECT ?statement WHERE {
                    ?statement owl:annotatedSource ?s ; owl:annotatedProperty :WATCHED ; owl:annotatedTarget ?o .
                }
                """)) {
            Assertions.assertEquals(6, bindingSets.stream().count());
        }
    }

    @Test
    void testDiscardReification() {
        ConversionConfig config = new ConversionConfigBuilder().setReificationVocabulary(ReificationVocabulary.OWL_REIFICATION)
                .setBasePrefix(basePrefix)
                .setReifyRelationships(false)
                .build();
        storeTestExtension.convertAndImportIntoStore("do-not-reify-relationships.ttl", config);
        try (TupleQueryResult bindingSets = storeTestExtension.executeQuery("""
                PREFIX owl:        <http://www.w3.org/2002/07/owl#>
                PREFIX :        <https://www.example.org#>
                SELECT ?statement WHERE {
                    ?statement owl:annotatedSource ?s ; owl:annotatedProperty :WATCHED ; owl:annotatedTarget ?o .
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

    @Test
    void testReificationBlacklist() {
        ConversionConfig config = new ConversionConfigBuilder().setReificationVocabulary(ReificationVocabulary.OWL_REIFICATION)
                .setBasePrefix(basePrefix)
                .setRelationshipTypeReificationBlacklist(List.of("WATCHED", "NON-EXISTENT-1", "NON-EXISTENT-2", "READ"))
                .build();
        storeTestExtension.convertAndImportIntoStore("do-not-reify-relationships.ttl", config);
        try (TupleQueryResult bindingSets = storeTestExtension.executeQuery("""
                PREFIX owl:        <http://www.w3.org/2002/07/owl#>
                PREFIX :           <https://www.example.org#>
                SELECT ?statement WHERE {
                    VALUES ?p {
                        :WATCHED :READ
                    }
                    ?statement owl:annotatedSource ?s ; owl:annotatedProperty ?p ; owl:annotatedTarget ?o .
                }
                """)) {
            Assertions.assertEquals(0, bindingSets.stream().count());
        }

        try (TupleQueryResult bindingSets = storeTestExtension.executeQuery("""
                PREFIX owl:        <http://www.w3.org/2002/07/owl#>
                PREFIX :           <https://www.example.org#>
                SELECT ?s ?p ?o WHERE {
                    ?s :KNOWS ?o .
                }
                """)) {
            List<BindingSet> results = bindingSets.stream().toList();
            Assertions.assertEquals(4, results.size());
        }
    }
}
