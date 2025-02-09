# Neo2RDF

Neo2RDF is a command line application that converts a Neo4j database into an RDF file
in [Turtle](https://www.w3.org/TR/turtle/) format. It is implemented in Java and
uses the [Cypher](https://neo4j.com/docs/cypher-manual/current/) query language
via the official Neo4j Java driver. Neo2RDF exclusively uses the [openCypher](https://opencypher.org)
standard in order to achieve the greatest possible compatibility with other graph databases
(tested with [Neo4j](https://neo4j.com) and [Memgraph](https://memgraph.com)).

For instance, with the help of Neo2RDF, you can connect to a running Neo4j instance (local, remote,
or on [Neo4j Aura](https://neo4j.com/product/auradb/)) and either dump a Turtle
file or generate a Turtle data stream. While Neo2RDF uses the Neo4j driver,
it may also work with other graph databases that support the Bolt protocol, such
as Memgraph.

## Installation & Quickstart

Neo2RDF requires Java 17 or higher. To get started, download the latest zip file from
the [releases page](https://github.com/derivo-company/neo2rdf/releases), which contains the executable JAR with all
dependencies as well as Bash and Batch startup scripts. Unpack the archive to your preferred location and add
the directory to your PATH environment variable in case you want to invoke the Neo2RDF scripts from any location.

The command line application can be invoked as follows:

```
./neo2rdf.sh
```

The execution of the script will print general information on the application and an exemplary usage of the two available
conversion modes.

### Conversion Modes

Two conversion modes are available:

- **DB-to-File**: A provided Neo4j database is converted into an RDF file in Turtle format, which is written to a
  specified location on disk. \
  Example that connects to a local Neo4j instance:
  ```
  ./neo2rdf.sh dump --database="someDBName" \
  --uri="bolt://localhost:7687" \
  --user="neo4j" \
  --password="PASSWORD123" \
  --outputPath=output/path/data.ttl
  ```

- **DB-to-Stream**: The application starts an HTTP server for the provided Neo4j database. When a GET request is sent to
  the server (e.g., to `http://localhost:8080`), the conversion procedure is initiated and the response returns an RDF
  Turtle stream to the client. \
  Example that connects to an Neo4j cloud instance on Aura and generates a stream on request:
  ```
  ./neo2rdf.sh server --database="neo4j" \
  --uri="neo4j+s://867928679.databases.neo4j.io" \
  --user="neo4j" \
  --password="eBWczH5dRt2VR1C1eYKvk5jRt2VR1C1eY72NUCk" \
  --port=8080
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
       Neo2RDF is a command line application that converts a Neo4j database into an RDF file
       in Turtle format. It is implemented in Java and uses the Cypher query language via
       the official Neo4j Java driver.

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
       [--derivePropertyHierarchyByRelationshipSubsetCheck]
       [--reifyOnlyRelationshipsWithProperties] [--reifyRelationships]
       [--basePrefix=<basePrefix>] [-cfg=<conversionConfigFile>] -db=<databaseName>
       -o=<outputPath> [--password=<dbPassword>]
       [--reificationVocabulary=<reificationVocabulary>]
       [--schemaOutputPath=<schemaOutputPath>]
       [--sequenceConversionType=<sequenceConversionType>] [-u=<dbUser>] --uri=<dbURI>
       [--relationshipTypeReificationBlacklist=<relationshipTypeReificationBlacklist>[,
       <relationshipTypeReificationBlacklist>...]]...

DESCRIPTION
       The Neo4j database is converted into an RDF file in Turtle format, which is written
       to the specified location on disk. 
       Exemplary usage: dump --database="someDBName" \
                     --uri="bolt://localhost:7687" \
                     --user="neo4j" \ 
                     --password="PASSWORD123" \
                     --outputPath=output/path/data.ttl

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

       -db, --database=<databaseName>
	   The name of the database to connect to.

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

       -o, --outputPath=<outputPath>,
        
        --password=<dbPassword>
	   The password for the database user.

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
	   
	      Default: "false".

       --reifyRelationships
	   By default, each Neo4j relationship is reified in RDF by a distinct blank node.
	   If this option is set to false, no Neo4j relationships will be reified in RDF. 
	   
	      Default: "true".

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

       -u, --user=<dbUser>
	   The username for the database instance.

       --uri=<dbURI>
	   The URI for the database instance. Example: bolt://localhost:7687

```

### DB-to-stream - Server Command

```
NEO2RDF-SERVER(1)		       Neo2rdf Manual			   NEO2RDF-SERVER(1)

NAME
       neo2rdf-server - Starts an HTTP server that serves the conversion result as RDF
       Turtle stream

SYNOPSIS
       neo2rdf server [--deriveClassHierarchyByLabelSubsetCheck]
       [--derivePropertyHierarchyByRelationshipSubsetCheck]
       [--reifyOnlyRelationshipsWithProperties] [--reifyRelationships]
       [--basePrefix=<basePrefix>] [-cfg=<conversionConfigFile>] -db=<databaseName>
       -p=<port> [--password=<dbPassword>] [--reificationVocabulary=<reificationVocabulary>]
       [--schemaOutputPath=<schemaOutputPath>]
       [--sequenceConversionType=<sequenceConversionType>] [-t=<numberOfServerThreads>]
       [-u=<dbUser>] --uri=<dbURI>
       [--relationshipTypeReificationBlacklist=<relationshipTypeReificationBlacklist>[,
       <relationshipTypeReificationBlacklist>...]]...

DESCRIPTION
       The application starts an HTTP server for the provided Neo4j database. When a GET
       request is sent to the server, the conversion procedure is initiated and the response
       returns an RDF Turtle stream to the client. 
       Exemplary usage: server --database="neo4j" \
                    --uri="neo4j+s://867928679.databases.neo4j.io" \
                    --user="neo4j" \
                    --password="eBWczH5dRt2VR1C1eYKvk5jRt2VR1C1eY72NUCk" \
                    --port=8080

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

       -db, --database=<databaseName>
	   The name of the database to connect to.

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

       -p, --port=<port>
	   Default: 8080

       --password=<dbPassword>
	   The password for the database user.

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

       -u, --user=<dbUser>
	   The username for the database instance.

       --uri=<dbURI>
	   The URI for the database instance. Example: bolt://localhost:7687
