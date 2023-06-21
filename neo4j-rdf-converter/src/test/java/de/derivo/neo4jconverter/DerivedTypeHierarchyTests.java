package de.derivo.neo4jconverter;

import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import de.derivo.neo4jconverter.rdf.config.ConversionConfigBuilder;
import de.derivo.neo4jconverter.store.RDFStoreTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class DerivedTypeHierarchyTests {
    private static final ConversionConfig config = ConversionConfigBuilder.newBuilder()
            .setDeriveClassHierarchyByLabelSubsetCheck(true)
            .setDerivePropertyHierarchyByRelationshipSubsetCheck(true)
            .build();

    @RegisterExtension
    private static final RDFStoreTestExtension storeTestExtension = new RDFStoreTestExtension("neo4j-derived-type-hierarchy");

    @Test
    public void testDeriveTypeHierarchies() {
        String outputFileName = "neo4j-derived-hierarchies-test.ttl";
        // TODO
    }
}
