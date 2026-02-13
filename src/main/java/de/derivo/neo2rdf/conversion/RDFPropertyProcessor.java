package de.derivo.neo2rdf.conversion;

import de.derivo.neo2rdf.conversion.config.ConversionConfig;
import de.derivo.neo2rdf.conversion.model.Neo4jToRDFMapper;
import de.derivo.neo2rdf.util.Neo4jValueUtil;
import de.derivo.neo2rdf.util.SequenceConversionType;
import de.derivo.neo2rdf.util.VectorConversionType;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.Values;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Vector;

import java.util.List;
import java.util.Set;

public interface RDFPropertyProcessor {

    Neo4jToRDFMapper getMapper();

    ConversionConfig getConfig();

    ValueFactory getValueFactory();

    Neo4jToRDFConverter getConverter();

    Set<String> getObjectPropertyKeys();

    Set<String> getDatatypePropertyKeys();

    default void processProperty(Resource subjectResource, String key, Value value) {
        boolean isObjectProperty;

        // handle sequence (list)
        if (Neo4jValueUtil.isList(value)) {
            List<Object> listValue = value.asList();
            getMapper().sequenceValueToRDF(subjectResource, key, listValue,
                    getConverter()::processStatement,
                    getConfig().getSequenceConversionType());

            isObjectProperty = getConfig().getSequenceConversionType() == SequenceConversionType.RDF_COLLECTION;

            // handle vector
        } else if (Neo4jValueUtil.isVector(value)) {
            Vector vector = value.asVector();
            getMapper().vectorValueToRDF(subjectResource, key, vector,
                    getConverter()::processStatement,
                    getConfig().getVectorConversionType());

            isObjectProperty = getConfig().getVectorConversionType() == VectorConversionType.RDF_COLLECTION;

            // handle spatial point
        } else if (Neo4jValueUtil.isPoint(value)) {
            getMapper().pointPropertyToRDFStatements(subjectResource, key, value.asPoint(),
                    getConverter()::processStatement);
            isObjectProperty = true;

            // handle scalar (standard literal)
        } else {
            Statement statement = getValueFactory().createStatement(
                    subjectResource,
                    getMapper().propertyKeyToResource(key),
                    Values.literal(getValueFactory(), value.asObject(), true));
            getConverter().processStatement(statement);
            isObjectProperty = false;
        }

        // register property type
        if (isObjectProperty) {
            getObjectPropertyKeys().add(key);
        } else {
            getDatatypePropertyKeys().add(key);
        }
    }
}