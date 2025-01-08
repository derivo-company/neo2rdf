package de.derivo.neo2rdf.conversion;

import de.derivo.neo2rdf.processors.Neo4jDBConnector;
import de.derivo.neo2rdf.util.ConsoleUtil;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Set;

@SuppressWarnings("CanBeFinal")
public class IndexedNeo4jSchema {
    protected Logger log = ConsoleUtil.getLogger();

    private final Neo4jDBConnector connector;

    private Set<String> propertyKeys = new UnifiedSet<>(1_000);
    private Set<String> relationshipTypes = new UnifiedSet<>(1_000);
    private Set<String> neo4jLabels = new UnifiedSet<>(10_000);

    public IndexedNeo4jSchema(Neo4jDBConnector connector) {
        this.connector = connector;
        init();
    }

    private void init() {
        log.info("Indexing schema of dataset...");
        this.connector.query("CALL db.labels() YIELD label RETURN label;",
                records -> records.forEach(r -> neo4jLabels.add(r.get("label").asString())));
        this.connector.query("CALL db.propertyKeys() YIELD propertyKey RETURN propertyKey;",
                records -> records.forEach(r -> propertyKeys.add(r.get("propertyKey").asString())));
        this.connector.query("CALL db.relationshipTypes() YIELD relationshipType RETURN relationshipType;",
                records -> records.forEach(r -> relationshipTypes.add(r.get("relationshipType").asString())));
        neo4jLabels = Collections.unmodifiableSet(neo4jLabels);
        propertyKeys = Collections.unmodifiableSet(propertyKeys);
        relationshipTypes = Collections.unmodifiableSet(relationshipTypes);
        log.info("Schema successfully indexed.");
    }


    public Set<String> getPropertyKeys() {
        return propertyKeys;
    }

    public Set<String> getRelationshipTypes() {
        return relationshipTypes;
    }

    public Set<String> getNeo4jLabels() {
        return neo4jLabels;
    }
}
