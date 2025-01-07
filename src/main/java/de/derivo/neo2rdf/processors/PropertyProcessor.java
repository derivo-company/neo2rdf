package de.derivo.neo2rdf.processors;

import org.neo4j.kernel.impl.store.PropertyType;
import org.neo4j.values.storable.Value;

public interface PropertyProcessor {


    void processNodeProperty(long nodeID,
                             long propertyKeyID,
                             PropertyType propertyType,
                             Value value);

    void processRelationshipProperty(long relationshipID,
                                     long propertyKeyID,
                                     PropertyType propertyType,
                                     Value value);

    void processSchemaRuleProperty(long schemaEntityID,
                                   long propertyKeyID,
                                   PropertyType propertyType,
                                   Value value);
}
