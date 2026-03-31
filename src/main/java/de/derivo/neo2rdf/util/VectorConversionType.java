package de.derivo.neo2rdf.util;

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

public enum VectorConversionType {
    /**
     * Converts a vector into a single string literal with
     * comma-separated values.
     */
    COMMA_SEPARATED_STRING,

    /**
     * Converts a vector into an RDF list (collection) of float literals.
     */
    RDF_COLLECTION
}
