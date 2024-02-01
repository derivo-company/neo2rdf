@echo off
set JAVA_PATH=java
set NEO2RDF_HOME=%~dp0
"%JAVA_PATH%" -cp "%NEO2RDF_HOME%neo2rdf.jar" de.derivo.neo2rdf.conversion.cli.Neo4jToRDFConversionCLI %*