package de.derivo.neo2rdf.conversion;

import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFMapper;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFValueFactory;
import de.derivo.neo2rdf.processors.Neo4jConnectorNodeProcessor;
import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import de.derivo.neo2rdf.util.SequenceConversionType;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.neo4j.values.SequenceValue;
import org.neo4j.values.storable.PointValue;
import org.neo4j.values.storable.Value;
import org.roaringbitmap.longlong.Roaring64Bitmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NodeToRDFConverter extends Neo4jConnectorNodeProcessor {

    private final Neo4jToRDFConverter neo4jToRDFConverter;
    private final Neo4jToRDFMapper neo4jToRDFMapper;
    private final ValueFactory valueFactory = new Neo4jToRDFValueFactory();

    private Set<String> datatypePropertyKeys;
    private Set<String> objectPropertyKeys;
    private Set<String> deployedNeo4jLabels;
    private final ConversionConfig config;
    private Map<String, Roaring64Bitmap> labelToInstanceSet = null;

    public NodeToRDFConverter(Neo4jDBConnector neo4jDBConnector, Neo4jToRDFConverter neo4jToRDFConverter, ConversionConfig config) {
        super(neo4jDBConnector);
        this.neo4jToRDFConverter = neo4jToRDFConverter;
        this.neo4jToRDFMapper = neo4jToRDFConverter.neo4jToRDFMapper;
        this.config = config;
        init();
    }

    private void init() {
        this.datatypePropertyKeys = new UnifiedSet<>(neo4jToRDFConverter.getIndexedSchema().getPropertyKeys().size());
        this.objectPropertyKeys = new UnifiedSet<>(neo4jToRDFConverter.getIndexedSchema().getRelationshipTypes().size());
        this.deployedNeo4jLabels = new UnifiedSet<>(neo4jToRDFConverter.getIndexedSchema().getNeo4jLabels().size());

        if (config.isDeriveClassHierarchyByLabelSubsetCheck()) {
            this.labelToInstanceSet = new HashMap<>(neo4jToRDFConverter.getIndexedSchema().getNeo4jLabels().size());
        }
    }

    @Override
    public void process(String nodeID, String assignedLabel) {
        deployedNeo4jLabels.add(assignedLabel);

        if (labelToInstanceSet != null) {
            //labelToInstanceSet.computeIfAbsent(assignedLabel, ignore -> new Roaring64Bitmap()) .add(nodeID);
            // TODO
            throw new IllegalStateException();
        }

        Statement statement = valueFactory.createStatement(
                neo4jToRDFMapper.nodeIDToResource(nodeID),
                RDF.TYPE,
                neo4jToRDFMapper.labelIDToResource(assignedLabel)
        );
        neo4jToRDFConverter.processStatement(statement);
    }

    @Override
    public void process(String nodeID, String propertyKey, Value value) {
        if (value.isSequenceValue()) {
            SequenceValue sequenceValue = (SequenceValue) value;
            neo4jToRDFMapper.sequenceValueToRDF(neo4jToRDFMapper.nodeIDToResource(nodeID),
                    propertyKey,
                    sequenceValue,
                    neo4jToRDFConverter::processStatement,
                    this.config.getSequenceConversionType());

            if (this.config.getSequenceConversionType().equals(SequenceConversionType.RDF_COLLECTION)) {
                objectPropertyKeys.add(propertyKey);
            } else {
                datatypePropertyKeys.add(propertyKey);
            }
        } else if (value instanceof PointValue) {
            objectPropertyKeys.add(propertyKey);

            neo4jToRDFMapper.pointPropertyToRDFStatements(neo4jToRDFMapper.nodeIDToResource(nodeID),
                    propertyKey,
                    (PointValue) value,
                    neo4jToRDFConverter::processStatement);
        } else {
            datatypePropertyKeys.add(propertyKey);

            Statement statement = valueFactory.createStatement(
                    neo4jToRDFMapper.nodeIDToResource(nodeID),
                    neo4jToRDFMapper.propertyKeyIDToResource(propertyKey),
                    Values.literal(valueFactory, value.asObject(), true));
            neo4jToRDFConverter.processStatement(statement);
        }
    }

    public Set<String> getDatatypePropertyKeys() {
        return datatypePropertyKeys;
    }

    public Set<String> getDeployedNeo4jLabels() {
        return deployedNeo4jLabels;
    }

    public Set<String> getObjectPropertyKeys() {
        return objectPropertyKeys;
    }

    public Map<String, Roaring64Bitmap> getLabelToInstanceSet() {
        return labelToInstanceSet;
    }
}
