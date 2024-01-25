#!/bin/bash
JAVA_PATH="java"
$JAVA_PATH -cp ./neo2rdf.jar de.derivo.neo2rdf.conversion.cli.Neo4jToRDFConversionCLI $@