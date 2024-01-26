package de.derivo.neo2rdf.conversion.cli;

import de.derivo.neo2rdf.util.VersionUtil;
import picocli.CommandLine;

public class Neo2RDFVersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() {
        return new String[]{VersionUtil.getVersion()};
    }
}
