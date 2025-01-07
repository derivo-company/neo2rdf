package de.derivo.neo2rdf.conversion;

import de.derivo.neo2rdf.conversion.checks.SubsetCheck;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFMapper;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFMapperBuilder;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFValueFactory;
import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import de.derivo.neo2rdf.util.ConsoleUtil;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.roaringbitmap.longlong.Roaring64Bitmap;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;

public abstract class Neo4jToRDFConverter {

    protected Logger log = ConsoleUtil.getLogger();
    private NodeToRDFConverter nodeProcessor;
    private RelationshipToRDFConverter relationshipProcessor;
    private boolean includeDeletedNeo4jLabels = false;
    private boolean includeDeletedPropertyKeys = false;
    private boolean includeDeletedRelationshipTypes = false;

    protected Neo4jToRDFMapper neo4jToRDFMapper;
    protected IndexedNeo4jSchema indexedSchema;

    private Set<String> deployedNeo4jLabels;
    private Set<String> deployedRelationshipTypes;
    private Set<String> deployedPropertyKeys;
    private Set<String> datatypePropertyKeys;
    private Set<String> objectPropertyKeys;
    private Set<String> annotationPropertyKeys;

    private final ValueFactory valueFactory = new Neo4jToRDFValueFactory();

    private final Neo4jDBConnector neo4jDBConnector;
    protected final ConversionConfig config;

    public Neo4jToRDFConverter(Neo4jDBConnector neo4jDBConnector, ConversionConfig config) {
        this.neo4jDBConnector = neo4jDBConnector;
        this.config = config;
        init();
    }

    protected void init() {
        this.includeDeletedRelationshipTypes = config.isIncludeDeletedRelationshipTypes();
        this.includeDeletedNeo4jLabels = config.isIncludeDeletedNeo4jLabels();
        this.includeDeletedPropertyKeys = config.isIncludeDeletedPropertyKeys();

        log.info("Indexing schema of dataset...");

        Neo4jToRDFMapperBuilder builder = new Neo4jToRDFMapperBuilder(config.getBasePrefix());
        builder.setReificationVocabulary(config.getReificationVocabulary());
        this.neo4jToRDFMapper = builder.build();

        indexedSchema = new IndexedNeo4jSchema(neo4jDBConnector);
        nodeProcessor = new NodeToRDFConverter(neo4jDBConnector, this, config);
        relationshipProcessor = new RelationshipToRDFConverter(neo4jDBConnector, this, config);
    }


    protected abstract void processStatement(Statement s);

    protected abstract void processStatementForDerivedSchema(Statement s);

    public void startProcessing() {
        onStart();
        log.info("Processing axiomatic Neo4j triples...");
        processNeo4jAxiomaticTriples();
        log.info("Processing nodes...");
        nodeProcessor.startProcessing();
        int defaultMaxExpectedNumberOfProperties = 1_000;
        this.deployedPropertyKeys = new UnifiedSet<>(defaultMaxExpectedNumberOfProperties);
        this.objectPropertyKeys = new UnifiedSet<>(defaultMaxExpectedNumberOfProperties);
        this.datatypePropertyKeys = new UnifiedSet<>(defaultMaxExpectedNumberOfProperties);

        this.deployedNeo4jLabels = this.nodeProcessor.getDeployedNeo4jLabels();
        this.deployedPropertyKeys.addAll(this.nodeProcessor.getDatatypePropertyKeys());
        this.deployedPropertyKeys.addAll(this.nodeProcessor.getObjectPropertyKeys());

        this.datatypePropertyKeys.addAll(this.nodeProcessor.getDatatypePropertyKeys());
        this.objectPropertyKeys.addAll(this.nodeProcessor.getObjectPropertyKeys());

        log.info("Processing relationships...");
        relationshipProcessor.startProcessing();
        this.deployedRelationshipTypes = relationshipProcessor.getDeployedRelationshipTypes();
        this.annotationPropertyKeys = new UnifiedSet<>(defaultMaxExpectedNumberOfProperties);
        this.annotationPropertyKeys.addAll(relationshipProcessor.getObjectPropertyKeys());
        this.annotationPropertyKeys.addAll(relationshipProcessor.getDatatypePropertyKeys());
        this.deployedPropertyKeys.addAll(this.annotationPropertyKeys);

        this.datatypePropertyKeys.addAll(this.relationshipProcessor.getDatatypePropertyKeys());
        this.objectPropertyKeys.addAll(this.relationshipProcessor.getObjectPropertyKeys());

        if (this.config.isDeriveClassHierarchyByLabelSubsetCheck()) {
            log.info("Deriving class hierarchy based on Neo4j label subset check...");
            //deriveClassHierarchy(nodeProcessor.getLabelToInstanceSet());
            // TODO
            throw new IllegalStateException();
        }

        if (this.config.isDerivePropertyHierarchyByRelationshipSubsetCheck()) {
            log.info("Deriving property hierarchy based on Neo4j relationship subset check...");
            derivePropertyHierarchy(relationshipProcessor.getRelationshipIDToInstanceSet());
        }

        log.info("Adding schema for present labels, property keys, and relationship types...");
        processPropertyKeysAndLabels();
        onFinish();
        log.info("Neo4j database successfully processed.");
    }

