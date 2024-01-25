package de.derivo.neo4jconverter.processors;

import de.derivo.neo4jconverter.util.ConsoleUtil;
import org.neo4j.internal.recordstorage.RecordNodeCursor;
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

public abstract class NodeProcessor {

    private final Logger log = ConsoleUtil.getLogger();
    private long processedNodes = 0;
    private final long PROGRESS_MESSAGE_AFTER_X_NODES = 100_000;
    private final NeoStores neoStores;
    private final StoreCursors storeCursors;

    public NodeProcessor(NeoStores neoStores) {
        this.neoStores = neoStores;
        this.storeCursors = new CachedStoreCursors(neoStores, CursorContext.NULL_CONTEXT);
    }


    public void startProcessing() {
        RecordStorageReader recordStorageReader = new RecordStorageReader(neoStores);
        RecordNodeCursor nodeCursor = recordStorageReader.allocateNodeCursor(CursorContext.NULL_CONTEXT, storeCursors);
        nodeCursor.scan();
        StoragePropertyCursor propertyCursor = recordStorageReader.allocatePropertyCursor(CursorContext.NULL_CONTEXT,
                storeCursors,
                new LocalMemoryTracker());
        while (nodeCursor.next()) {
            long nodeID = nodeCursor.getId();
            for (long labelID : nodeCursor.labels()) {
                process(nodeID, labelID);
            }

            nodeCursor.properties(propertyCursor, PropertySelection.ALL_PROPERTIES);
            while (propertyCursor.next()) {
                process(nodeID,
                        propertyCursor.propertyKey(),
                        propertyCursor.propertyValue()
                );
            }

            processedNodes++;
            if (processedNodes % PROGRESS_MESSAGE_AFTER_X_NODES == 0) {
                log.info("Processed %s nodes.".formatted(ConsoleUtil.formatDecimal(
                        processedNodes)));
            }
        }
    }

    protected abstract void process(long nodeID, long assignedLabelID);

    protected abstract void process(long nodeID, long propertyKeyID, Value value);
}
