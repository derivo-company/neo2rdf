package de.derivo.neo2rdf.conversion.cli;

import picocli.CommandLine;

@CommandLine.Command(
        name = "",
        subcommands = {Neo4jToTurtleDumpCommand.class, Neo4jToRDFConversionServerCommand.class},
        mixinStandardHelpOptions = true,
        helpCommand = true,
        version = "0.3.0",
        usageHelpWidth = 100,
        description = """
                Example for dump conversion:      dump -db=./path/to/neo4jdb -o=output/path/data.ttl
                Example for conversion server:    server -db=./path/to/neo4jdb -p=8080
                """)
public class Neo4jToRDFConversionCLI implements Runnable {
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new Neo4jToRDFConversionCLI());
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        throw new CommandLine.ParameterException(spec.commandLine(), "Missing required subcommand");
    }
}
