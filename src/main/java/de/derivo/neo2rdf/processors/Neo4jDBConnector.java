package de.derivo.neo2rdf.processors;

import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface Neo4jDBConnector extends AutoCloseable {

    String getDatabase();

    default void query(String cypherQuery, Consumer<Stream<Record>> recordConsumer) {
        try (Session session = getDriver().session(SessionConfig.forDatabase(getDatabase()));
             Transaction tx = session.beginTransaction()) {
            Stream<Record> stream = tx.run(cypherQuery).stream();
            recordConsumer.accept(stream);
            tx.commit();
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        }
    }

    default void updateQuery(List<String> cypherQueries) {
        try (Session session = getDriver().session(SessionConfig.forDatabase(getDatabase()));
             Transaction tx = session.beginTransaction()) {
            for (String cypherQuery : cypherQueries) {
                tx.run(cypherQuery);
            }
            tx.commit();
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        }
    }

    default void clearDatabase() {
        this.query("MATCH (n) DETACH DELETE n;",
                r -> {
                });
    }


    Driver getDriver();

}
