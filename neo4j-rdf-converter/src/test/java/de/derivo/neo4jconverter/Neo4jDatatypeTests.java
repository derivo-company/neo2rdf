package de.derivo.neo4jconverter;

import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import de.derivo.neo4jconverter.rdf.config.ConversionConfigBuilder;
import de.derivo.neo4jconverter.store.RDFStoreTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Neo4jDatatypeTests extends ConverterTestBase {

    @RegisterExtension
    private static final RDFStoreTestExtension storeTestExtension = new RDFStoreTestExtension("neo4j-datatypes-example");
    private static final ConversionConfig config = ConversionConfigBuilder.newBuilder().build();

    @Test
    public void neo4jToRDFConverter() {
        storeTestExtension.convertAndImportIntoStore("neo4j-datatypes-test.ttl", config);
        // TODO
    }
}
