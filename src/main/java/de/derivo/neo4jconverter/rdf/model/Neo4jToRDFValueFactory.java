package de.derivo.neo4jconverter.rdf.model;

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
