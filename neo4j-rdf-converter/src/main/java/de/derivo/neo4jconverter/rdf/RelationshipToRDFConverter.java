package de.derivo.neo4jconverter.rdf;

import de.derivo.neo4jconverter.processors.RelationshipProcessor;
import de.derivo.neo4jconverter.rdf.model.Neo4jToRDFMapper;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.neo4j.kernel.impl.store.NeoStores;
import org.neo4j.values.SequenceValue;
import org.neo4j.values.storable.PointValue;
import org.neo4j.values.storable.Value;

public class RelationshipToRDFConverter extends RelationshipProcessor {
    private final Neo4jToRDFConverter neo4jToRDFConverter;
    private final Neo4jToRDFMapper neo4jToRDFMapper;
    private final ValueFactory valueFactory = Values.getValueFactory();

    public RelationshipToRDFConverter(NeoStores neoStores, Neo4jToRDFConverter neo4jToRDFConverter) {
        super(neoStores);
        this.neo4jToRDFConverter = neo4jToRDFConverter;
        this.neo4jToRDFMapper = neo4jToRDFConverter.neo4jToRDFMapper;
    }

    @Override
    protected void process(long relationshipID,
                           long sourceID,
                           long targetID,
                           long typeID,
                           boolean statementHasAnnotations) {
        Statement statement = valueFactory.createStatement(
                neo4jToRDFMapper.nodeIDToResource(sourceID),
                neo4jToRDFMapper.relationshipTypeIDToIRI(typeID),
                neo4jToRDFMapper.nodeIDToResource(targetID),
                neo4jToRDFMapper.relationshipIDToResource(relationshipID)
        );
        if (statementHasAnnotations) {
            neo4jToRDFConverter.processStatement(statement);
            neo4jToRDFMapper.statementToReificationTriples(statement, neo4jToRDFConverter::processStatement);
        } else {
            neo4jToRDFConverter.processStatement(statement);
        }
    }

    @Override
    protected void process(long relationshipID, long propertyKeyID, Value value) {
        if (value.isSequenceValue()) {
            SequenceValue sequenceValue = (SequenceValue) value;
            neo4jToRDFMapper.sequenceValueToRDFListStatements(neo4jToRDFMapper.relationshipIDToResource(relationshipID),
                    propertyKeyID,
                    sequenceValue,
                    neo4jToRDFConverter::processStatement);
        } else if (value instanceof PointValue) {
            neo4jToRDFMapper.pointPropertyToRDFStatements(neo4jToRDFMapper.relationshipIDToResource(relationshipID),
                    propertyKeyID,
                    (PointValue) value,
                    neo4jToRDFConverter::processStatement);
        } else {
            Statement statement = valueFactory.createStatement(
                    neo4jToRDFMapper.relationshipIDToResource(relationshipID),
                    neo4jToRDFMapper.propertyKeyIDToResource(propertyKeyID),
                    Values.literal(value.asObject())
            );
            neo4jToRDFConverter.processStatement(statement);
        }
    }
}
