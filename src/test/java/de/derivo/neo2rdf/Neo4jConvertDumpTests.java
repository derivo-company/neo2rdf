package de.derivo.neo2rdf;

import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.config.ConversionConfigBuilder;
import de.derivo.neo2rdf.store.RDFStoreTestExtension;
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
            TestUtil.getResource("neo4j-db-example.dump"),
            TestUtil.getTempDirectory("neo4j-db-dump-output-example"));


    @Test
    public void loadArchiveAndConvertDB() {
        storeTestExtension.convertAndImportIntoStore("neo4j-dump-output.ttl", config, true);
        Assertions.assertFalse(storeTestExtension.getInstances(RDFS.RESOURCE.toString()).isEmpty());
    }
}
