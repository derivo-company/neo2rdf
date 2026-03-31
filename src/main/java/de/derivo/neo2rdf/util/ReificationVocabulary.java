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
