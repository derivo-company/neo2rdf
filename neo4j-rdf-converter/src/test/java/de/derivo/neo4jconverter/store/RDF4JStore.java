package de.derivo.neo4jconverter.store;

import de.derivo.neo4jconverter.util.ConsoleUtil;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class RDF4JStore {

    protected static Logger log = ConsoleUtil.getLogger();
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
            e.printStackTrace();
        }
    }

    public boolean executeAskQuery(String queryStr) {
        BooleanQuery tupleQuery = connection.prepareBooleanQuery(queryStr);
        return tupleQuery.evaluate();
    }


    public void importData(List<File> datasetPaths) {
        for (File dataset : datasetPaths) {
            RDFFormat format = Rio.getParserFormatForFileName(dataset.getName()).get();
            try {
                connection.add(dataset, format);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
