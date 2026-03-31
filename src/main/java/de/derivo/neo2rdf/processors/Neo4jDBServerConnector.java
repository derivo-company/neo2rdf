package de.derivo.neo2rdf.processors;

/*-
 * #%L
 * neo2rdf
 * %%
 * Copyright (C) 2026 Derivo Company
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
    public Driver driver() {
        return driver;
    }

    public String getUri() {
        return uri;
    }


}
