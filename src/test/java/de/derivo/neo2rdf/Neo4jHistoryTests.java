package de.derivo.neo2rdf;

import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.config.ConversionConfigBuilder;
import de.derivo.neo2rdf.store.RDFStoreTestExtension;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Set;


public class Neo4jHistoryTests {

    @RegisterExtension
    private static final RDFStoreTestExtension storeTestExtension = new RDFStoreTestExtension(TestUtil.getResource("neo4j-history"));

    @Test
    public void includeDeletedLabelsPropertyKeysAndRelationshipTypes() {
        boolean include = true;
        ConversionConfig config = ConversionConfigBuilder.newBuilder()
                .setIncludeDeletedNeo4jLabels(include)
                .setIncludeDeletedPropertyKeys(include)
                .setIncludeDeletedRelationshipTypes(include)
                .build();
        storeTestExtension.convertAndImportIntoStore("neo4j-store-with-history.ttl", config);

        Set<String> assignedClasses = storeTestExtension.getAssignedClasses(config.getBasePrefix() + "node-0");
        System.out.println(assignedClasses);
        Set<String> expected = Set.of(config.getBasePrefix() + "Label1",
                config.getBasePrefix() + "Label2",
                config.getBasePrefix() + "Label3");
        Assertions.assertEquals(expected, assignedClasses);

        Set<String> allClasses = storeTestExtension.getInstances(OWL.CLASS.toString());
        Assertions.assertTrue(allClasses.contains(config.getBasePrefix() + "RemovedLabel"));

        Set<String> allProperties = storeTestExtension.getAllProperties();
        Assertions.assertTrue(allProperties.contains(config.getBasePrefix() + "DELETED_RELATION"));
        Assertions.assertTrue(allProperties.contains(config.getBasePrefix() + "removedAnnotationPropertyKey"));
        Assertions.assertTrue(allProperties.contains(config.getBasePrefix() + "removedPropertyKey"));
    }


    @Test
    public void excludeDeletedLabelsPropertyKeysAndRelationshipTypes() {
        boolean include = false;
        ConversionConfig config = ConversionConfigBuilder.newBuilder()
                .setIncludeDeletedNeo4jLabels(include)
                .setIncludeDeletedPropertyKeys(include)
                .setIncludeDeletedRelationshipTypes(include)
                .build();
        storeTestExtension.convertAndImportIntoStore("neo4j-store-without-history.ttl", config);

        Set<String> allClasses = storeTestExtension.getInstances(OWL.CLASS.toString());
        Assertions.assertFalse(allClasses.contains(config.getBasePrefix() + "RemovedLabel"));

        Set<String> allProperties = storeTestExtension.getAllProperties();
        Assertions.assertFalse(allProperties.contains(config.getBasePrefix() + "DELETED_RELATION"));
        Assertions.assertFalse(allProperties.contains(config.getBasePrefix() + "removedAnnotationPropertyKey"));
        Assertions.assertFalse(allProperties.contains(config.getBasePrefix() + "removedPropertyKey"));
    }
}
