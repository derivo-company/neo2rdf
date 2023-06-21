package de.derivo.neo4jconverter.rdf;

import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.neo4j.kernel.impl.store.NeoStores;

import java.io.OutputStream;

public class Neo4jDBToTurtle extends Neo4jToRDFConverter {
    private TurtleWriter turtleWriter;
    private final OutputStream outputStream;

    public Neo4jDBToTurtle(NeoStores neoStores, ConversionConfig config, OutputStream outputStream) {
        super(neoStores, config);
        this.outputStream = outputStream;
    }

    @Override
    protected void processStatement(Statement s) {
        turtleWriter.handleStatement(s);
    }

    @Override
    protected void onStart() {
        turtleWriter = new TurtleWriter(outputStream);
        turtleWriter.startRDF();
        turtleWriter.handleNamespace(neo4jToRDFMapper.getBaseNamespace().getPrefix(),
                neo4jToRDFMapper.getBaseNamespace().getName());
        turtleWriter.handleNamespace(Neo4jRDFSchema.neo4jNamespace.getPrefix(),
                Neo4jRDFSchema.neo4jNamespace.getName());
        turtleWriter.handleNamespace(RDF.PREFIX, RDF.NAMESPACE);
        turtleWriter.handleNamespace(XSD.PREFIX, XSD.NAMESPACE);
        turtleWriter.handleNamespace(RDFS.PREFIX, RDFS.NAMESPACE);
        turtleWriter.handleNamespace(OWL.PREFIX, OWL.NAMESPACE);
    }

    @Override
    protected void onFinish() {
        turtleWriter.endRDF();
        log.info("Neo4j to RDF Turtle conversion successfully accomplished.");
    }
}
