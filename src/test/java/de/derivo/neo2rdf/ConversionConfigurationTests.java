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
