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
  ./neo2rdf.sh dump -db=./path/to/dbmss/dbms-xxx/data/databases/neo4j/ -o=output/path/data.ttl
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
NEO2RDF(1)			       Neo2rdf Manual				  NEO2RDF(1)

NAME
       neo2rdf - Converts a Neo4j database to RDF Turtle

SYNOPSIS
       neo2rdf [-hV] [COMMAND]

DESCRIPTION
       Neo2RDF is a command line application that converts a Neo4j database into RDF Turtle
       format. It is implemented in Java and uses the official Neo4j record storage reader.

OPTIONS
       -h, --help
	   Show this help message and exit.

       -V, --version
	   Print version information and exit.

COMMANDS
       dump
	   Converts a Neo4j database into an RDF file in Turtle format

       server
	   Starts an HTTP server that serves the conversion result as RDF Turtle stream

       help
	   Display help information about the specified command.
```

### DB-to-File - Dump Command

```
NEO2RDF-DUMP(1)			       Neo2rdf Manual			     NEO2RDF-DUMP(1)

NAME
       neo2rdf-dump - Converts a Neo4j database into an RDF file in Turtle format

SYNOPSIS
       neo2rdf dump [--deriveClassHierarchyByLabelSubsetCheck]
       [--derivePropertyHierarchyByRelationshipSubsetCheck] [--includeDeletedNeo4jLabels]
       [--includeDeletedPropertyKeys] [--includeDeletedRelationshipTypes]
       [--reifyRelationships]
       [--reifyOnlyRelationshipsWithProperties] [--basePrefix=<basePrefix>]
       [-cfg=<conversionConfigFile>] [-d=<neo4jDBDumpPath>] -db=<neo4jDBDirectory>
       -o=<outputPath> [--reificationVocabulary=<reificationVocabulary>]
       [--schemaOutputPath=<schemaOutputPath>]
       [--sequenceConversionType=<sequenceConversionType>]
       [--relationshipTypeReificationBlacklist=<relationshipTypeReificationBlacklist>[,
       <relationshipTypeReificationBlacklist>...]]...

DESCRIPTION
       The Neo4j database is converted into an RDF file in Turtle format, which is written
       to the specified location on disk. Exemplary usage: dump -db=./path/to/neo4jdb
       -o=output/path/data.ttl

