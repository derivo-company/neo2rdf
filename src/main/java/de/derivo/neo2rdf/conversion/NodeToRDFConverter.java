package de.derivo.neo2rdf.conversion;

import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFMapper;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFValueFactory;
import de.derivo.neo2rdf.processors.Neo4jConnectorNodeProcessor;
import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.neo4j.driver.Value;
import org.roaringbitmap.longlong.Roaring64Bitmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NodeToRDFConverter extends Neo4jConnectorNodeProcessor implements RDFPropertyProcessor {

    private final Neo4jToRDFConverter neo4jToRDFConverter;
    private final Neo4jToRDFMapper neo4jToRDFMapper;
    private final ValueFactory valueFactory = new Neo4jToRDFValueFactory();

    private Set<String> datatypePropertyKeys;
    private Set<String> objectPropertyKeys;
    private Set<String> deployedNeo4jLabels;
    private final ConversionConfig config;
    private Map<String, Roaring64Bitmap> labelToInstanceSet = null;

    public NodeToRDFConverter(Neo4jDBConnector neo4jDBConnector, Neo4jToRDFConverter neo4jToRDFConverter,
                              ConversionConfig config) {
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
            labelToInstanceSet.computeIfAbsent(assignedLabel, ignore -> new Roaring64Bitmap())
                    .add(Long.parseLong(nodeID));
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
        processProperty(getMapper().nodeIDToResource(nodeID), propertyKey, value);
    }

    public Set<String> getDatatypePropertyKeys() {
        return datatypePropertyKeys;
    }

    public Set<String> getDeployedNeo4jLabels() {
        return deployedNeo4jLabels;
    }

    @Override
    public Neo4jToRDFMapper getMapper() {
        return neo4jToRDFMapper;
    }

    @Override
    public ConversionConfig getConfig() {
        return config;
    }

    @Override
    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    @Override
    public Neo4jToRDFConverter getConverter() {
        return neo4jToRDFConverter;
    }

    public Set<String> getObjectPropertyKeys() {
        return objectPropertyKeys;
    }

    public Map<String, Roaring64Bitmap> getLabelToInstanceSet() {
        return labelToInstanceSet;
    }
}
