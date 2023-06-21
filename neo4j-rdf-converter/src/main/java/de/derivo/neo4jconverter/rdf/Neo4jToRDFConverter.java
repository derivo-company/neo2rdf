package de.derivo.neo4jconverter.rdf;

import de.derivo.neo4jconverter.rdf.checks.SubsetCheck;
import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import de.derivo.neo4jconverter.rdf.model.Neo4jToRDFMapper;
import de.derivo.neo4jconverter.rdf.model.Neo4jToRDFMapperBuilder;
import de.derivo.neo4jconverter.schema.IndexedNeo4jSchema;
import de.derivo.neo4jconverter.schema.IndexedNeo4jSchemaGenerator;
import de.derivo.neo4jconverter.util.ConsoleUtil;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.neo4j.kernel.impl.store.NeoStores;
import org.roaringbitmap.longlong.Roaring64Bitmap;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;

public abstract class Neo4jToRDFConverter {

    protected Logger log = ConsoleUtil.getLogger();
    private final NeoStores neoStores;
    private NodeToRDFConverter nodeProcessor;
    private RelationshipToRDFConverter relationshipProcessor;
    private boolean includeDeletedNeo4jLabels = false;
    private boolean includeDeletedPropertyKeys = false;
    private boolean includeDeletedRelationshipTypes = false;

    private IndexedNeo4jSchema indexedSchema;

    protected Neo4jToRDFMapper neo4jToRDFMapper;

    private Map<Long, String> neo4jLabelIDToStr;
    private Map<Long, String> propertyKeyIDToStr;
    private Map<Long, String> relationshipTypeIDToStr;

    private Set<Long> deployedNeo4jLabels;
    private Set<Long> deployedRelationshipTypes;
    private Set<Long> deployedPropertyKeys;
    private Set<Long> datatypePropertyKeys;
    private Set<Long> objectPropertyKeys;
    private Set<Long> annotationPropertyKeys;

    private final ValueFactory valueFactory = Values.getValueFactory();

    private final ConversionConfig config;

    public Neo4jToRDFConverter(NeoStores neoStores, ConversionConfig config) {
        this.neoStores = neoStores;
        this.config = config;
        init();
    }

    protected void init() {
        this.includeDeletedRelationshipTypes = config.isIncludeDeletedRelationshipTypes();
        this.includeDeletedNeo4jLabels = config.isIncludeDeletedNeo4jLabels();
        this.includeDeletedPropertyKeys = config.isIncludeDeletedPropertyKeys();

        log.info("Indexing schema of dataset...");
        IndexedNeo4jSchemaGenerator indexedSchemaGenerator = new IndexedNeo4jSchemaGenerator(neoStores);
        this.indexedSchema = indexedSchemaGenerator.generate();
        this.neo4jLabelIDToStr = indexedSchema.getLabelIDToStr();
        this.propertyKeyIDToStr = indexedSchema.getPropertyKeyIDToStr();
        this.relationshipTypeIDToStr = indexedSchema.getRelationshipTypeIDToStr();

        Neo4jToRDFMapperBuilder builder = new Neo4jToRDFMapperBuilder(config.getBasePrefix(), this.indexedSchema);
        builder.setReificationVocabulary(config.getReificationVocabulary());
        this.neo4jToRDFMapper = builder.build();

        nodeProcessor = new NodeToRDFConverter(neoStores, this, config);
        relationshipProcessor = new RelationshipToRDFConverter(neoStores, this, config);
    }


    protected abstract void processStatement(Statement s);

