#!/bin/bash
JAVA_PATH="java"
# install directory is directory of given script by default
if [ -z "${NEO2RDF_HOME}" ]; then
    NEO2RDF_HOME=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd)
fi
$JAVA_PATH -cp "$NEO2RDF_HOME/neo2rdf.jar" de.derivo.neo2rdf.conversion.cli.Neo4jToRDFConversionCLI $@