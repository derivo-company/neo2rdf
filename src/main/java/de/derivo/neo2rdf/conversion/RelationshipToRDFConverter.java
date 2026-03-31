package de.derivo.neo2rdf.conversion;

/*-
 * #%L
 * neo2rdf
 * %%
 * Copyright (C) 2026 Derivo Company
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFMapper;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFValueFactory;
import de.derivo.neo2rdf.processors.Neo4jConnectorRelationshipProcessor;
import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.neo4j.driver.Value;
import org.roaringbitmap.longlong.Roaring64Bitmap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RelationshipToRDFConverter extends Neo4jConnectorRelationshipProcessor implements RDFPropertyProcessor {
    private final Neo4jToRDFConverter neo4jToRDFConverter;
    private final Neo4jToRDFMapper neo4jToRDFMapper;
    private final ValueFactory valueFactory = new Neo4jToRDFValueFactory();

    private Set<String> deployedRelationshipTypes;
    private Set<String> datatypePropertyKeys;
    private Set<String> objectPropertyKeys;
    private final ConversionConfig config;

    private boolean reifyRelationships;
    private Set<String> relationshipTypeIDReificationBlacklist;

    private Map<String, Roaring64Bitmap> relationshipIDToInstanceSet = null;

    public RelationshipToRDFConverter(Neo4jDBConnector neo4jDBConnector, Neo4jToRDFConverter neo4jToRDFConverter,
                                      ConversionConfig config) {
        super(neo4jDBConnector);
        this.neo4jToRDFConverter = neo4jToRDFConverter;
        this.neo4jToRDFMapper = neo4jToRDFConverter.neo4jToRDFMapper;
        this.config = config;
        init();
    }

    private void init() {
        int initialCapacity = 10_000;
        this.deployedRelationshipTypes = new UnifiedSet<>(initialCapacity);
        this.datatypePropertyKeys = new UnifiedSet<>(initialCapacity);
        this.objectPropertyKeys = new UnifiedSet<>(initialCapacity);

        if (this.config.isDerivePropertyHierarchyByRelationshipSubsetCheck()) {
            this.relationshipIDToInstanceSet = new HashMap<>(initialCapacity);
        }
        this.reifyRelationships = config.isReifyRelationships();
        this.relationshipTypeIDReificationBlacklist = getRelationshipTypeBlacklistSet();
    }

    private Set<String> getRelationshipTypeBlacklistSet() {
        return config.getRelationshipTypeReificationBlacklist().stream()
                .collect(Collectors.toCollection(UnifiedSet::new));
    }

    @Override
    public void process(String relationshipID,
                        String sourceID,
                        String targetID,
                        String relationshipType,
                        Map<String, Value> propertyValuePairs) {

        deployedRelationshipTypes.add(relationshipType);

        // create the main statement (s - p - o)
        Resource subject = neo4jToRDFMapper.nodeIDToResource(sourceID);
        IRI predicate = neo4jToRDFMapper.relationshipTypeToIRI(relationshipType);
        Resource object = neo4jToRDFMapper.nodeIDToResource(targetID);
        Resource relResource = neo4jToRDFMapper.relationshipIDToResource(relationshipID);

        Statement statement = valueFactory.createStatement(subject, predicate, object, relResource);

        // process statement & reification
        neo4jToRDFConverter.processStatement(statement);

        if (shouldReify(relationshipType, propertyValuePairs)) {
            neo4jToRDFMapper.statementToReificationTriples(statement, neo4jToRDFConverter::processStatement);
        }

        // track instances
        trackRelationshipInstance(relationshipType, sourceID, targetID);

        // process properties (delegating to the unified handler)
        propertyValuePairs.forEach((key, value) -> processProperty(relResource, key, value));
    }

    private boolean shouldReify(String relationshipType, Map<String, Value> properties) {
        if (!reifyRelationships) {
            return false;
        }

        // check config: reify only if it has properties?
        if (config.isReifyOnlyRelationshipsWithProperties() && properties.isEmpty()) {
            return false;
        }

        // check blacklist
        return relationshipTypeIDReificationBlacklist.isEmpty()
               || !relationshipTypeIDReificationBlacklist.contains(relationshipType);
    }

    private void trackRelationshipInstance(String relationshipType, String sourceID, String targetID) {
        if (relationshipIDToInstanceSet == null) return;

        long sourceVal = Long.parseLong(sourceID);
        long targetVal = Long.parseLong(targetID);

        relationshipIDToInstanceSet
                .computeIfAbsent(relationshipType, k -> new Roaring64Bitmap())
                .add(sourceVal << 32 | targetVal);
    }

    public Map<String, Roaring64Bitmap> getRelationshipIDToInstanceSet() {
        return relationshipIDToInstanceSet;
    }

    public Set<String> getDeployedRelationshipTypes() {
        return deployedRelationshipTypes;
    }

    public Set<String> getDatatypePropertyKeys() {
        return datatypePropertyKeys;
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
}
