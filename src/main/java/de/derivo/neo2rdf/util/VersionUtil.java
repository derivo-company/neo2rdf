package de.derivo.neo2rdf.util;

public class VersionUtil {

    public static String getVersion() {
        String version = VersionUtil.class.getPackage().getImplementationVersion();
        if (version == null) {
            version = "DEVELOPMENT";
        }
        return version;
    }
}
