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

import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.config.ConversionConfigBuilder;
import de.derivo.neo2rdf.store.RDFStoreTestExtension;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;
import java.util.Set;

public class DerivedTypeHierarchyTests {
    private static final ConversionConfig config = ConversionConfigBuilder.newBuilder()
            .setDeriveClassHierarchyByLabelSubsetCheck(true)
            .setDerivePropertyHierarchyByRelationshipSubsetCheck(true)
            .build();

    @RegisterExtension
    private static final RDFStoreTestExtension storeTestExtension = new RDFStoreTestExtension(TestUtil.getCypherCreateQueries(
            "neo4j-derived-type-hierarchies.cypher"));


    @Test
    public void derivedClassHierarchy() {
        String outputFileName = "neo4j-derived-hierarchies-test.ttl";
        storeTestExtension.convertAndImportIntoStore(outputFileName, config);
        {
            Set<String> expected = Set.of(
                    "Alice",
                    "Bob",
                    "Charlie"
            );
            Assertions.assertEquals(expected, getNamesOfInstances(config.getBasePrefix() + "Mammal"));
        }

        {
            Set<String> expected = Set.of(
                    "Alice",
                    "Bob"
            );
            Assertions.assertEquals(expected, getNamesOfInstances(config.getBasePrefix() + "Human"));
        }

        {
            Set<String> expected = Set.of(
                    "Alice",
                    "Bob",
                    "Charlie",
                    "Bubbles"
            );
            Assertions.assertEquals(expected, getNamesOfInstances(config.getBasePrefix() + "Animal"));
        }

        Map<String, Set<String>> subClassOfRelations = storeTestExtension.getSubClassOfRelations();
        String b = config.getBasePrefix();
        Assertions.assertTrue(subClassOfRelations.get(b + "Human").contains(b + "Mammal"));
        Assertions.assertTrue(subClassOfRelations.get(b + "Cat").contains(b + "Animal"));
        Assertions.assertTrue(subClassOfRelations.get(b + "Mammal").contains(b + "Animal"));
        Assertions.assertTrue(subClassOfRelations.get(b + "Fish").contains(b + "Animal"));
        Assertions.assertTrue(subClassOfRelations.get(b + "Animal").contains(b + "Entity"));

        Assertions.assertFalse(subClassOfRelations.get(b + "Animal").contains(b + "Cat"));
        Assertions.assertFalse(subClassOfRelations.get(b + "Animal").contains(b + "Mammal"));
        Assertions.assertFalse(subClassOfRelations.get(b + "Animal").contains(b + "Fish"));
    }

    @Test
    public void rdfsMaterialization() {
        String outputFileName = "neo4j-derived-hierarchies-test-materialized.ttl";
        storeTestExtension.convertAndImportIntoStore(outputFileName, config, true);
        {
            Set<String> expected = Set.of(
                    "Alice",
                    "Bob",
                    "Charlie"
            );
            Assertions.assertEquals(expected, getNamesOfInstances(config.getBasePrefix() + "Mammal"));
        }

        {
            Set<String> expected = Set.of(
                    "Alice",
                    "Bob"
            );
            Assertions.assertEquals(expected, getNamesOfInstances(config.getBasePrefix() + "Human"));
        }

        {
            Set<String> expected = Set.of(
                    "Alice",
                    "Bob",
                    "Charlie",
                    "Bubbles"
            );
            Assertions.assertEquals(expected, getNamesOfInstances(config.getBasePrefix() + "Animal"));
        }
    }

    @Test
    public void derivedPropertyHierarchy() {
        String outputFileName = "neo4j-derived-hierarchies-test.ttl";
        storeTestExtension.convertAndImportIntoStore(outputFileName, config);

        Map<String, Set<String>> subPropertyOfMap = storeTestExtension.getSubPropertyOfRelations();
        String b = config.getBasePrefix();
        Assertions.assertTrue(subPropertyOfMap.get(b + "FRIENDS_WITH").contains(b + "KNOWS"));
        Assertions.assertTrue(subPropertyOfMap.get(b + "HAS_PET").contains(b + "KNOWS"));
        Assertions.assertTrue(subPropertyOfMap.get(b + "KNOWS").contains(b + "SOCIAL_RELATION"));

        Assertions.assertFalse(subPropertyOfMap.get(b + "KNOWS").contains(b + "FRIENDS_WITH"));
        Assertions.assertFalse(subPropertyOfMap.get(b + "KNOWS").contains(b + "HAS_PET"));
    }

    private Set<String> getNamesOfInstances(String rdfClassIRI) {
        Set<String> nameOfInstances = new UnifiedSet<>();

        String query = """
                SELECT ?name
                WHERE {
                    ?s <%s> ?name .
                    ?s a <%s> .
                }
                """.formatted(config.getBasePrefix() + "name", rdfClassIRI);

        try (TupleQueryResult bindingSets = storeTestExtension.executeQuery(query)) {
            for (BindingSet bindingSet : bindingSets) {
                String name = bindingSet.getValue("name").stringValue();
                nameOfInstances.add(name);
            }
        }

        return nameOfInstances;
    }
}
