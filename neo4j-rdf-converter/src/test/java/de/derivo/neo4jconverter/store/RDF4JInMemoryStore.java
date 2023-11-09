package de.derivo.neo4jconverter.store;

import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.inferencer.fc.SchemaCachingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class RDF4JInMemoryStore extends RDF4JStore {


    public static void materializeDataset(List<File> dataset, File exportPath) {
        log.info("Loading RDF dataset...");
        RDF4JInMemoryStore rdf4JStore = new RDF4JInMemoryStore(
                dataset, true);
        log.info("Exporting RDFS entailed dataset...");
        rdf4JStore.export(exportPath);
        rdf4JStore.terminate();
        log.info("RDFS materialization finished.");
    }

    public static void materializeDataset(File dataset, File exportPath) {
        materializeDataset(Collections.singletonList(dataset), exportPath);
    }

    public RDF4JInMemoryStore(List<File> datasetPaths, boolean rdfsReasoning) {
        this.rdfsReasoning = rdfsReasoning;
        init(datasetPaths);
    }

    public RDF4JInMemoryStore(List<File> datasetPaths) {
        init(datasetPaths);
    }

    public RDF4JInMemoryStore() {
        init(Collections.emptyList());
    }

    public static void main(String[] args) {
        RDF4JInMemoryStore store = new RDF4JInMemoryStore(Arrays.stream(args)
                .map(File::new)
                .collect(Collectors.toList()));

        Scanner in = new Scanner(System.in);
        StringBuilder query = new StringBuilder();
        try {
            while (true) {
                String nextLine = in.nextLine();
                while (!nextLine.equals("x") && !nextLine.equals("exit")) {
                    query.append(nextLine);
                    nextLine = in.nextLine();
                }
                if (nextLine.equals("exit")) {
                    break;
                }
                store.executeQuery(query.toString());
            }
        } catch (Exception e) {
            System.exit(0);
        }
    }

    protected void init(List<File> datasetPaths) {
        if (rdfsReasoning) {
            repository = new SailRepository(
                    new SchemaCachingRDFSInferencer(
                            new MemoryStore()));
        } else {
            repository = new SailRepository(new MemoryStore());
        }
        connection = repository.getConnection();
        importData(datasetPaths);
    }

    public Set<String> getAssignedClasses(String individualIRI) {
        Set<String> assignedClasses = new UnifiedSet<>();
        TupleQueryResult bindingSets = executeQuery("""
                SELECT ?c
                WHERE {
                    <%s> a ?c .
                }
                """.formatted(individualIRI));
        bindingSets.forEach(bindings -> assignedClasses.add(bindings.getValue("c").toString()));
        return assignedClasses;
    }

    public Set<String> getAllProperties() {
        Set<String> properties = new UnifiedSet<>();
        properties.addAll(getInstances(RDF.PROPERTY.toString()));
        properties.addAll(getInstances(OWL.DATATYPEPROPERTY.toString()));
        properties.addAll(getInstances(OWL.OBJECTPROPERTY.toString()));
        properties.addAll(getInstances(OWL.ANNOTATIONPROPERTY.toString()));
        return properties;
    }

    public Set<String> getInstances(String classIRI) {
        Set<String> result = new UnifiedSet<>();
        TupleQueryResult bindingSets = executeQuery("""
                PREFIX owl:        <http://www.w3.org/2002/07/owl#>
                SELECT ?i
                WHERE {
                    ?i a <%s> .
                }
                """.formatted(classIRI));
        bindingSets.forEach(bindings -> result.add(bindings.getValue("i").toString()));
        return result;
    }

    public void clearStore() {
        executeUpdate("""
                DELETE { ?s ?p ?o }
                WHERE { ?s ?p ?o }
                """);
    }

    public void printData() {
        TupleQueryResult bindingSets = executeQuery("""
                SELECT ?s ?p ?o WHERE { ?s ?p ?o }
                """);
        bindingSets.forEach(b -> b.forEach(binding -> System.out.println(binding.getValue())));
    }


}
