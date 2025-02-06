package de.derivo.neo2rdf.conversion;

import de.derivo.neo2rdf.conversion.checks.SubsetCheck;
import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFMapper;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFMapperBuilder;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFValueFactory;
import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.roaringbitmap.longlong.Roaring64Bitmap;
import org.tinylog.Logger;

import java.util.Map;
import java.util.Set;

public abstract class Neo4jToRDFConverter {

    private NodeToRDFConverter nodeProcessor;
    private RelationshipToRDFConverter relationshipProcessor;

    protected Neo4jToRDFMapper neo4jToRDFMapper;

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
        Neo4jToRDFMapperBuilder builder = new Neo4jToRDFMapperBuilder(config.getBasePrefix());
        builder.setReificationVocabulary(config.getReificationVocabulary());
        this.neo4jToRDFMapper = builder.build();

        nodeProcessor = new NodeToRDFConverter(neo4jDBConnector, this, config);
        relationshipProcessor = new RelationshipToRDFConverter(neo4jDBConnector, this, config);
    }


    protected abstract void processStatement(Statement s);

    protected abstract void processStatementForDerivedSchema(Statement s);

    public void startProcessing() {
        onStart();
        Logger.info("Processing axiomatic Neo4j triples...");
        processNeo4jAxiomaticTriples();
        Logger.info("Processing nodes...");
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

        Logger.info("Processing relationships...");
        relationshipProcessor.startProcessing();
        this.deployedRelationshipTypes = relationshipProcessor.getDeployedRelationshipTypes();
        this.annotationPropertyKeys = new UnifiedSet<>(defaultMaxExpectedNumberOfProperties);
        this.annotationPropertyKeys.addAll(relationshipProcessor.getObjectPropertyKeys());
        this.annotationPropertyKeys.addAll(relationshipProcessor.getDatatypePropertyKeys());
        this.deployedPropertyKeys.addAll(this.annotationPropertyKeys);

        this.datatypePropertyKeys.addAll(this.relationshipProcessor.getDatatypePropertyKeys());
        this.objectPropertyKeys.addAll(this.relationshipProcessor.getObjectPropertyKeys());

        if (this.config.isDeriveClassHierarchyByLabelSubsetCheck()) {
            Logger.info("Deriving class hierarchy based on Neo4j label subset check...");
            deriveClassHierarchy(nodeProcessor.getLabelToInstanceSet());
        }

        if (this.config.isDerivePropertyHierarchyByRelationshipSubsetCheck()) {
            Logger.info("Deriving property hierarchy based on Neo4j relationship subset check...");
            derivePropertyHierarchy(relationshipProcessor.getRelationshipIDToInstanceSet());
        }

        Logger.info("Adding schema for present labels, property keys, and relationship types...");
        processPropertyKeysAndLabels();
        onFinish();
        Logger.info("Neo4j database successfully processed.");
    }

    private void processNeo4jAxiomaticTriples() {
        Neo4jRDFSchema.AXIOMATIC_TRIPLES.forEach(this::processStatement);
    }

    private void processPropertyKeysAndLabels() {
        deployedNeo4jLabels.forEach((neo4jLabel) -> {
            Resource rdfClass = neo4jToRDFMapper.labelToResource(neo4jLabel);
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

        deployedPropertyKeys.forEach((propertyKey) -> {
            Resource dataProperty = neo4jToRDFMapper.propertyKeyToResource(propertyKey);
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

        deployedRelationshipTypes.forEach((relationshipType) -> {
            Resource objectProperty = neo4jToRDFMapper.relationshipTypeToIRI(relationshipType);
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

    private void deriveClassHierarchy(Map<String, Roaring64Bitmap> labelIDToInstanceSet) {
        SubsetCheck subsetChecker = new SubsetCheck(labelIDToInstanceSet);
        Map<String, Set<String>> subIDToSuperIDs = subsetChecker.deriveSubsumesRelations();
        subIDToSuperIDs.forEach((subID, superIDs) -> {
            Resource subClassIRI = neo4jToRDFMapper.labelToResource(subID);
            for (String superID : superIDs) {
                Resource superClassIRI = neo4jToRDFMapper.labelToResource(superID);
                Statement s = valueFactory.createStatement(
                        subClassIRI,
                        RDFS.SUBCLASSOF,
                        superClassIRI
                );
                processStatementForDerivedSchema(s);
            }
        });
    }

    private void derivePropertyHierarchy(Map<String, Roaring64Bitmap> relationshipTypeIDToInstanceSet) {
        SubsetCheck subsetChecker = new SubsetCheck(relationshipTypeIDToInstanceSet);
        Map<String, Set<String>> subIDToSuperIDs = subsetChecker.deriveSubsumesRelations();
        subIDToSuperIDs.forEach((subID, superIDs) -> {
            Resource subPropertyIRI = neo4jToRDFMapper.relationshipTypeToIRI(subID);
            for (String superID : superIDs) {
                Resource superPropertyIRI = neo4jToRDFMapper.relationshipTypeToIRI(superID);
                Statement s = valueFactory.createStatement(
                        subPropertyIRI,
                        RDFS.SUBPROPERTYOF,
                        superPropertyIRI
                );
                processStatementForDerivedSchema(s);
            }
        });
    }

    protected abstract void onStart();

    protected abstract void onFinish();

    public Set<String> getDeployedNeo4jLabels() {
        return deployedNeo4jLabels;
    }

    public Set<String> getDeployedRelationshipTypes() {
        return deployedRelationshipTypes;
    }

    public Set<String> getDeployedPropertyKeys() {
        return deployedPropertyKeys;
    }
}
