package de.derivo.neo2rdf.conversion;

import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFMapper;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFValueFactory;
import de.derivo.neo2rdf.processors.Neo4jConnectorNodeProcessor;
import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import de.derivo.neo2rdf.util.Neo4jValueUtil;
import de.derivo.neo2rdf.util.SequenceConversionType;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.neo4j.driver.Value;
import org.roaringbitmap.longlong.Roaring64Bitmap;

import java.util.HashMap;
import java.util.List;
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
        int initialCapacity = 10_000;
        this.datatypePropertyKeys = new UnifiedSet<>(initialCapacity);
        this.objectPropertyKeys = new UnifiedSet<>(initialCapacity);
        this.deployedNeo4jLabels = new UnifiedSet<>(initialCapacity);

        if (config.isDeriveClassHierarchyByLabelSubsetCheck()) {
            this.labelToInstanceSet = new HashMap<>(initialCapacity);
        }
    }

    @Override
    public void process(String nodeID, String assignedLabel) {
        deployedNeo4jLabels.add(assignedLabel);

        if (labelToInstanceSet != null) {
            labelToInstanceSet.computeIfAbsent(assignedLabel, ignore -> new Roaring64Bitmap()).add(Long.parseLong(nodeID));
        }

        Statement statement = valueFactory.createStatement(
                neo4jToRDFMapper.nodeIDToResource(nodeID),
                RDF.TYPE,
                neo4jToRDFMapper.labelToResource(assignedLabel)
        );
        neo4jToRDFConverter.processStatement(statement);
    }

    @Override
    public void process(String nodeID, String propertyKey, Value value) {
        if (Neo4jValueUtil.isList(value)) {
            List<Object> sequenceValue = value.asList();
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
        } else if (Neo4jValueUtil.isPoint(value)) {
            objectPropertyKeys.add(propertyKey);

            neo4jToRDFMapper.pointPropertyToRDFStatements(neo4jToRDFMapper.nodeIDToResource(nodeID),
                    propertyKey,
                    value.asPoint(),
                    neo4jToRDFConverter::processStatement);
        } else {
            datatypePropertyKeys.add(propertyKey);

            Statement statement = valueFactory.createStatement(
                    neo4jToRDFMapper.nodeIDToResource(nodeID),
                    neo4jToRDFMapper.propertyKeyToResource(propertyKey),
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
