package de.derivo.neo2rdf.util;

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
