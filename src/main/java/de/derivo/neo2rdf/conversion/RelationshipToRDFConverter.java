package de.derivo.neo2rdf.conversion;

import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFMapper;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFValueFactory;
import de.derivo.neo2rdf.processors.Neo4jConnectorRelationshipProcessor;
import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import de.derivo.neo2rdf.util.ConsoleUtil;
import de.derivo.neo2rdf.util.SequenceConversionType;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.neo4j.driver.Value;
import org.neo4j.values.SequenceValue;
import org.neo4j.values.storable.PointValue;
import org.roaringbitmap.longlong.Roaring64Bitmap;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RelationshipToRDFConverter extends Neo4jConnectorRelationshipProcessor {
    private static final Logger log = ConsoleUtil.getLogger();

    private final Neo4jToRDFConverter neo4jToRDFConverter;
    private final Neo4jToRDFMapper neo4jToRDFMapper;
    private final ValueFactory valueFactory = new Neo4jToRDFValueFactory();

    private Set<String> deployedRelationshipTypes;
    private Set<String> datatypePropertyKeys;
    private Set<String> objectPropertyKeys;
    private final ConversionConfig config;

    private boolean reifyRelationships;
    private Set<String> relationshipTypeIDReificationBlacklist;

    private Map<Long, Roaring64Bitmap> relationshipIDToInstanceSet = null;
    private IndexedNeo4jSchema indexedNeo4jSchema;

    public RelationshipToRDFConverter(Neo4jDBConnector neo4jDBConnector, Neo4jToRDFConverter neo4jToRDFConverter, ConversionConfig config) {
        super(neo4jDBConnector);
        this.neo4jToRDFConverter = neo4jToRDFConverter;
        this.neo4jToRDFMapper = neo4jToRDFConverter.neo4jToRDFMapper;
        this.config = config;
        init();
    }

    private void init() {
        this.indexedNeo4jSchema = neo4jToRDFConverter.getIndexedSchema();
        this.deployedRelationshipTypes = new UnifiedSet<>(indexedNeo4jSchema.getRelationshipTypes().size());
        this.datatypePropertyKeys = new UnifiedSet<>(indexedNeo4jSchema.getPropertyKeys().size());
        this.objectPropertyKeys = new UnifiedSet<>(indexedNeo4jSchema.getPropertyKeys().size());

        if (this.config.isDerivePropertyHierarchyByRelationshipSubsetCheck()) {
            this.relationshipIDToInstanceSet = new HashMap<>(indexedNeo4jSchema.getPropertyKeys().size());
        }
        this.reifyRelationships = config.isReifyRelationships();
        this.relationshipTypeIDReificationBlacklist = getRelationshipTypeBlacklistSet();
    }

    private Set<String> getRelationshipTypeBlacklistSet() {
        return config.getRelationshipTypeReificationBlacklist().stream()
                .map(blackListedRelType -> {
                    if (!indexedNeo4jSchema.getRelationshipTypes().contains(blackListedRelType)) {
                        log.warn("Provided relationship type to blacklist for reification does not exist: %s".formatted(blackListedRelType));
                    }
                    return blackListedRelType;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(UnifiedSet::new));
    }

    @Override
    public void process(String relationshipID,
                        String sourceID,
                        String targetID,
                        String typeID,
                        Stream<Map.Entry<String, Value>> propertyValuePairs) {
        deployedRelationshipTypes.add(relationshipID);
        Statement statement = valueFactory.createStatement(
                neo4jToRDFMapper.nodeIDToResource(sourceID),
                neo4jToRDFMapper.relationshipTypeIDToIRI(typeID),
                neo4jToRDFMapper.nodeIDToResource(targetID),
                neo4jToRDFMapper.relationshipIDToResource(relationshipID)
        );
        if (reifyRelationships
            && !config.isReifyOnlyRelationshipsWithProperties()
            && (relationshipTypeIDReificationBlacklist.isEmpty() || !relationshipTypeIDReificationBlacklist.contains(typeID))) {
            neo4jToRDFConverter.processStatement(statement);
            neo4jToRDFMapper.statementToReificationTriples(statement, neo4jToRDFConverter::processStatement);
        } else {
            neo4jToRDFConverter.processStatement(statement);
        }

        if (relationshipIDToInstanceSet != null) {
            // TODO
            throw new IllegalStateException();
            //relationshipIDToInstanceSet.computeIfAbsent(typeID, ignore -> new Roaring64Bitmap()).add(sourceID << 32 | targetID);
        }

        propertyValuePairs.forEach(pair -> {
            process(relationshipID, pair.getKey(), pair.getValue());
        });
    }

    private void process(String relationshipID, String propertyKey, Value value) {
        if (value.type().name().equals("LIST")) { // TODO refactor
            SequenceValue sequenceValue = (SequenceValue) value;
            neo4jToRDFMapper.sequenceValueToRDF(neo4jToRDFMapper.relationshipIDToResource(relationshipID),
                    propertyKey,
                    sequenceValue,
                    neo4jToRDFConverter::processStatement,
                    config.getSequenceConversionType());

            if (this.config.getSequenceConversionType().equals(SequenceConversionType.RDF_COLLECTION)) {
                objectPropertyKeys.add(propertyKey);
            } else {
                datatypePropertyKeys.add(propertyKey);
            }
        } else if (value instanceof PointValue) {
            objectPropertyKeys.add(propertyKey);

            neo4jToRDFMapper.pointPropertyToRDFStatements(neo4jToRDFMapper.relationshipIDToResource(relationshipID),
                    propertyKey,
                    (PointValue) value,
                    neo4jToRDFConverter::processStatement);
        } else {
            datatypePropertyKeys.add(propertyKey);

            Statement statement = valueFactory.createStatement(
                    neo4jToRDFMapper.relationshipIDToResource(relationshipID),
                    neo4jToRDFMapper.propertyKeyIDToResource(propertyKey),
                    Values.literal(valueFactory, value.asObject(), true)
            );
            neo4jToRDFConverter.processStatement(statement);
        }
    }

    public Map<Long, Roaring64Bitmap> getRelationshipIDToInstanceSet() {
        return relationshipIDToInstanceSet;
    }

    public Set<String> getDeployedRelationshipTypes() {
        return deployedRelationshipTypes;
    }

    public Set<String> getDatatypePropertyKeys() {
        return datatypePropertyKeys;
    }

    public Set<String> getObjectPropertyKeys() {
        return objectPropertyKeys;
    }
}
