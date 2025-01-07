package de.derivo.neo2rdf.processors;

import org.neo4j.driver.Record;
import org.neo4j.driver.*;

import java.util.function.Consumer;
import java.util.stream.Stream;

public class Neo4jDBConnector implements AutoCloseable {
    private String uri;
    private final String user;
    private final String password;
    private String database;
    private Driver driver;

    public Neo4jDBConnector(String uri, String user, String password, String database) {
        this.uri = uri;
        this.user = user;
        this.password = password;
        if (database != null) {
            this.database = database;
        }
        init();
    }

    private void init() {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public void query(String cypherQuery, Consumer<Stream<Record>> recordConsumer) {
        try (Session session = driver.session(SessionConfig.forDatabase(database)); Transaction tx = session.beginTransaction()) {
            Stream<Record> stream = tx.run(cypherQuery).stream();
            recordConsumer.accept(stream);
            tx.commit();
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void close() {
        driver.close();
    }

    public String getUser() {
        return user;
    }

    public String getDatabase() {
        return database;
    }

    public String getUri() {
        return uri;
    }

    public void clearDatabase() {
        this.query("MATCH (n) DETACH DELETE n;",
                r -> {
                });
    }
}
