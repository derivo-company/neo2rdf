package de.derivo.neo4jconverter;

import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import de.derivo.neo4jconverter.rdf.config.ConversionConfigBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ConversionConfigurationTests {

    private final String basePrefix = "https://www.example.org#";
    private final ConversionConfig config = new ConversionConfigBuilder()
            .setIncludeDeletedNeo4jLabels(true)
            .setIncludeDeletedRelationshipTypes(true)
            .setIncludeDeletedPropertyKeys(true)
            .setBasePrefix(basePrefix)
            .build();

    @Test
    public void testReadWriteConversionConfig() {
        File outputFile = TestUtil.getResource("temp/configTest.yaml");
        config.write(outputFile);
        Assertions.assertTrue(outputFile.exists());

        ConversionConfig cfg = ConversionConfig.read(outputFile);
        Assertions.assertEquals(basePrefix, cfg.getBasePrefix());
    }

}