OPTIONS
       --basePrefix=<basePrefix>
	   Prefix that is used for all converted nodes, property keys, relationship types,
	   and relationships.

	       Default: https://www.example.org/

       -cfg, --config=<conversionConfigFile>
	   Instead of specifying non-mandatory options in terms of command line parameters,
	   it is also possible to use an external YAML configuration.
	   The YAML keys must have the same identifiers as the long option names, e.g.,
	   'basePrefix: https://www.example.org/other-prefix#'.

       -d, --neo4jDBDumpPath=<neo4jDBDumpPath>
	   If a path to a Neo4j dump has been specified using this parameter, the DB dump is
	   extracted to the appropriate target Neo4j DB directory first, and subsequently,
	   the conversion procedure gets executed as usual.

       -db, --neo4jDBDirectory=<neo4jDBDirectory>
	   If you do not know the directory location of your DBMS, check out the following
	   link:
	      https://neo4j.com/docs/desktop-manual/current/troubleshooting/locating-dbms/
	   The individual DB directories of your DBMS are subsequently located under
	    "./dbmss/dbms-XYZ/data/databases/*" (specify one for the given parameter).
	   Although the conversion procedure often runs successfully while the Neo4j DB is
	    running, it is suggested to correctly shut the DB down beforehand since it can
	    also lead to execution errors.
	   Also if the DB is not running but has not been shut down correctly, the DB files
	    might be in a corrupt state. In this case, try to start and stop the Neo4j DB to
	    resolve the issue.

       --deriveClassHierarchyByLabelSubsetCheck
	   Indicates whether the RDF class hierarchy should be derived.
	   For this purpose, it is examined which sets of Neo4j nodes with an assigned label
	   are a subset of one another.

       --derivePropertyHierarchyByRelationshipSubsetCheck
	   Indicates whether the RDF property hierarchy should be derived.
	   For this, the node-node combinations for each relationship type are initially
	   collected in a set.
	   Subsequently, for every pair of sets, it is examined whether they are a subset of
	   each other.

       --includeDeletedNeo4jLabels

       --includeDeletedPropertyKeys

       --includeDeletedRelationshipTypes

       -o, --outputPath=<outputPath>

       --reificationVocabulary=<reificationVocabulary>
	   The reification vocabulary defines how a quadruple (sbj, pred, obj, statementID)
	   should be reified.
	   Options:
	   •   RDF_REIFICATION: uses the RDF reification vocabulary, i.e.,  rdf:Statement,
	       rdf:subject, rdf:predicate, and rdf:object
		   (cf. https://www.w3.org/TR/rdf11-mt/#reification)
	   •   OWL_REIFICATION: uses the OWL vocabulary, i.e.,	owl:Axiom,
	       rdf:annotatedSource, owl:annotatedProperty, and owl:annotatedTarget
		   (cf. https://www.w3.org/TR/owl2-quick-reference/#Annotations)

		   Default: OWL_REIFICATION

       --reifyOnlyRelationshipsWithProperties
	   By default, each Neo4j relationship is reified in RDF by a distinct blank node.
	   If this option is set, only Neo4j relationships with properties will be reified
	   in RDF.

       --reifyRelationships
	   By default, each Neo4j relationship is reified in RDF by a distinct blank node.
	   If this option is set, no Neo4j relationships will be reified in RDF.

       --relationshipTypeReificationBlacklist=<relationshipTypeReificationBlacklist>[,<relationshipTypeReificationBlacklist>...]
	   By default, each Neo4j relationship is reified in RDF by a distinct blank node.
	   Using this option, the blacklisted Neo4j relationship types will not be reified
	   in RDF.

	       Default: []

       --schemaOutputPath=<schemaOutputPath>
	   If the RDF schema is derived from the Neo4j dataset, e.g., the class or property
	   hierarchy, an additional path can be specified to store it separately on disk.
	   If the value is null or left out, the derived schema is stored along with the
	   data.

       --sequenceConversionType=<sequenceConversionType>
	   Options:
	   •   RDF_COLLECTION: Neo4j sequences are converted into open lists in RDF.
	   •   SEPARATE_LITERALS: Neo4j sequences are converted into separate assertions,
	       e.g., (x { has: [1, 2] }) is converted to (:x, :has, 1) and (:x, :has, 2).

		   Default: RDF_COLLECTION
```


### DB-to-stream - Server Command

```
NEO2RDF-SERVER(1)		       Neo2rdf Manual			   NEO2RDF-SERVER(1)

NAME
       neo2rdf-server - Starts an HTTP server that serves the conversion result as RDF
       Turtle stream

SYNOPSIS
       neo2rdf server [--deriveClassHierarchyByLabelSubsetCheck]
       [--derivePropertyHierarchyByRelationshipSubsetCheck] [--includeDeletedNeo4jLabels]
       [--includeDeletedPropertyKeys] [--includeDeletedRelationshipTypes]
       [--reifyRelationships]
       [--reifyOnlyRelationshipsWithProperties] [--basePrefix=<basePrefix>]
       [-cfg=<conversionConfigFile>] [-d=<neo4jDBDumpPath>] -db=<neo4jDBDirectory> -p=<port>
       [--reificationVocabulary=<reificationVocabulary>]
       [--schemaOutputPath=<schemaOutputPath>]
       [--sequenceConversionType=<sequenceConversionType>] [-t=<numberOfServerThreads>]
       [--relationshipTypeReificationBlacklist=<relationshipTypeReificationBlacklist>[,
       <relationshipTypeReificationBlacklist>...]]...

DESCRIPTION
       The application starts an HTTP server for the provided Neo4j database. When a GET
       request is sent to the server, the conversion procedure is initiated and the response
       returns an RDF Turtle stream to the client. 
       Exemplary usage: server -db=./path/to/neo4jdb -p=8080

OPTIONS
       --basePrefix=<basePrefix>
	   Prefix that is used for all converted nodes, property keys, relationship types,
	   and relationships.
	       Default: https://www.example.org/

       -cfg, --config=<conversionConfigFile>
	   Instead of specifying non-mandatory options in terms of command line parameters,
	   it is also possible to use an external YAML configuration.
	   The YAML keys must have the same identifiers as the long option names, e.g.,
	   'basePrefix: https://www.example.org/other-prefix#'.

       -d, --neo4jDBDumpPath=<neo4jDBDumpPath>
	   If a path to a Neo4j dump has been specified using this parameter, the DB dump is
	   extracted to the appropriate target Neo4j DB directory first, and subsequently,
	   the conversion procedure gets executed as usual.

       -db, --neo4jDBDirectory=<neo4jDBDirectory>
	   If you do not know the directory location of your DBMS, check out the following
	   link:
	      https://neo4j.com/docs/desktop-manual/current/troubleshooting/locating-dbms/
	   The individual DB directories of your DBMS are subsequently located under
	    "./dbmss/dbms-XYZ/data/databases/*" (specify one for the given parameter).
	   Although the conversion procedure often runs successfully while the Neo4j DB is
	    running, it is suggested to correctly shut the DB down beforehand since it can
	    also lead to execution errors.
	   Also if the DB is not running but has not been shut down correctly, the DB files
	    might be in a corrupt state. In this case, try to start and stop the Neo4j DB to
	    resolve the issue.

       --deriveClassHierarchyByLabelSubsetCheck
	   Indicates whether the RDF class hierarchy should be derived.
	   For this purpose, it is examined which sets of Neo4j nodes with an assigned label
	   are a subset of one another.

       --derivePropertyHierarchyByRelationshipSubsetCheck
	   Indicates whether the RDF property hierarchy should be derived.
	   For this, the node-node combinations for each relationship type are initially
	   collected in a set.
	   Subsequently, for every pair of sets, it is examined whether they are a subset of
	   each other.

       --includeDeletedNeo4jLabels

       --includeDeletedPropertyKeys

       --includeDeletedRelationshipTypes 
       
       -p, --port=<port>
	      Default: 8080

       --reificationVocabulary=<reificationVocabulary>
	   The reification vocabulary defines how a quadruple (sbj, pred, obj, statementID)
	   should be reified.

	   Options:
	   •   RDF_REIFICATION: uses the RDF reification vocabulary, i.e.,  rdf:Statement,
	       rdf:subject, rdf:predicate, and rdf:object
		   (cf. https://www.w3.org/TR/rdf11-mt/#reification)
	   •   OWL_REIFICATION: uses the OWL vocabulary, i.e.,	owl:Axiom,
	       rdf:annotatedSource, owl:annotatedProperty, and owl:annotatedTarget
		   (cf. https://www.w3.org/TR/owl2-quick-reference/#Annotations)

		   Default: OWL_REIFICATION

       --reifyOnlyRelationshipsWithProperties
	   By default, each Neo4j relationship is reified in RDF by a distinct blank node.
	   If this option is set, only Neo4j relationships with properties will be reified
	   in RDF.

       --reifyRelationships
	   By default, each Neo4j relationship is reified in RDF by a distinct blank node.
	   If this option is set to false, no Neo4j relationships will be reified in RDF.

       --relationshipTypeReificationBlacklist=<relationshipTypeReificationBlacklist>[,<relationshipTypeReificationBlacklist>...]
	   By default, each Neo4j relationship is reified in RDF by a distinct blank node.
	   Using this option, the blacklisted Neo4j relationship types will not be reified
	   in RDF.

	       Default: []

       --schemaOutputPath=<schemaOutputPath>
	   If the RDF schema is derived from the Neo4j dataset, e.g., the class or property
	   hierarchy, an additional path can be specified to store it separately on disk.
	   If the value is null or left out, the derived schema is stored along with the
	   data.

       --sequenceConversionType=<sequenceConversionType>
	   Options:
	   •   RDF_COLLECTION: Neo4j sequences are converted into open lists in RDF.
	   •   SEPARATE_LITERALS: Neo4j sequences are converted into separate assertions,
	       e.g., (x { has: [1, 2] }) is converted to (:x, :has, 1) and (:x, :has, 2).

		   Default: RDF_COLLECTION

       -t, --numberOfServerThreads=<numberOfServerThreads>
	      Default: 2
```
