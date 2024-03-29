package de.derivo.neo2rdf.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

public class ConsoleUtil {

    private static final Logger log;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");

    static {
        log = LoggerFactory.getLogger("Neo2RDF");
    }

    public static Logger getLogger() {
        return log;
    }

    private static final String SEPARATOR = "-------------------------------------------------------------";

    public static String getSeparator() {
        return SEPARATOR;
    }

    public static String formatDecimal(Number val) {
        return DECIMAL_FORMAT.format(val);
    }
}
