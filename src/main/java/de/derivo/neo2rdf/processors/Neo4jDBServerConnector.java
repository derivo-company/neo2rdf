package de.derivo.neo2rdf.processors;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jDBServerConnector implements Neo4jDBConnector {
    private final String uri;
    private final String user;
    private final String password;
    private String database;

    private Driver driver;

    public Neo4jDBServerConnector(String uri, String user, String password, String database) {
        this.uri = uri;
        this.user = user;
        this.password = password;
        if (database != null) {
            this.database = database;
        }
        init();
    }

    private void init() {
        if (user != null && password != null) {
            driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        } else {
            driver = GraphDatabase.driver(uri);
        }
    }


    @Override
    public void close() {
        driver.close();
    }

    public String getUser() {
        return user;
    }

    @Override
    public String getDatabase() {
        return database;
    }

    @Override
    public Driver getDriver() {
        return driver;
    }

    public String getUri() {
        return uri;
    }


}
