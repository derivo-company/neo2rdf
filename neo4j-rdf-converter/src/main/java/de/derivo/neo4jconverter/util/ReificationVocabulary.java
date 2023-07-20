package de.derivo.neo4jconverter.util;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;

public enum ReificationVocabulary {
    RDF_REIFICATION,
    OWL_REIFICATION;

    ReificationVocabulary() {
    }

    public IRI getStatementClassIRI() {
        switch (this) {
            case OWL_REIFICATION -> {
                return OWL.AXIOM;
            }
            case RDF_REIFICATION -> {
                return RDF.STATEMENT;
            }
            default -> throw new IllegalStateException();
        }
    }

    public IRI getPropertyForReifiedSubject() {
        switch (this) {
            case OWL_REIFICATION -> {
                return OWL.ANNOTATEDSOURCE;
            }
            case RDF_REIFICATION -> {
                return RDF.SUBJECT;
            }
            default -> throw new IllegalStateException();
        }
    }

    public IRI getPropertyForReifiedPredicate() {
        switch (this) {
            case OWL_REIFICATION -> {
                return OWL.ANNOTATEDPROPERTY;
            }
            case RDF_REIFICATION -> {
                return RDF.PREDICATE;
            }
            default -> throw new IllegalStateException();
        }
    }

    public IRI getPropertyForReifiedObject() {
        switch (this) {
            case OWL_REIFICATION -> {
                return OWL.ANNOTATEDTARGET;
            }
            case RDF_REIFICATION -> {
                return RDF.OBJECT;
            }
            default -> throw new IllegalStateException();
        }
    }
}
