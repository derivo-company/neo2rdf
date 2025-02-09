package de.derivo.neo2rdf;

import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.config.ConversionConfigBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ConversionConfigurationTests {

    private final String basePrefix = "https://www.example.org#";
    private final ConversionConfig config = new ConversionConfigBuilder()
            .setBasePrefix(basePrefix)
            .build();

    @Test
    public void readWriteConversionConfig() {
        File outputFile = TestUtil.getTempFile("configTest.yaml");
        outputFile.getParentFile().mkdirs();
        config.write(outputFile);
        Assertions.assertTrue(outputFile.exists());

        ConversionConfig cfg = ConversionConfig.read(outputFile);
        Assertions.assertEquals(basePrefix, cfg.getBasePrefix());
    }

}
