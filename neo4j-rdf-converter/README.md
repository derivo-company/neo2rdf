# Neo4j to RDF Converter

## File-to-File Conversion

## File-to-Stream Conversion

## Configuration YAML File
```YAML
# Prefix that is used for all converted nodes, property keys, relationship types, and relationships.
basePrefix: "https://www.example.org#"

# The reification vocabulary defines how a quadruple (sbj, pred, obj, statementID) should be reified in RDF. 
# Options: 
# - RDF_REIFICATION: uses rdf:Subject, rdf:Predicate, rdf:Object to reify triples and rdf:Statement as statement type. 
# - OWL_REIFICATION: uses owl:annotatedSource, owl:annotatedProperty, owl:annotatedTarget to reify triples and owl:Axiom as statement type.
reificationVocabulary: "OWL_REIFICATION"

# Options: 
# - RDF_COLLECTION: Neo4j sequences are converted into open lists in RDF.
# - SEPARATE_LITERALS: Neo4j sequences are converted into separate assertions, e.g., (x { has: [1, 2] }) is converted to (:x, :has, 1) and (:x, :has, 2).
sequenceConversionType: "SEPARATE_LITERALS"

includeDeletedNeo4jLabels: false
includeDeletedPropertyKeys: false
includeDeletedRelationshipTypes: false

# Indicates whether the RDF class hierarchy should be derived.
# For this purpose, it is examined which sets of Neo4j nodes with an assigned label are a subset of one another.   
deriveClassHierarchyByLabelSubsetCheck: true

# Indicates whether the RDF property hierarchy should be derived.
# For this, the node-node combinations for each relationship type are initially collected in a set.
# Subsequently, for every pair of sets, it is examined whether they are a subset of each other.
derivePropertyHierarchyByRelationshipSubsetCheck: true

# If the RDF schema is derived from the Neo4j dataset, e.g., the class or property hierarchy,
# an additional path can be specified to store it separately on disk.
# If the value is null or left out, the derived schema is stored along with the data.   
schemaOutputPath: null
```