    private void processNeo4jAxiomaticTriples() {
        Neo4jRDFSchema.AXIOMATIC_TRIPLES.forEach(this::processStatement);
    }

    private void processPropertyKeysAndLabels() {
        indexedSchema.getNeo4jLabels().forEach((neo4jLabel) -> {
            if (!includeDeletedNeo4jLabels && !deployedNeo4jLabels.contains(neo4jLabel)) {
                return;
            }
            Resource rdfClass = neo4jToRDFMapper.labelIDToResource(neo4jLabel);
            Statement s = valueFactory.createStatement(
                    rdfClass,
                    RDFS.LABEL,
                    valueFactory.createLiteral(neo4jLabel)
            );
            processStatement(s);
            s = valueFactory.createStatement(
                    rdfClass,
                    RDF.TYPE,
                    OWL.CLASS
            );
            processStatement(s);
        });

        indexedSchema.getPropertyKeys().forEach((propertyKey) -> {
            if (!includeDeletedPropertyKeys && !deployedPropertyKeys.contains(propertyKey)) {
                return;
            }
            Resource dataProperty = neo4jToRDFMapper.propertyKeyIDToResource(propertyKey);
            if (propertyKey.startsWith("__org.neo4j.SchemaRule.")) {
                // skip schema rules
                // TODO still relevant?
                return;
            }
            Statement s = valueFactory.createStatement(
                    dataProperty,
                    RDFS.LABEL,
                    valueFactory.createLiteral(propertyKey)
            );
            processStatement(s);

            if (datatypePropertyKeys.contains(propertyKey)) {
                s = valueFactory.createStatement(
                        dataProperty,
                        RDF.TYPE,
                        OWL.DATATYPEPROPERTY
                );
                processStatement(s);
            }

            if (objectPropertyKeys.contains(propertyKey)) {
                s = valueFactory.createStatement(
                        dataProperty,
                        RDF.TYPE,
                        OWL.OBJECTPROPERTY
                );
                processStatement(s);
            }

            if (annotationPropertyKeys.contains(propertyKey)) {
                s = valueFactory.createStatement(
                        dataProperty,
                        RDF.TYPE,
                        OWL.ANNOTATIONPROPERTY
                );
                processStatement(s);
            }

            s = valueFactory.createStatement(
                    dataProperty,
                    RDF.TYPE,
                    RDF.PROPERTY
            );
            processStatement(s);

        });

        indexedSchema.getRelationshipTypes().forEach((relationshipType) -> {
            if (!includeDeletedRelationshipTypes && !deployedRelationshipTypes.contains(relationshipType)) {
                return;
            }
            Resource objectProperty = neo4jToRDFMapper.relationshipTypeIDToIRI(relationshipType);
            Statement s = valueFactory.createStatement(
                    objectProperty,
                    RDFS.LABEL,
                    valueFactory.createLiteral(relationshipType)
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

    private void deriveClassHierarchy(Map<Long, Roaring64Bitmap> labelIDToInstanceSet) {
        SubsetCheck subsetChecker = new SubsetCheck(labelIDToInstanceSet);
        Map<Long, Set<Long>> subIDToSuperIDs = subsetChecker.deriveSubsumesRelations();
        subIDToSuperIDs.forEach((subID, superIDs) -> {
            // TODO
            /*
            Resource subClassIRI = neo4jToRDFMapper.labelIDToResource(subID);
            for (Long superID : superIDs) {
                Resource superClassIRI = neo4jToRDFMapper.labelIDToResource(superID);
                Statement s = valueFactory.createStatement(
                        subClassIRI,
                        RDFS.SUBCLASSOF,
                        superClassIRI
                );
                processStatementForDerivedSchema(s);
            }

             */
        });
    }

    private void derivePropertyHierarchy(Map<Long, Roaring64Bitmap> relationshipTypeIDToInstanceSet) {
        SubsetCheck subsetChecker = new SubsetCheck(relationshipTypeIDToInstanceSet);
        Map<Long, Set<Long>> subIDToSuperIDs = subsetChecker.deriveSubsumesRelations();
        subIDToSuperIDs.forEach((subID, superIDs) -> {
            // TODO
            /*
            Resource subPropertyIRI = neo4jToRDFMapper.relationshipTypeIDToIRI(subID);
            for (Long superID : superIDs) {
                Resource superPropertyIRI = neo4jToRDFMapper.relationshipTypeIDToIRI(superID);
                Statement s = valueFactory.createStatement(
                        subPropertyIRI,
                        RDFS.SUBPROPERTYOF,
                        superPropertyIRI
                );
                processStatementForDerivedSchema(s);
            }
             */
        });
    }

    public IndexedNeo4jSchema getIndexedSchema() {
        return indexedSchema;
    }

    protected abstract void onStart();

    protected abstract void onFinish();


}
