package de.derivo.neo4jconverter.rdf;

import de.derivo.neo4jconverter.processors.NodeProcessor;
import de.derivo.neo4jconverter.rdf.config.ConversionConfig;
import de.derivo.neo4jconverter.rdf.model.Neo4jToRDFMapper;
import de.derivo.neo4jconverter.rdf.model.Neo4jToRDFValueFactory;
import de.derivo.neo4jconverter.util.SequenceConversionType;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.neo4j.kernel.impl.store.NeoStores;
import org.neo4j.values.SequenceValue;
import org.neo4j.values.storable.PointValue;
import org.neo4j.values.storable.Value;
import org.roaringbitmap.longlong.Roaring64Bitmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NodeToRDFConverter extends NodeProcessor {

    private final Neo4jToRDFConverter neo4jToRDFConverter;
    private final Neo4jToRDFMapper neo4jToRDFMapper;
    private final ValueFactory valueFactory = new Neo4jToRDFValueFactory();

    private Set<Long> datatypePropertyKeys;
    private Set<Long> objectPropertyKeys;
    private Set<Long> deployedNeo4jLabels;
    private final ConversionConfig config;
    private Map<Long, Roaring64Bitmap> labelIDToInstanceSet = null;

    public NodeToRDFConverter(NeoStores neoStores, Neo4jToRDFConverter neo4jToRDFConverter, ConversionConfig config) {
        super(neoStores);
        this.neo4jToRDFConverter = neo4jToRDFConverter;
        this.neo4jToRDFMapper = neo4jToRDFConverter.neo4jToRDFMapper;
        this.config = config;
        init();
    }

    private void init() {
        this.datatypePropertyKeys = new UnifiedSet<>(neo4jToRDFConverter.getIndexedSchema().getPropertyKeyIDToStr().size());
        this.objectPropertyKeys = new UnifiedSet<>(neo4jToRDFConverter.getIndexedSchema().getPropertyKeyIDToStr().size());
        this.deployedNeo4jLabels = new UnifiedSet<>(neo4jToRDFConverter.getIndexedSchema().getLabelIDToStr().size());

        if (config.isDeriveClassHierarchyByLabelSubsetCheck()) {
            this.labelIDToInstanceSet = new HashMap<>(neo4jToRDFConverter.getIndexedSchema().getLabelIDToStr().size());
        }
    }

    @Override
    protected void process(long nodeID, long assignedLabelID) {
        deployedNeo4jLabels.add(assignedLabelID);

        if (labelIDToInstanceSet != null) {
            labelIDToInstanceSet.computeIfAbsent(assignedLabelID, ignore -> new Roaring64Bitmap())
                    .add(nodeID);
        }

        Statement statement = valueFactory.createStatement(
                neo4jToRDFMapper.nodeIDToResource(nodeID),
                RDF.TYPE,
                neo4jToRDFMapper.labelIDToResource(assignedLabelID)
        );
        neo4jToRDFConverter.processStatement(statement);
    }

    @Override
    protected void process(long nodeID, long propertyKeyID, Value value) {
        if (value.isSequenceValue()) {
            SequenceValue sequenceValue = (SequenceValue) value;
            neo4jToRDFMapper.sequenceValueToRDF(neo4jToRDFMapper.nodeIDToResource(nodeID),
                    propertyKeyID,
                    sequenceValue,
                    neo4jToRDFConverter::processStatement,
                    this.config.getSequenceConversionType());

            if (this.config.getSequenceConversionType().equals(SequenceConversionType.RDF_COLLECTION)) {
                objectPropertyKeys.add(propertyKeyID);
            } else {
                datatypePropertyKeys.add(propertyKeyID);
            }
        } else if (value instanceof PointValue) {
            objectPropertyKeys.add(propertyKeyID);

            neo4jToRDFMapper.pointPropertyToRDFStatements(neo4jToRDFMapper.nodeIDToResource(nodeID),
                    propertyKeyID,
                    (PointValue) value,
                    neo4jToRDFConverter::processStatement);
        } else {
            datatypePropertyKeys.add(propertyKeyID);

            Statement statement = valueFactory.createStatement(
                    neo4jToRDFMapper.nodeIDToResource(nodeID),
                    neo4jToRDFMapper.propertyKeyIDToResource(propertyKeyID),
                    Values.literal(valueFactory, value.asObject(), true));
            neo4jToRDFConverter.processStatement(statement);
        }
    }

    public Set<Long> getDatatypePropertyKeys() {
        return datatypePropertyKeys;
    }

    public Set<Long> getDeployedNeo4jLabels() {
        return deployedNeo4jLabels;
    }

    public Set<Long> getObjectPropertyKeys() {
        return objectPropertyKeys;
    }

    public Map<Long, Roaring64Bitmap> getLabelIDToInstanceSet() {
        return labelIDToInstanceSet;
    }
}
