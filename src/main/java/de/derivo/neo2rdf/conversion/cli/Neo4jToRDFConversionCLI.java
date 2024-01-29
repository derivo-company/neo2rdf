package de.derivo.neo2rdf.conversion.cli;

import picocli.CommandLine;

@CommandLine.Command(
        name = "neo2rdf",
        aliases = "",
        subcommands = {Neo4jToTurtleDumpCommand.class, Neo4jToRDFConversionServerCommand.class},
        mixinStandardHelpOptions = true,
        versionProvider = Neo2RDFVersionProvider.class,
        usageHelpWidth = 100)
public class Neo4jToRDFConversionCLI {

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new Neo4jToRDFConversionCLI());
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

}
