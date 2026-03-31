package de.derivo.neo2rdf.conversion;

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
import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.tinylog.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class Neo4jDBToTurtle extends Neo4jToRDFConverter {
    private TurtleWriter turtleWriter;
    private TurtleWriter schemaWriter = null;
    private final OutputStream outputStream;

    public Neo4jDBToTurtle(Neo4jDBConnector neo4jDBConnector, ConversionConfig config, OutputStream outputStream) {
        super(neo4jDBConnector, config);
        this.outputStream = outputStream;
    }

    @Override
    protected void processStatement(Statement s) {
        turtleWriter.handleStatement(s);
    }

    @Override
    protected void processStatementForDerivedSchema(Statement s) {
        if (schemaWriter != null) {
            schemaWriter.handleStatement(s);
        } else {
            turtleWriter.handleStatement(s);
        }
    }

    @Override
    protected void onStart() {
        turtleWriter = new TurtleWriter(outputStream);
        turtleWriter.startRDF();
        addNamespacesToTurtleWriter(turtleWriter);

        if (this.config.getSchemaOutputPath() != null) {
            try {
                schemaWriter = new TurtleWriter(new FileOutputStream(config.getSchemaOutputPath()));
                schemaWriter.startRDF();
                addNamespacesToTurtleWriter(schemaWriter);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void addNamespacesToTurtleWriter(TurtleWriter writer) {
        writer.handleNamespace(neo4jToRDFMapper.getBaseNamespace().getPrefix(),
                neo4jToRDFMapper.getBaseNamespace().getName());
        writer.handleNamespace(Neo4jRDFSchema.neo4jNamespace.getPrefix(),
                Neo4jRDFSchema.neo4jNamespace.getName());
        writer.handleNamespace(RDF.PREFIX, RDF.NAMESPACE);
        writer.handleNamespace(XSD.PREFIX, XSD.NAMESPACE);
        writer.handleNamespace(RDFS.PREFIX, RDFS.NAMESPACE);
        writer.handleNamespace(OWL.PREFIX, OWL.NAMESPACE);
    }

    @Override
    protected void onFinish() {
        turtleWriter.endRDF();
        if (schemaWriter != null) {
            schemaWriter.endRDF();
        }
        Logger.info("Neo4j to RDF Turtle conversion successfully accomplished.");
    }
}
