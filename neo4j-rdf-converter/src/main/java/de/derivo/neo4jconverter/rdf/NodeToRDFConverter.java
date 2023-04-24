package de.derivo.neo4jconverter.rdf;

import de.derivo.neo4jconverter.processors.NodeProcessor;
import de.derivo.neo4jconverter.rdf.model.Neo4jToRDFMapper;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.neo4j.kernel.impl.store.NeoStores;
import org.neo4j.values.SequenceValue;
import org.neo4j.values.storable.PointValue;
import org.neo4j.values.storable.Value;

public class NodeToRDFConverter extends NodeProcessor {

    private final Neo4jToRDFConverter neo4jToRDFConverter;
    private final Neo4jToRDFMapper neo4jToRDFMapper;
    private final ValueFactory valueFactory = Values.getValueFactory();

    public NodeToRDFConverter(NeoStores neoStores, Neo4jToRDFConverter neo4jToRDFConverter) {
        super(neoStores);
        this.neo4jToRDFConverter = neo4jToRDFConverter;
        this.neo4jToRDFMapper = neo4jToRDFConverter.neo4jToRDFMapper;
    }

    @Override
    protected void process(long nodeID, long assignedLabelID) {
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
            neo4jToRDFMapper.sequenceValueToRDFListStatements(neo4jToRDFMapper.nodeIDToResource(nodeID),
                    propertyKeyID,
                    sequenceValue,
                    neo4jToRDFConverter::processStatement);
        } else if (value instanceof PointValue) {
            neo4jToRDFMapper.pointPropertyToRDFStatements(neo4jToRDFMapper.nodeIDToResource(nodeID),
                    propertyKeyID,
                    (PointValue) value,
                    neo4jToRDFConverter::processStatement);
        } else {
            Statement statement = valueFactory.createStatement(
                    neo4jToRDFMapper.nodeIDToResource(nodeID),
                    neo4jToRDFMapper.propertyKeyIDToResource(propertyKeyID),
                    Values.literal(value.asObject()));
            neo4jToRDFConverter.processStatement(statement);
        }
    }

}
