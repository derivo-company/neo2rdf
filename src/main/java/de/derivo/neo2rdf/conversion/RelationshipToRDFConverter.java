package de.derivo.neo2rdf.conversion;

import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFMapper;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFValueFactory;
import de.derivo.neo2rdf.processors.RelationshipProcessor;
import de.derivo.neo2rdf.util.ConsoleUtil;
import de.derivo.neo2rdf.util.SequenceConversionType;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.neo4j.kernel.impl.store.NeoStores;
import org.neo4j.values.SequenceValue;
import org.neo4j.values.storable.PointValue;
import org.neo4j.values.storable.Value;
import org.roaringbitmap.longlong.Roaring64Bitmap;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class RelationshipToRDFConverter extends RelationshipProcessor {
    private static final Logger log = ConsoleUtil.getLogger();

    private final Neo4jToRDFConverter neo4jToRDFConverter;
    private final Neo4jToRDFMapper neo4jToRDFMapper;
    private final ValueFactory valueFactory = new Neo4jToRDFValueFactory();

    private Set<Long> deployedRelationshipTypes;
    private Set<Long> datatypePropertyKeys;
    private Set<Long> objectPropertyKeys;
    private final ConversionConfig config;

    private boolean reifyRelationships;
    private Set<Long> relationshipTypeIDReificationBlacklist;

    private Map<Long, Roaring64Bitmap> relationshipIDToInstanceSet = null;

    public RelationshipToRDFConverter(NeoStores neoStores, Neo4jToRDFConverter neo4jToRDFConverter, ConversionConfig config) {
        super(neoStores);
        this.neo4jToRDFConverter = neo4jToRDFConverter;
        this.neo4jToRDFMapper = neo4jToRDFConverter.neo4jToRDFMapper;
        this.config = config;
        init();
    }

    private void init() {
        this.deployedRelationshipTypes = new UnifiedSet<>(neo4jToRDFConverter.getIndexedSchema().getRelationshipTypeIDToStr().size());
        this.datatypePropertyKeys = new UnifiedSet<>(neo4jToRDFConverter.getIndexedSchema().getPropertyKeyIDToStr().size());
        this.objectPropertyKeys = new UnifiedSet<>(neo4jToRDFConverter.getIndexedSchema().getPropertyKeyIDToStr().size());

        if (this.config.isDerivePropertyHierarchyByRelationshipSubsetCheck()) {
            this.relationshipIDToInstanceSet = new HashMap<>(neo4jToRDFConverter.getIndexedSchema().getPropertyKeyIDToStr().size());
        }
        this.reifyRelationships = config.isReifyRelationships();
        this.relationshipTypeIDReificationBlacklist = getRelationshipTypeBlacklistSet();
    }

    private Set<Long> getRelationshipTypeBlacklistSet() {
        Map<String, Long> relationshipTypeToID = new UnifiedMap<>();
        Map<Long, String> relationshipTypeIDToStr = this.neo4jToRDFConverter.getIndexedSchema().getRelationshipTypeIDToStr();
        for (Map.Entry<Long, String> entry : relationshipTypeIDToStr.entrySet()) {
            relationshipTypeToID.put(entry.getValue(), entry.getKey());
        }
        return config.getRelationshipTypeReificationBlacklist().stream()
                .map(relationshipType -> {
                    Long typeID = relationshipTypeToID.get(relationshipType);
                    if (typeID == null) {
                        log.warn("Provided relationship type to blacklist for reification does not exist: %s".formatted(relationshipType));
                    }
                    return typeID;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(UnifiedSet::new));
    }

    @Override
    protected void process(long relationshipID,
                           long sourceID,
                           long targetID,
                           long typeID,
                           boolean statementHasAnnotations) {
        deployedRelationshipTypes.add(relationshipID);
        Statement statement = valueFactory.createStatement(
                neo4jToRDFMapper.nodeIDToResource(sourceID),
                neo4jToRDFMapper.relationshipTypeIDToIRI(typeID),
                neo4jToRDFMapper.nodeIDToResource(targetID),
                neo4jToRDFMapper.relationshipIDToResource(relationshipID)
        );
        if (reifyRelationships
            && (!config.isReifyOnlyRelationshipsWithProperties() || statementHasAnnotations)
            && (relationshipTypeIDReificationBlacklist.isEmpty() || !relationshipTypeIDReificationBlacklist.contains(typeID))) {
            neo4jToRDFConverter.processStatement(statement);
            neo4jToRDFMapper.statementToReificationTriples(statement, neo4jToRDFConverter::processStatement);
        } else {
            neo4jToRDFConverter.processStatement(statement);
        }

        if (relationshipIDToInstanceSet != null) {
            relationshipIDToInstanceSet.computeIfAbsent(typeID, ignore -> new Roaring64Bitmap())
                    .add(sourceID << 32 | targetID);
        }
    }

    @Override
    protected void process(long relationshipID, long propertyKeyID, Value value) {
        if (value.isSequenceValue()) {
            SequenceValue sequenceValue = (SequenceValue) value;
            neo4jToRDFMapper.sequenceValueToRDF(neo4jToRDFMapper.relationshipIDToResource(relationshipID),
                    propertyKeyID,
                    sequenceValue,
                    neo4jToRDFConverter::processStatement,
                    config.getSequenceConversionType());

            if (this.config.getSequenceConversionType().equals(SequenceConversionType.RDF_COLLECTION)) {
                objectPropertyKeys.add(propertyKeyID);
            } else {
                datatypePropertyKeys.add(propertyKeyID);
            }
        } else if (value instanceof PointValue) {
            objectPropertyKeys.add(propertyKeyID);

            neo4jToRDFMapper.pointPropertyToRDFStatements(neo4jToRDFMapper.relationshipIDToResource(relationshipID),
                    propertyKeyID,
                    (PointValue) value,
                    neo4jToRDFConverter::processStatement);
        } else {
            datatypePropertyKeys.add(propertyKeyID);

            Statement statement = valueFactory.createStatement(
                    neo4jToRDFMapper.relationshipIDToResource(relationshipID),
                    neo4jToRDFMapper.propertyKeyIDToResource(propertyKeyID),
                    Values.literal(valueFactory, value.asObject(), true)
            );
            neo4jToRDFConverter.processStatement(statement);
        }
    }

    public Map<Long, Roaring64Bitmap> getRelationshipIDToInstanceSet() {
        return relationshipIDToInstanceSet;
    }

    public Set<Long> getDeployedRelationshipTypes() {
        return deployedRelationshipTypes;
    }

    public Set<Long> getDatatypePropertyKeys() {
        return datatypePropertyKeys;
    }

    public Set<Long> getObjectPropertyKeys() {
        return objectPropertyKeys;
    }
}
