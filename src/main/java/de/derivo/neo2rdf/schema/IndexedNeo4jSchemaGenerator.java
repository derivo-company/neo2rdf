package de.derivo.neo2rdf.schema;

import de.derivo.neo2rdf.conversion.Neo4jStoreFactory;
import org.neo4j.internal.helpers.collection.Visitor;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.io.pagecache.context.CursorContext;
import org.neo4j.kernel.impl.store.LabelTokenStore;
import org.neo4j.kernel.impl.store.NeoStores;
import org.neo4j.kernel.impl.store.PropertyKeyTokenStore;
import org.neo4j.kernel.impl.store.RelationshipTypeTokenStore;
import org.neo4j.kernel.impl.store.cursor.CachedStoreCursors;
import org.neo4j.kernel.impl.store.record.LabelTokenRecord;
import org.neo4j.kernel.impl.store.record.PropertyKeyTokenRecord;
import org.neo4j.kernel.impl.store.record.RelationshipTypeTokenRecord;
import org.neo4j.memory.MemoryTracker;
import org.neo4j.storageengine.api.cursor.StoreCursors;

import java.util.HashMap;
import java.util.Map;

public class IndexedNeo4jSchemaGenerator {

    private final NeoStores neoStores;
    private final StoreCursors storeCursors;
    private final Map<Long, String> labelIDToLabel = new HashMap<>();
    private final Map<Long, String> propertyKeyToProperty = new HashMap<>();
    private final Map<Long, String> relationshipTypeIDToRelationship = new HashMap<>();
    private final MemoryTracker localMemoryTracker = Neo4jStoreFactory.getDefaultMemoryTracker();

    public IndexedNeo4jSchemaGenerator(NeoStores neoStores) {
        this.neoStores = neoStores;
        this.storeCursors = new CachedStoreCursors(neoStores, CursorContext.NULL_CONTEXT);
    }

    public IndexedNeo4jSchema generate() {
        initLabelIndex();
        initPropertyKeyIndex();
        initRelationshipIndex();
        return new IndexedNeo4jSchema(labelIDToLabel, propertyKeyToProperty, relationshipTypeIDToRelationship);
    }

    private void initLabelIndex() {
        LabelTokenStore labelTokenStore = neoStores.getLabelTokenStore();
        PageCursor cursor = labelTokenStore.openPageCursorForReading(0, CursorContext.NULL_CONTEXT);
        Visitor<LabelTokenRecord, RuntimeException> visitor = record -> {
            if (!record.inUse()) {
                return false;
            }
            String stringVal = labelTokenStore.getStringFor(record, storeCursors, this.localMemoryTracker);
            this.labelIDToLabel.put(record.getId(), stringVal);
            return false; // return true to stop processing
        };
        labelTokenStore.scanAllRecords(visitor, cursor, this.localMemoryTracker);
    }

    private void initRelationshipIndex() {
        RelationshipTypeTokenStore relationshipTypeTokenStore = neoStores.getRelationshipTypeTokenStore();
        PageCursor cursor = relationshipTypeTokenStore.openPageCursorForReading(0, CursorContext.NULL_CONTEXT);
        Visitor<RelationshipTypeTokenRecord, RuntimeException> visitor = record -> {
            if (!record.inUse()) {
                return false;
            }
            String stringVal = relationshipTypeTokenStore.getStringFor(record, storeCursors, this.localMemoryTracker);
            this.relationshipTypeIDToRelationship.put(record.getId(), stringVal);
            return false; // return true to stop processing
        };
        relationshipTypeTokenStore.scanAllRecords(visitor, cursor, this.localMemoryTracker);
    }

    private void initPropertyKeyIndex() {
        PropertyKeyTokenStore propertyKeyTokenStore = neoStores.getPropertyKeyTokenStore();
        PageCursor cursor = propertyKeyTokenStore.openPageCursorForReading(0, CursorContext.NULL_CONTEXT);
        Visitor<PropertyKeyTokenRecord, RuntimeException> visitor = record -> {
            if (!record.inUse()) {
                return false;
            }
            String stringVal = propertyKeyTokenStore.getStringFor(record, storeCursors, this.localMemoryTracker);
            this.propertyKeyToProperty.put(record.getId(), stringVal);
            return false; // return true to stop processing
        };
        propertyKeyTokenStore.scanAllRecords(visitor, cursor, this.localMemoryTracker);
    }
}
