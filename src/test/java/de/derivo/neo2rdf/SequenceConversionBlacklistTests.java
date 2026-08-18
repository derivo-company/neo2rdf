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
import de.derivo.neo2rdf.util.SequenceConversionType;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SequenceConversionBlacklistTests {

    @RegisterExtension
    public static final RDFStoreTestExtension storeTestExtension = new RDFStoreTestExtension(TestUtil.getCypherCreateQueries(
            "neo4j-datatypes.cypher"));

    @Test
    public void testRdfCollectionWithBlacklistedProperty() {
        ConversionConfig config = ConversionConfigBuilder.newBuilder()
                .setSequenceConversionType(SequenceConversionType.RDF_COLLECTION)
                .setSequenceConversionTypeBlacklist(List.of("intList"))
                .build();

        storeTestExtension.convertAndImportIntoStore("seq-blacklist-collection-test.ttl", config);
        String b = config.getBasePrefix();

        // check property classifications
        Set<String> objectProperties = storeTestExtension.getInstances(OWL.OBJECTPROPERTY.toString());
        Set<String> dataProperties = storeTestExtension.getInstances(OWL.DATATYPEPROPERTY.toString());

        // intList was blacklisted -> converted using SEPARATE_LITERALS -> DatatypeProperty
        Assertions.assertTrue(dataProperties.contains(b + "intList"));
        Assertions.assertFalse(objectProperties.contains(b + "intList"));

        // floatList was NOT blacklisted -> converted using RDF_COLLECTION -> ObjectProperty
        Assertions.assertTrue(objectProperties.contains(b + "floatList"));
        Assertions.assertFalse(dataProperties.contains(b + "floatList"));

        // verify intList statements are direct literals
        String queryIntList = """
                PREFIX : <%s>
                SELECT ?val WHERE {
                    ?node :intList ?val .
                }
                """.formatted(b);

        List<String> intValues = new ArrayList<>();
        try (TupleQueryResult result = storeTestExtension.executeQuery(queryIntList)) {
            while (result.hasNext()) {
                BindingSet bs = result.next();
                Assertions.assertTrue(bs.getValue("val") instanceof Literal);
                intValues.add(bs.getValue("val").stringValue());
            }
        }
        Assertions.assertEquals(4, intValues.size());
        Assertions.assertTrue(intValues.containsAll(List.of("0", "1", "2", "3")));

        // verify floatList is an RDF collection pointing to blank node
        String queryFloatListCollection = """
                PREFIX : <%s>
                PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                SELECT ?first WHERE {
                    ?node :floatList ?head .
                    ?head rdf:first ?first .
                }
                """.formatted(b);

        try (TupleQueryResult result = storeTestExtension.executeQuery(queryFloatListCollection)) {
            Assertions.assertTrue(result.hasNext());
            BindingSet bs = result.next();
            Assertions.assertEquals("0.0E0", bs.getValue("first").stringValue());
        }
    }

    @Test
    public void testSeparateLiteralsWithBlacklistedProperty() {
        ConversionConfig config = ConversionConfigBuilder.newBuilder()
                .setSequenceConversionType(SequenceConversionType.SEPARATE_LITERALS)
                .setSequenceConversionTypeBlacklist(List.of("floatList"))
                .build();

        storeTestExtension.convertAndImportIntoStore("seq-blacklist-separate-test.ttl", config);
        String b = config.getBasePrefix();

        // check property classifications
        Set<String> objectProperties = storeTestExtension.getInstances(OWL.OBJECTPROPERTY.toString());
        Set<String> dataProperties = storeTestExtension.getInstances(OWL.DATATYPEPROPERTY.toString());

        // floatList was blacklisted -> converted using RDF_COLLECTION -> ObjectProperty
        Assertions.assertTrue(objectProperties.contains(b + "floatList"));
        Assertions.assertFalse(dataProperties.contains(b + "floatList"));

        // intList was NOT blacklisted -> converted using SEPARATE_LITERALS -> DatatypeProperty
        Assertions.assertTrue(dataProperties.contains(b + "intList"));
        Assertions.assertFalse(objectProperties.contains(b + "intList"));

        // verify floatList is an RDF collection pointing to blank node
        String queryFloatListCollection = """
                PREFIX : <%s>
                PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                SELECT ?first WHERE {
                    ?node :floatList ?head .
                    ?head rdf:first ?first .
                }
                """.formatted(b);

        try (TupleQueryResult result = storeTestExtension.executeQuery(queryFloatListCollection)) {
            Assertions.assertTrue(result.hasNext());
            BindingSet bs = result.next();
            Assertions.assertEquals("0.0E0", bs.getValue("first").stringValue());
        }

        // verify intList statements are direct literals
        String queryIntList = """
                PREFIX : <%s>
                SELECT ?val WHERE {
                    ?node :intList ?val .
                }
                """.formatted(b);

        List<String> intValues = new ArrayList<>();
        try (TupleQueryResult result = storeTestExtension.executeQuery(queryIntList)) {
            while (result.hasNext()) {
                BindingSet bs = result.next();
                Assertions.assertTrue(bs.getValue("val") instanceof Literal);
                intValues.add(bs.getValue("val").stringValue());
            }
        }
        Assertions.assertEquals(4, intValues.size());
        Assertions.assertTrue(intValues.containsAll(List.of("0", "1", "2", "3")));
    }
}
