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

To get more information on the available configuration parameters read the subsequent section or
execute `./neo2rdf.sh help <COMMAND>`.

## Neo2RDF CLI Manual

```
NAME
       neo2rdf - Converts a Neo4j database into an RDF file in Turtle format.

SYNOPSIS
       neo2rdf [-hV] [COMMAND]

DESCRIPTION
       Converts a Neo4j database into an RDF file in Turtle format.

OPTIONS
       -h, --help
	   Show this help message and exit.

       -V, --version
	   Print version information and exit.

COMMANDS
       dump
	   The Neo4j database is converted into an RDF file in Turtle format, which is written to the specified
	   location on disk.

	   Exemplary usage: dump -db=./path/to/neo4jdb -o=output/path/data.ttl

       server
	   The application starts an HTTP server for the provided Neo4j database. When a GET request is sent to the
	   server, the conversion procedure is initiated and the response returns an RDF Turtle stream to the client.

	   Exemplary usage: server -db=./path/to/neo4jdb -p=8080

       help
	   Display help information about the specified command.
```

### DB-to-File - Dump Command

```
NAME
       neo2rdf-dump - The Neo4j database is converted into an RDF file in Turtle format, which is written to the
       specified location on disk. Exemplary usage: `dump -db=./path/to/neo4jdb -o=output/path/data.ttl`

SYNOPSIS
       neo2rdf dump [--deriveClassHierarchyByLabelSubsetCheck] [--derivePropertyHierarchyByRelationshipSubsetCheck]
       [--includeDeletedNeo4jLabels] [--includeDeletedPropertyKeys] [--includeDeletedRelationshipTypes]
       [--basePrefix=<basePrefix>] [-cfg=<conversionConfigFile>] [-d=<neo4jDBDumpPath>] -db=<neo4jDBDirectory>
       -o=<outputPath> [--reificationVocabulary=<reificationVocabulary>] [--schemaOutputPath=<schemaOutputPath>]
       [--sequenceConversionType=<sequenceConversionType>]

DESCRIPTION
       The Neo4j database is converted into an RDF file in Turtle format, which is written to the specified location
       on disk. Exemplary usage: dump -db=./path/to/neo4jdb -o=output/path/data.ttl

OPTIONS
       --basePrefix=<basePrefix>
	   Prefix that is used for all converted nodes, property keys, relationship types, and relationships.

       -cfg, --config=<conversionConfigFile>
	   Instead of specifying non-mandatory options in terms of command line parameters, it is also possible to
	   use an external YAML configuration. The YAML keys must have the same identifiers as the long option names, 
	   e.g., 'basePrefix: https://www.example.org/other-prefix#'.

       -d, --neo4jDBDumpPath=<neo4jDBDumpPath>
	   If a path to a Neo4j dump has been specified using this parameter, the DB dump is extracted to the
	   appropriate target

	   Neo4j DB directory first, and subsequently, the conversion procedure gets executed as usual.

       -db, --neo4jDBDirectory=<neo4jDBDirectory>, --deriveClassHierarchyByLabelSubsetCheck
	   Indicates whether the RDF class hierarchy should be derived.

	   For this purpose, it is examined which sets of Neo4j nodes with an assigned label are a subset of one
	   another.

       --derivePropertyHierarchyByRelationshipSubsetCheck
	   Indicates whether the RDF property hierarchy should be derived.

	   For this, the node-node combinations for each relationship type are initially collected in a set.

	   Subsequently, for every pair of sets, it is examined whether they are a subset of each other.

       --includeDeletedNeo4jLabels, --includeDeletedPropertyKeys, --includeDeletedRelationshipTypes, -o,
       --outputPath=<outputPath>, --reificationVocabulary=<reificationVocabulary>
	   The reification vocabulary defines how a quadruple (sbj, pred, obj, statementID) should be reified in RDF.

	   Options:

	   •   RDF_COLLECTION: Neo4j sequences are converted into open lists in RDF.

	   •   SEPARATE_LITERALS: Neo4j sequences are converted into separate assertions, e.g., (x { has: [1, 2] })
	       is converted to (:x, :has, 1) and (:x, :has, 2).

       --schemaOutputPath=<schemaOutputPath>
	   If the RDF schema is derived from the Neo4j dataset, e.g., the class or property hierarchy,

	   an additional path can be specified to store it separately on disk.

	   If the value is null or left out, the derived schema is stored along with the data.

       --sequenceConversionType=<sequenceConversionType>
	   Options:

	   •   RDF_COLLECTION: Neo4j sequences are converted into open lists in RDF.

	   •   SEPARATE_LITERALS: Neo4j sequences are converted into separate assertions, e.g., (x { has: [1, 2] })
	       is converted to (:x, :has, 1) and (:x, :has, 2).
```

