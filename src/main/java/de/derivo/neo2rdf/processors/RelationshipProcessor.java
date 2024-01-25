package de.derivo.neo2rdf.processors;

import de.derivo.neo2rdf.util.ConsoleUtil;
import org.neo4j.internal.recordstorage.RecordRelationshipScanCursor;
import org.neo4j.internal.recordstorage.RecordStorageReader;
import org.neo4j.io.pagecache.context.CursorContext;
import org.neo4j.kernel.impl.store.NeoStores;
import org.neo4j.kernel.impl.store.cursor.CachedStoreCursors;
import org.neo4j.memory.LocalMemoryTracker;
import org.neo4j.storageengine.api.PropertySelection;
import org.neo4j.storageengine.api.StoragePropertyCursor;
import org.neo4j.storageengine.api.cursor.StoreCursors;
import org.neo4j.values.storable.Value;
import org.slf4j.Logger;

public abstract class RelationshipProcessor {

    private final Logger log = ConsoleUtil.getLogger();
    private final NeoStores neoStores;
    private final StoreCursors storeCursors;

    private long processedRelationships = 0;
    private final long PROGRESS_MESSAGE_AFTER_X_RELATIONSHIPS = 100_000;

    public RelationshipProcessor(NeoStores neoStores) {
        this.neoStores = neoStores;
        this.storeCursors = new CachedStoreCursors(neoStores, CursorContext.NULL_CONTEXT);
    }

    public void startProcessing() {
        RecordStorageReader recordStorageReader = new RecordStorageReader(neoStores);

        RecordRelationshipScanCursor relCursor = recordStorageReader.allocateRelationshipScanCursor(CursorContext.NULL_CONTEXT,
                storeCursors);
        relCursor.scan();
        StoragePropertyCursor propertyCursor = recordStorageReader.allocatePropertyCursor(CursorContext.NULL_CONTEXT,
                storeCursors,
                new LocalMemoryTracker());
        while (relCursor.next()) {
            long relationshipID = relCursor.getId();
            long sourceID = relCursor.getFirstNode();
            long targetID = relCursor.getSecondNode();
            long relationshipTypeID = relCursor.getType();

            boolean hasProperties = relCursor.hasProperties();
            process(relationshipID, sourceID, targetID, relationshipTypeID, hasProperties);

            if (!hasProperties) {
                continue;
            }

            relCursor.properties(propertyCursor, PropertySelection.ALL_PROPERTIES);
            while (propertyCursor.next()) {
                process(relationshipID,
                        propertyCursor.propertyKey(),
                        propertyCursor.propertyValue()
                );
            }

            processedRelationships++;
            if (processedRelationships % PROGRESS_MESSAGE_AFTER_X_RELATIONSHIPS == 0) {
                log.info("Processed %s relationships.".formatted(ConsoleUtil.formatDecimal(
                        processedRelationships)));
            }
        }
    }

    protected abstract void process(long relationshipID,
                                    long sourceID,
                                    long targetID,
                                    long typeID,
                                    boolean statementHasAnnotations);

    protected abstract void process(long relationshipID, long propertyKey, Value value);
}