    public void startProcessing() {
        onStart();
        log.info("Processing axiomatic Neo4j triples...");
        processNeo4jAxiomaticTriples();
        log.info("Processing nodes...");
        nodeProcessor.startProcessing();
        this.deployedPropertyKeys = new UnifiedSet<>(indexedSchema.getPropertyKeyIDToStr().size());
        this.objectPropertyKeys = new UnifiedSet<>(indexedSchema.getPropertyKeyIDToStr().size());
        this.datatypePropertyKeys = new UnifiedSet<>(indexedSchema.getPropertyKeyIDToStr().size());

        this.deployedNeo4jLabels = this.nodeProcessor.getDeployedNeo4jLabels();
        this.deployedPropertyKeys.addAll(this.nodeProcessor.getDatatypePropertyKeys());
        this.deployedPropertyKeys.addAll(this.nodeProcessor.getObjectPropertyKeys());

        this.datatypePropertyKeys.addAll(this.nodeProcessor.getDatatypePropertyKeys());
        this.objectPropertyKeys.addAll(this.nodeProcessor.getObjectPropertyKeys());

        log.info("Processing relationships...");
        relationshipProcessor.startProcessing();
        this.deployedRelationshipTypes = relationshipProcessor.getDeployedRelationshiptypes();
        this.annotationPropertyKeys = new UnifiedSet<>(indexedSchema.getPropertyKeyIDToStr().size());
        this.annotationPropertyKeys.addAll(relationshipProcessor.getObjectPropertyKeys());
        this.annotationPropertyKeys.addAll(relationshipProcessor.getDatatypePropertyKeys());
        this.deployedPropertyKeys.addAll(this.annotationPropertyKeys);

        this.datatypePropertyKeys.addAll(this.relationshipProcessor.getDatatypePropertyKeys());
        this.objectPropertyKeys.addAll(this.relationshipProcessor.getObjectPropertyKeys());

        if (this.config.isDeriveClassHierarchyByLabelSubsetCheck()) {
            log.info("Deriving class hierarchy based on Neo4j label subset check...");
            deriveClassHierarchy(nodeProcessor.getLabelIDToInstanceSet());
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
        neo4jLabelIDToStr.forEach((labelID, str) -> {
            if (!includeDeletedNeo4jLabels && !deployedNeo4jLabels.contains(labelID)) {
                return;
            }
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
            if (!includeDeletedPropertyKeys && !deployedPropertyKeys.contains(propertyKeyID)) {
                return;
            }
            Resource dataProperty = neo4jToRDFMapper.propertyKeyIDToResource(propertyKeyID);
            if (str.startsWith("__org.neo4j.SchemaRule.")) {
                // skip schema rules
                return;
            }
            Statement s = valueFactory.createStatement(
                    dataProperty,
                    RDFS.LABEL,
                    Values.literal(str)
            );
            processStatement(s);

            if (datatypePropertyKeys.contains(propertyKeyID)) {
                s = valueFactory.createStatement(
                        dataProperty,
                        RDF.TYPE,
                        OWL.DATATYPEPROPERTY
                );
                processStatement(s);
            }

            if (objectPropertyKeys.contains(propertyKeyID)) {
                s = valueFactory.createStatement(
                        dataProperty,
                        RDF.TYPE,
                        OWL.OBJECTPROPERTY
                );
                processStatement(s);
            }

            if (annotationPropertyKeys.contains(propertyKeyID)) {
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

        relationshipTypeIDToStr.forEach((relationshipTypeID, str) -> {
            if (!includeDeletedRelationshipTypes && !deployedRelationshipTypes.contains(relationshipTypeID)) {
                return;
            }
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

    private void deriveClassHierarchy(Map<Long, Roaring64Bitmap> labelIDToInstanceSet) {
        SubsetCheck subsetChecker = new SubsetCheck(labelIDToInstanceSet);
        Map<Long, Set<Long>> subIDToSuperIDs = subsetChecker.deriveSubsumesRelations();
        subIDToSuperIDs.forEach((subID, superIDs) -> {
            Resource subClassIRI = neo4jToRDFMapper.labelIDToResource(subID);
            for (Long superID : superIDs) {
                Resource superClassIRI = neo4jToRDFMapper.labelIDToResource(superID);
                Statement s = valueFactory.createStatement(
                        subClassIRI,
                        RDFS.SUBCLASSOF,
                        superClassIRI
                );
                processStatement(s);
            }
        });
    }

    private void derivePropertyHierarchy(Map<Long, Roaring64Bitmap> relationshipTypeIDToInstanceSet) {
        SubsetCheck subsetChecker = new SubsetCheck(relationshipTypeIDToInstanceSet);
        Map<Long, Set<Long>> subIDToSuperIDs = subsetChecker.deriveSubsumesRelations();
        subIDToSuperIDs.forEach((subID, superIDs) -> {
            Resource subPropertyIRI = neo4jToRDFMapper.relationshipTypeIDToIRI(subID);
            for (Long superID : superIDs) {
                Resource superPropertyIRI = neo4jToRDFMapper.relationshipTypeIDToIRI(superID);
                Statement s = valueFactory.createStatement(
                        subPropertyIRI,
                        RDFS.SUBPROPERTYOF,
                        superPropertyIRI
                );
                processStatement(s);
            }
        });
    }

    public IndexedNeo4jSchema getIndexedSchema() {
        return indexedSchema;
    }

    protected abstract void onStart();

    protected abstract void onFinish();


}