### DB-to-stream - Server Command

```
NAME
       neo2rdf-server - The application starts an HTTP server for the provided Neo4j database. When a GET request is
       sent to the server, the conversion procedure is initiated and the response returns an RDF Turtle stream to the
       client. Exemplary usage: `server -db=./path/to/neo4jdb -p=8080`

SYNOPSIS
       neo2rdf server [--deriveClassHierarchyByLabelSubsetCheck] [--derivePropertyHierarchyByRelationshipSubsetCheck]
       [--includeDeletedNeo4jLabels] [--includeDeletedPropertyKeys] [--includeDeletedRelationshipTypes]
       [--basePrefix=<basePrefix>] [-cfg=<conversionConfigFile>] [-d=<neo4jDBDumpPath>] -db=<neo4jDBDirectory>
       -p=<port> [--reificationVocabulary=<reificationVocabulary>] [--schemaOutputPath=<schemaOutputPath>]
       [--sequenceConversionType=<sequenceConversionType>] [-t=<numberOfServerThreads>]

DESCRIPTION
       The application starts an HTTP server for the provided Neo4j database. When a GET request is sent to the
       server, the conversion procedure is initiated and the response returns an RDF Turtle stream to the client.
       Exemplary usage: server -db=./path/to/neo4jdb -p=8080

OPTIONS
       --basePrefix=<basePrefix>
	   Prefix that is used for all converted nodes, property keys, relationship types, and relationships.

       -cfg, --config=<conversionConfigFile>
	   Instead of specifying non-mandatory options in terms of command line parameters, it is also possible to
	   use an external YAML configuration. The YAML keys must have the same identifiers as the long option names, 
	   e.g., 'basePrefix: https://www.example.org/other-prefix#'.

       -d, --neo4jDBDumpPath=<neo4jDBDumpPath>
	   If a path to a Neo4j dump has been specified using this parameter, the DB dump is extracted to the
	   appropriate target

	   Neo4j DB directory first, and subsequently, the conversion procedure gets executed as usual.

       -db, --neo4jDBDirectory=<neo4jDBDirectory>, --deriveClassHierarchyByLabelSubsetCheck
	   Indicates whether the RDF class hierarchy should be derived.

	   For this purpose, it is examined which sets of Neo4j nodes with an assigned label are a subset of one
	   another.

       --derivePropertyHierarchyByRelationshipSubsetCheck
	   Indicates whether the RDF property hierarchy should be derived.

	   For this, the node-node combinations for each relationship type are initially collected in a set.

	   Subsequently, for every pair of sets, it is examined whether they are a subset of each other.

       --includeDeletedNeo4jLabels, --includeDeletedPropertyKeys, --includeDeletedRelationshipTypes, -p,
       --port=<port>, --reificationVocabulary=<reificationVocabulary>
	   The reification vocabulary defines how a quadruple (sbj, pred, obj, statementID) should be reified in RDF.

	   Options:

	   •   RDF_COLLECTION: Neo4j sequences are converted into open lists in RDF.

	   •   SEPARATE_LITERALS: Neo4j sequences are converted into separate assertions, e.g., (x { has: [1, 2] })
	       is converted to (:x, :has, 1) and (:x, :has, 2).

       --schemaOutputPath=<schemaOutputPath>
	   If the RDF schema is derived from the Neo4j dataset, e.g., the class or property hierarchy,

	   an additional path can be specified to store it separately on disk.

	   If the value is null or left out, the derived schema is stored along with the data.

       --sequenceConversionType=<sequenceConversionType>
	   Options:

	   •   RDF_COLLECTION: Neo4j sequences are converted into open lists in RDF.

	   •   SEPARATE_LITERALS: Neo4j sequences are converted into separate assertions, e.g., (x { has: [1, 2] })
	       is converted to (:x, :has, 1) and (:x, :has, 2).

       -t, --numberOfServerThreads=<numberOfServerThreads>
```