package de.derivo.neo2rdf;

import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.tinylog.Logger;


public class Neo4jTestDBStub implements Neo4jDBConnector {

    private final Neo4j neo4j;
    private final Driver driver;

    public Neo4jTestDBStub() {
        Logger.info("Initializing Neo4j test DB stub...");
        neo4j = Neo4jBuilders.newInProcessBuilder().withDisabledServer().build();
        driver = GraphDatabase.driver(neo4j.boltURI(), AuthTokens.none());
        Logger.info("Neo4j test DB stub successfully initialized.");
    }

    @Override
    public String getDatabase() {
        return "neo4j";
    }

    @Override
    public Driver getDriver() {
        return driver;
    }

    @Override
    public void close() {
        driver.close();
        neo4j.close();
    }
}
