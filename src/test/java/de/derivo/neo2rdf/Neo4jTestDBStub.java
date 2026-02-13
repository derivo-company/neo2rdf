package de.derivo.neo2rdf;

import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import org.neo4j.driver.Driver;
import org.tinylog.Logger;

public record Neo4jTestDBStub(Driver driver) implements Neo4jDBConnector {

    public Neo4jTestDBStub(Driver driver) {
        this.driver = driver;
        Logger.info("Neo4j test DB stub initialized with container driver.");
    }

    @Override
    public String getDatabase() {
        return "neo4j";
    }

    @Override
    public void close() {
        if (driver != null) {
            driver.close();
        }
    }
}