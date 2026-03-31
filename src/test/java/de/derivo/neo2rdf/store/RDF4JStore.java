package de.derivo.neo2rdf.store;

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

import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.Rio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class RDF4JStore {

    protected Repository repository;
    protected RepositoryConnection connection;
    protected boolean rdfsReasoning = false;


    public void terminate() {
        connection.close();
        repository.shutDown();
    }

    public TupleQueryResult executeQuery(String queryStr) {
        TupleQuery tupleQuery = connection.prepareTupleQuery(queryStr);
        return tupleQuery.evaluate();
    }


    public void executeUpdate(String queryStr) {
        Update update = connection.prepareUpdate(queryStr);
        update.execute();
    }

    public void export(File path) {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            RDFHandler writer = Rio.createWriter(RDFFormat.TURTLE, fos);
            connection.exportStatements(null, null, null, true, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean executeAskQuery(String queryStr) {
        BooleanQuery tupleQuery = connection.prepareBooleanQuery(queryStr);
        return tupleQuery.evaluate();
    }


    public void importData(List<File> datasetPaths) {
        for (File dataset : datasetPaths) {
            RDFFormat format = Rio.getParserFormatForFileName(dataset.getName()).orElseThrow();
            try {
                connection.add(dataset, format);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Map<String, Set<String>> getSubClassOfRelations() {
        return getSubToSuperConceptMap(RDFS.SUBCLASSOF.toString());
    }

    public Map<String, Set<String>> getSubPropertyOfRelations() {
        return getSubToSuperConceptMap(RDFS.SUBPROPERTYOF.toString());
    }

    public Map<String, Set<String>> getSubToSuperConceptMap(String property) {
        Map<String, Set<String>> subToSuper = new HashMap<>();
        String subClassVarName = "sub";
        String superClassVarName = "super";
        String query = """
                PREFIX rdfs:       <http://www.w3.org/2000/01/rdf-schema#>
                SELECT ?%1$s ?%2$s
                WHERE {
                    ?%1$s <%3$s> ?%2$s .
                }
                """.formatted(subClassVarName, superClassVarName, property);
        try (TupleQueryResult queryResult = executeQuery(query)) {
            for (BindingSet bindings : queryResult) {
                Resource subConcept = (Resource) bindings.getBinding(subClassVarName).getValue();
                Resource superConcept = (Resource) bindings.getBinding(superClassVarName).getValue();
                subToSuper.computeIfAbsent(subConcept.toString(), ignore -> new UnifiedSet<>())
                        .add(superConcept.toString());
            }
        }
        return subToSuper;
    }
}
