package de.derivo.neo2rdf.processors;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface Neo4jDBConnector extends AutoCloseable {

    String getDatabase();

    default void query(String cypherQuery, Consumer<Stream<Record>> recordConsumer) {
        try (Session session = driver().session(SessionConfig.forDatabase(getDatabase()))) {
            session.executeRead(tx -> {
                Stream<Record> stream = tx.run(cypherQuery).stream();
                recordConsumer.accept(stream);
                return null;
            });
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        }
    }

    default void updateQuery(List<String> cypherQueries) {
        try (Session session = driver().session(SessionConfig.forDatabase(getDatabase()))) {
            session.executeWrite(tx -> {
                for (String cypherQuery : cypherQueries) {
                    tx.run(cypherQuery).consume();
                }
                return null;
            });
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        }
    }

    default void clearDatabase() {
        this.updateQuery(List.of("MATCH (n) DETACH DELETE n"));
    }

    Driver driver();

}
