# Neo2RDF

Neo2RDF is a command line application that converts a Neo4j database into an RDF file in Turtle format. It is
implemented in Java and uses the official Neo4j record storage reader.

## Installation & Quickstart

Neo2RDF requires Java 17 or higher. To get started, download the latest zip file from
the [releases page](https://github.com/derivo-company/neo2rdf/releases), which contains the executable JAR with all
dependencies as well as Bash and Batch startup scripts. Unpack the archive to your preferred location and add
the directory to your PATH environment variable in case you want to invoke the Neo2RDF scripts from any location.

The command line application can be invoked as follows:

```
./neo2rdf.sh
```

The execution of the script will print general information on the application and an exemplary usage of the 2 available
conversion modes.

### Conversion Modes

Two conversion modes are available:

- **DB-to-File**: A provided Neo4j database is converted into an RDF file in Turtle format, which is written to a
  specified
  location on disk. \
  **Example:**
  ```
  ./neo2rdf.sh dump -db=./path/to/neo4jdb -o=output/path/data.ttl
  ```

- **DB-to-Stream**: The application starts an HTTP server for the provided Neo4j database. When a GET request is sent to
  the server (e.g., to `http://localhost:8080`), the conversion procedure is initiated and the response returns an RDF
  Turtle stream to the client. \
  **Example:**
  ```
  ./neo2rdf.sh server -db=./path/to/neo4jdb -p=8080
  ```

To get more information on the available configuration parameters execute `./neo2rdf.sh help dump`
or `./neo2rdf.sh help server`.

## Advanced Configuration

An advanced configuration can be either provided in terms of additional command line parameters or by using an external
YAML configuration. For the sake of readability, we describe all available parameters with their default values in the
following using the external YAML configuration format:

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