package de.derivo.neo4jconverter;

import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import de.derivo.neo4jconverter.rdf.config.ConversionConfigBuilder;
import de.derivo.neo4jconverter.store.RDFStoreTestExtension;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Neo4jConvertDumpTests {

    private static final ConversionConfig config = ConversionConfigBuilder.newBuilder()
            .setDeriveClassHierarchyByLabelSubsetCheck(true)
            .build();

    @RegisterExtension
    public static final RDFStoreTestExtension storeTestExtension = new RDFStoreTestExtension(
            "neo4j-db-example.dump",
            "temp/neo4j-db-dump-output-example");


    @Test
    public void loadArchiveAndConvertDB() {
        storeTestExtension.convertAndImportIntoStore("neo4j-dump-output.ttl", config, true);
        Assertions.assertFalse(storeTestExtension.getInstances(RDFS.RESOURCE.toString()).isEmpty());
    }
}
