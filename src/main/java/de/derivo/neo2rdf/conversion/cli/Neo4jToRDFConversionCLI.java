package de.derivo.neo2rdf.conversion.cli;

/*-
 * #%L
 * neo2rdf
 * %%
 * Copyright (C) 2026 Derivo Company
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import picocli.CommandLine;

@CommandLine.Command(
        name = "neo2rdf",
        header = "Converts a Neo4j database to RDF Turtle",
        description = """
                Neo2RDF is a command line application that converts a Neo4j database into an RDF file in Turtle format. It is
                implemented in Java and uses the Cypher query language via the official Neo4j Java driver.
                """,
        subcommands = {Neo4jToTurtleDumpCommand.class, Neo4jToRDFConversionServerCommand.class, CommandLine.HelpCommand.class},
        mixinStandardHelpOptions = true,
        showDefaultValues = true,
        versionProvider = Neo2RDFVersionProvider.class,
        usageHelpWidth = 95)
public class Neo4jToRDFConversionCLI {

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new Neo4jToRDFConversionCLI());
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

}
