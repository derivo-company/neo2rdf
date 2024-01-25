# Neo4j to RDF Converter

The Neo4j-RDF-Converter represents an application that can be used in order to convert a Neo4j database into an RDF file
in Turtle format. It is implemented in Java and uses the official Neo4j record storage reader to iterate over all
entries of a Neo4j database to accomplish the conversion.

## Conversion Modes

There exist two distinct conversion modes:

- **DB-to-File**: The Neo4j database is converted into an RDF file in Turtle format, which is written to a specified
  location on disk.
- **DB-to-Stream**: The application starts an HTTP server for the provided Neo4j database. When a GET request is sent to
  the server, the conversion procedure is initiated and the response returns an RDF Turtle stream to the client.

## External Configuration YAML File

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