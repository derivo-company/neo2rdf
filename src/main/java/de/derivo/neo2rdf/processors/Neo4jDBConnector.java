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
