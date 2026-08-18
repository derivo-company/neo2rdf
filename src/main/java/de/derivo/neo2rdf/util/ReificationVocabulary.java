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
    RDF_REIFICATION(RDF.SUBJECT, RDF.PREDICATE, RDF.OBJECT, RDF.STATEMENT, false),
    OWL_REIFICATION(OWL.ANNOTATEDSOURCE, OWL.ANNOTATEDPROPERTY, OWL.ANNOTATEDTARGET, OWL.AXIOM, false),
    RDF_INTEROPERABILITY_VOCABULARY(RDF.PROPOSITION_FORM_SUBJECT,
            RDF.PROPOSITION_FORM_PREDICATE,
            RDF.PROPOSITION_FORM_OBJECT,
            RDF.PROPOSITION_FORM,
            false),
    RDF_12_TRIPLE_TERM(null, null, null, null, true);

    private final IRI subjectProperty;
    private final IRI predicateProperty;
    private final IRI objectProperty;
    private final IRI statementClass;
    private final boolean isNativeRDF12;

    ReificationVocabulary(IRI subjectProperty,
                          IRI predicateProperty,
                          IRI objectProperty,
                          IRI statementClass,
                          boolean isNativeRDF12) {
        this.subjectProperty = subjectProperty;
        this.predicateProperty = predicateProperty;
        this.objectProperty = objectProperty;
        this.statementClass = statementClass;
        this.isNativeRDF12 = isNativeRDF12;
    }

    public boolean isNativeRDF12() {
        return isNativeRDF12;
    }

    public IRI getStatementClassIRI() {
        return statementClass;
    }

    public IRI getPropertyForReifiedSubject() {
        return subjectProperty;
    }

    public IRI getPropertyForReifiedPredicate() {
        return predicateProperty;
    }

    public IRI getPropertyForReifiedObject() {
        return objectProperty;
    }
}
