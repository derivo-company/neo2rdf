package de.derivo.neo4jconverter.rdf;

import de.derivo.neo4jconverter.processors.NodeProcessor;
import de.derivo.neo4jconverter.processors.RelationshipProcessor;
import de.derivo.neo4jconverter.rdf.model.Neo4jToRDFMapper;
import de.derivo.neo4jconverter.rdf.model.Neo4jToRDFMapperBuilder;
import de.derivo.neo4jconverter.schema.IndexedNeo4jSchema;
import de.derivo.neo4jconverter.schema.IndexedNeo4jSchemaGenerator;
import de.derivo.neo4jconverter.utils.ConsoleUtils;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.neo4j.kernel.impl.store.NeoStores;
import org.slf4j.Logger;

import java.util.Map;

public abstract class Neo4jToRDFConverter {

    protected Logger log = ConsoleUtils.getLogger();
    private final NeoStores neoStores;
    private NodeProcessor nodeProcessor;
    private RelationshipProcessor relationshipProcessor;
    private final String baseURI;

    private IndexedNeo4jSchema indexedSchema;

    protected Neo4jToRDFMapper neo4jToRDFMapper;

    private Map<Long, String> labelIDToStr;
    private Map<Long, String> propertyKeyIDToStr;
    private Map<Long, String> relationshipTypeIDToStr;

    private final ValueFactory valueFactory = Values.getValueFactory();

    public Neo4jToRDFConverter(NeoStores neoStores, String basePrefix) {
        this.neoStores = neoStores;
        this.baseURI = basePrefix;
        init();
    }

    protected void init() {

        log.info("Indexing schema of dataset...");
        IndexedNeo4jSchemaGenerator indexedSchemaGenerator = new IndexedNeo4jSchemaGenerator(neoStores);
        this.indexedSchema = indexedSchemaGenerator.generate();
        this.labelIDToStr = indexedSchema.getLabelIDToStr();
        this.propertyKeyIDToStr = indexedSchema.getPropertyKeyIDToStr();
        this.relationshipTypeIDToStr = indexedSchema.getRelationshipTypeIDToStr();

        Neo4jToRDFMapperBuilder builder = new Neo4jToRDFMapperBuilder(this.baseURI, this.indexedSchema);
        this.neo4jToRDFMapper = builder.build();

        nodeProcessor = new NodeToRDFConverter(neoStores, this);
        relationshipProcessor = new RelationshipToRDFConverter(neoStores, this);
    }


    protected abstract void processStatement(Statement s);

    public void startProcessing() {
        onStart();
        log.info("Processing axiomatic Neo4j triples...");
        processNeo4jAxiomaticTriples();
        processPropertyKeysAndLabels();
        log.info("Processing nodes...");
        nodeProcessor.startProcessing();
        log.info("Processing relationships...");
        relationshipProcessor.startProcessing();
        onFinish();
        log.info("Neo4j database successfully processed.");
    }

    private void processNeo4jAxiomaticTriples() {
        Neo4jRDFSchema.AXIOMATIC_TRIPLES.forEach(this::processStatement);
    }

    private void processPropertyKeysAndLabels() {
        labelIDToStr.forEach((labelID, str) -> {
            Resource rdfClass = neo4jToRDFMapper.labelIDToResource(labelID);
            Statement s = valueFactory.createStatement(
                    rdfClass,
                    RDFS.LABEL,
                    Values.literal(str)
            );
            processStatement(s);
            s = valueFactory.createStatement(
                    rdfClass,
                    RDF.TYPE,
                    OWL.CLASS
            );
            processStatement(s);
        });

        propertyKeyIDToStr.forEach((propertyKeyID, str) -> {
            Resource dataProperty = neo4jToRDFMapper.propertyKeyIDToResource(propertyKeyID);
            Statement s = valueFactory.createStatement(
                    dataProperty,
                    RDFS.LABEL,
                    Values.literal(str)
            );
            processStatement(s);
            s = valueFactory.createStatement(
                    dataProperty,
                    RDF.TYPE,
                    OWL.DATATYPEPROPERTY
            );
            processStatement(s);
        });

        relationshipTypeIDToStr.forEach((relationshipTypeID, str) -> {
            Resource objectProperty = neo4jToRDFMapper.relationshipTypeIDToIRI(relationshipTypeID);
            Statement s = valueFactory.createStatement(
                    objectProperty,
                    RDFS.LABEL,
                    Values.literal(str)
            );
            processStatement(s);
            s = valueFactory.createStatement(
                    objectProperty,
                    RDF.TYPE,
                    OWL.OBJECTPROPERTY
            );
            processStatement(s);
        });

    }


    protected abstract void onStart();

    protected abstract void onFinish();


}
