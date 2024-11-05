package de.derivo.neo2rdf.processors;

import de.derivo.neo2rdf.conversion.Neo4jStoreFactory;
import org.neo4j.internal.helpers.collection.Visitor;
import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.io.pagecache.context.CursorContext;
import org.neo4j.kernel.impl.store.NeoStores;
import org.neo4j.kernel.impl.store.PropertyStore;
import org.neo4j.kernel.impl.store.PropertyType;
import org.neo4j.kernel.impl.store.cursor.CachedStoreCursors;
import org.neo4j.kernel.impl.store.record.PropertyBlock;
import org.neo4j.kernel.impl.store.record.PropertyRecord;
import org.neo4j.memory.MemoryTracker;
import org.neo4j.storageengine.api.cursor.StoreCursors;
import org.neo4j.values.storable.Value;

public abstract class PropertyProcessor {
    private final NeoStores neoStores;
    private PropertyStore propertyStore;
    private final StoreCursors storeCursors;
    private final MemoryTracker memoryTracker = Neo4jStoreFactory.getDefaultMemoryTracker();

    public PropertyProcessor(NeoStores neoStores) {
        this.neoStores = neoStores;
        this.storeCursors = new CachedStoreCursors(neoStores, CursorContext.NULL_CONTEXT);
    }

    public void startProcessing() {
        propertyStore = neoStores.getPropertyStore();
        PageCursor cursor = propertyStore.openPageCursorForReading(0, CursorContext.NULL_CONTEXT);
        Visitor<PropertyRecord, RuntimeException> visitor = record -> {
            long entityID = record.getEntityId();

            for (PropertyBlock propertyBlock : record.propertyBlocks()) {
                PropertyType propertyType = propertyBlock.getType();
                Value value = propertyStore.getValue(propertyBlock, storeCursors, memoryTracker);
                if (record.isRelSet()) {
                    processRelationshipProperty(entityID, propertyBlock.getKeyIndexId(), propertyType, value);
                } else if (record.isNodeSet()) {
                    processNodeProperty(entityID, propertyBlock.getKeyIndexId(), propertyType, value);
                } else if (record.isSchemaSet()) {
                    processSchemaRuleProperty(entityID, propertyBlock.getKeyIndexId(), propertyType, value);
                }
            }
            return false; // return true to stop processing
        };
        propertyStore.scanAllRecords(visitor, cursor, memoryTracker);

    }

    protected abstract void processNodeProperty(long nodeID,
                                                long propertyKeyID,
                                                PropertyType propertyType,
                                                Value value);

    protected abstract void processRelationshipProperty(long relationshipID,
                                                        long propertyKeyID,
                                                        PropertyType propertyType,
                                                        Value value);

    protected abstract void processSchemaRuleProperty(long schemaEntityID,
                                                      long propertyKeyID,
                                                      PropertyType propertyType,
                                                      Value value);
}
