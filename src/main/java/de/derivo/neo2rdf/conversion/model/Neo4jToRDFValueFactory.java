package de.derivo.neo2rdf.conversion.model;

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

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.base.CoreDatatype;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Values;

public class Neo4jToRDFValueFactory extends SimpleValueFactory {

    private final Literal MINUS_INFINITY = Values.literal("-INF", CoreDatatype.XSD.DOUBLE);
    private final Literal PLUS_INFINITY = Values.literal("INF", CoreDatatype.XSD.DOUBLE);
    private final Literal ZERO = Values.literal("0.0", CoreDatatype.XSD.DOUBLE);

    @Override
    public Literal createLiteral(double value) {
        if (value < -1.797693134862315E+308) {
            return MINUS_INFINITY;
        }
        if (value > 1.797693134862315E+308) {
            return PLUS_INFINITY;
        }
        if (value > 0 && value < 2.225073858507201E-308
            || value < 0 && value > -2.225073858507201E-308) {
            // Java double values have higher precision than XSD datatype, i.e., 4.94065645841246544E-324
            return ZERO;
        }
        return super.createLiteral(value);
    }
}
