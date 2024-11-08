package de.derivo.neo2rdf.conversion;

import de.derivo.neo2rdf.util.ConsoleUtil;
import org.neo4j.configuration.Config;
import org.neo4j.dbms.archive.DumpFormatSelector;
import org.neo4j.dbms.archive.IncorrectFormat;
import org.neo4j.dbms.archive.Loader;
import org.neo4j.internal.id.DefaultIdGeneratorFactory;
import org.neo4j.io.fs.DefaultFileSystemAbstraction;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.io.layout.recordstorage.RecordDatabaseLayout;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.io.pagecache.context.CursorContextFactory;
import org.neo4j.io.pagecache.impl.SingleFilePageSwapperFactory;
import org.neo4j.io.pagecache.impl.muninn.MuninnPageCache;
import org.neo4j.io.pagecache.tracing.DefaultPageCacheTracer;
import org.neo4j.io.pagecache.tracing.PageCacheTracer;
import org.neo4j.kernel.impl.scheduler.JobSchedulerFactory;
import org.neo4j.kernel.impl.store.NeoStores;
import org.neo4j.kernel.impl.store.StoreFactory;
import org.neo4j.kernel.impl.transaction.log.LogTailLogVersionsMetadata;
import org.neo4j.logging.log4j.Log4jLogProvider;
import org.neo4j.memory.LocalMemoryTracker;
import org.neo4j.memory.MemoryTracker;
import org.neo4j.scheduler.JobScheduler;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

import static org.neo4j.index.internal.gbptree.RecoveryCleanupWorkCollector.immediate;

public class Neo4jStoreFactory {
    private static final Logger log = ConsoleUtil.getLogger();
    private static final LocalMemoryTracker localMemoryTracker = new LocalMemoryTracker();

    public static NeoStores getNeo4jStore(File neo4jDBDirectory) {
        log.info("Generating Neo4j store from database directory: " + neo4jDBDirectory);
        DefaultFileSystemAbstraction fileSystemAbstraction = new DefaultFileSystemAbstraction();
        NeoStores store = new StoreFactory(
                RecordDatabaseLayout.ofFlat(neo4jDBDirectory.toPath()),
                Config.newBuilder().build(),
                new DefaultIdGeneratorFactory(fileSystemAbstraction,
                        immediate(),
                        PageCacheTracer.NULL,
                        "neo4j-db"),
                getPageCache(fileSystemAbstraction),
                new DefaultPageCacheTracer(),
                fileSystemAbstraction,
                new Log4jLogProvider(System.out),
                CursorContextFactory.NULL_CONTEXT_FACTORY,
                true,
                LogTailLogVersionsMetadata.EMPTY_LOG_TAIL
        ).openAllNeoStores();
        log.info("Store successfully initialized.");
        return store;
    }


    public static NeoStores getNeo4jStoreFromDump(File neo4jDumpPath, File outputDirectory) {
        log.info("Extracting Neo4j database from dump: " + neo4jDumpPath);
        try {
            FileUtils.deleteDirectory(outputDirectory.toPath());
            DatabaseLayout databaseLayout = RecordDatabaseLayout.ofFlat(outputDirectory.toPath().toAbsolutePath());

            DefaultFileSystemAbstraction fileSystemAbstraction = new DefaultFileSystemAbstraction();
            Loader loader = new Loader(fileSystemAbstraction);

            loader.load(neo4jDumpPath.toPath(),
                    databaseLayout,
                    true,
                    true,
                    DumpFormatSelector::decompress);

            return getNeo4jStore(outputDirectory);
        } catch (IOException | IncorrectFormat e) {
            throw new RuntimeException(e);
        }
    }

    public static PageCache getPageCache(FileSystemAbstraction fileSystemAbstraction) {
        JobScheduler scheduler = JobSchedulerFactory.createInitialisedScheduler();

        return new MuninnPageCache(
                new SingleFilePageSwapperFactory(fileSystemAbstraction,
                        new DefaultPageCacheTracer(),
                        getDefaultMemoryTracker()),
                scheduler,
                MuninnPageCache.config(100)
        );
    }

    public static MemoryTracker getDefaultMemoryTracker() {
        return localMemoryTracker;
    }
}
