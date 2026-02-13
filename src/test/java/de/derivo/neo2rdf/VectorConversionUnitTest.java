package de.derivo.neo2rdf;

import de.derivo.neo2rdf.conversion.model.Neo4jToRDFMapperBuilder;
import de.derivo.neo2rdf.util.VectorConversionType;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.internal.InternalFloat32Vector;
import org.neo4j.driver.internal.InternalInt32Vector;

import java.util.ArrayList;
import java.util.List;


/**
 * Unit tests for Neo4j native Vector datatypes to RDF conversion.
 * <p>
 * <b>Architectural Note: Why this is a Unit Test</b><br>
 * Native Vector properties in Neo4j require the Enterprise Edition "Block" storage format.
 * The open-source Community Edition used in our standard Testcontainers setup only supports
 * the "Aligned" format and will throw an exception if vector persistence is attempted.
 * <p>
 * To avoid forcing open-source contributors to implicitly accept a commercial Neo4j Enterprise
 * evaluation license just to run the test suite, we bypass the database engine entirely.
 * By directly instantiating the internal Vector objects, we can verify
 * our RDF mapping logic in pure Java with 100% isolation and compliance.
 */
public class VectorConversionUnitTest {

    private final String prefix = "https://example.org/";
    private final SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();

    @Test
    public void testIntegerVectorToStringConversion() {
        InternalInt32Vector intVector = new InternalInt32Vector(new int[]{1, 2, 3});
        List<Statement> statements = new ArrayList<>();

        new Neo4jToRDFMapperBuilder(prefix).build().vectorValueToRDF(
                valueFactory.createIRI(prefix + "node-1"),
                "embedding",
                intVector,
                statements::add,
                VectorConversionType.COMMA_SEPARATED_STRING
        );

        Assertions.assertEquals(1, statements.size());
        Assertions.assertEquals("1,2,3", statements.getFirst().getObject().stringValue());
    }

    @Test
    public void testFloatVectorToStringConversion() {
        InternalFloat32Vector floatVector = new InternalFloat32Vector(new float[]{0.1f, 0.5f, 0.9f});
        List<Statement> statements = new ArrayList<>();

        new Neo4jToRDFMapperBuilder(prefix).build().vectorValueToRDF(
                valueFactory.createIRI(prefix + "node-1"),
                "embedding",
                floatVector,
                statements::add,
                VectorConversionType.COMMA_SEPARATED_STRING
        );

        Assertions.assertEquals(1, statements.size());
        String resultString = statements.getFirst().getObject().stringValue();
        Assertions.assertTrue(resultString.contains("0.1") && resultString.contains("0.9"));
    }

    @Test
    public void testFloatVectorToRDFCollection() {
        InternalFloat32Vector floatVector = new InternalFloat32Vector(new float[]{0.1f, 0.5f, 0.9f});
        List<Statement> statements = new ArrayList<>();

        new Neo4jToRDFMapperBuilder(prefix).build().vectorValueToRDF(
                valueFactory.createIRI(prefix + "node-1"),
                "embedding",
                floatVector,
                statements::add,
                VectorConversionType.RDF_COLLECTION
        );

        // verify using an isolated in-memory RDF4J repository
        Repository rep = new SailRepository(new MemoryStore());
        try (RepositoryConnection conn = rep.getConnection()) {
            conn.add(statements);

            String query = """
                    PREFIX : <%s>
                    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                    SELECT ?val
                    WHERE {
                        <url:node-1> :embedding ?list .
                        ?list rdf:rest*/rdf:first ?val .
                    }
                    """.formatted(prefix).replace("url:node-1", prefix + "node-1");

            TupleQuery tupleQuery = conn.prepareTupleQuery(query);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                List<Float> results = new ArrayList<>();
                for (BindingSet bindings : result) {
                    results.add(((org.eclipse.rdf4j.model.Literal) bindings.getValue("val")).floatValue());
                }

                Assertions.assertEquals(3, results.size(), "List should contain exactly 3 elements");
                Assertions.assertTrue(results.contains(0.1f));
                Assertions.assertTrue(results.contains(0.5f));
                Assertions.assertTrue(results.contains(0.9f));
            }
        } finally {
            rep.shutDown();
        }
    }

    @Test
    public void testIntegerVectorToRDFCollection() {
        InternalInt32Vector intVector = new InternalInt32Vector(new int[]{42, 1337});
        List<Statement> statements = new ArrayList<>();

        new Neo4jToRDFMapperBuilder(prefix).build().vectorValueToRDF(
                valueFactory.createIRI(prefix + "node-1"),
                "embedding",
                intVector,
                statements::add,
                VectorConversionType.RDF_COLLECTION
        );

        Repository rep = new SailRepository(new MemoryStore());
        try (RepositoryConnection conn = rep.getConnection()) {
            conn.add(statements);

            String query = """
                    PREFIX : <%s>
                    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                    SELECT ?val
                    WHERE {
                        <url:node-1> :embedding ?list .
                        ?list rdf:rest*/rdf:first ?val .
                    }
                    """.formatted(prefix).replace("url:node-1", prefix + "node-1");

            TupleQuery tupleQuery = conn.prepareTupleQuery(query);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                List<Integer> results = new ArrayList<>();
                for (BindingSet bindings : result) {
                    results.add(((org.eclipse.rdf4j.model.Literal) bindings.getValue("val")).intValue());
                }

                Assertions.assertEquals(2, results.size());
                Assertions.assertTrue(results.contains(42));
                Assertions.assertTrue(results.contains(1337));
            }
        } finally {
            rep.shutDown();
        }
    }
}