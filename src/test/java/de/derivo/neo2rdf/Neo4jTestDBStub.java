package de.derivo.neo2rdf;

import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import de.derivo.neo2rdf.util.ConsoleUtil;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.slf4j.Logger;

@SuppressWarnings("CanBeFinal")
public class Neo4jTestDBStub implements Neo4jDBConnector {

    private Logger log = ConsoleUtil.getLogger();

    private final Neo4j neo4j;
    private final Driver driver;

    public Neo4jTestDBStub() {
        log.info("Initializing Neo4j test DB stub...");
        neo4j = Neo4jBuilders.newInProcessBuilder().withDisabledServer().build();
        driver = GraphDatabase.driver(neo4j.boltURI(), AuthTokens.none());
        log.info("Neo4j test DB stub successfully initialized.");
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
