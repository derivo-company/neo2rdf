package de.derivo.neo2rdf.conversion;

import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;

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
        log.info("Neo4j to RDF Turtle conversion successfully accomplished.");
    }
}
