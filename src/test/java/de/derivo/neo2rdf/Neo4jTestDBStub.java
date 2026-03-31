package de.derivo.neo2rdf;

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
