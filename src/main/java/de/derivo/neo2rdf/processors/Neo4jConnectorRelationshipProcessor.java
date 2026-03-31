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

import de.derivo.neo2rdf.util.ConsoleUtil;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.tinylog.Logger;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class Neo4jConnectorRelationshipProcessor implements RelationshipProcessor {

    private final Neo4jDBConnector connector;

    private final long PROGRESS_MESSAGE_AFTER_X_RELATIONSHIPS = 100_000;

    public Neo4jConnectorRelationshipProcessor(Neo4jDBConnector connector) {
        this.connector = connector;
    }

    public void startProcessing() {
        processRelationships();
    }

    private void processRelationships() {
        AtomicLong relationshipCounter = new AtomicLong(0);
        Consumer<Stream<Record>> recordConsumer = records -> records.forEach(record -> {
            String nodeIdSource = record.get("nodeIdSource").asLong() + "";
            String nodeIdTarget = record.get("nodeIdTarget").asLong() + "";
            String relationshipType = record.get("relationshipType").asString();
            String relId = record.get("relationshipId").asLong() + "";
            Value properties = record.get("relationshipProperties");
            Map<String, Value> propertyValuePairs = properties.asMap(Function.identity());
            process(relId, nodeIdSource, nodeIdTarget, relationshipType,
                    propertyValuePairs);

            if (relationshipCounter.incrementAndGet() % PROGRESS_MESSAGE_AFTER_X_RELATIONSHIPS == 0) {
                Logger.info("Processed %s relationships and their assigned properties.".formatted(ConsoleUtil.formatDecimal(
                        relationshipCounter.get())));
            }
        });

        this.connector.query("""
                        MATCH (n)-[r]->(m)
                        RETURN id(n) AS nodeIdSource,
                               id(m) AS nodeIdTarget,
                               type(r) AS relationshipType,
                               id(r) AS relationshipId,
                               properties(r) AS relationshipProperties;
                        """,
                recordConsumer);
    }


}
