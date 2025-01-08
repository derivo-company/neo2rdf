package de.derivo.neo2rdf.util;


import java.text.DecimalFormat;

public class ConsoleUtil {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");

    private static final String SEPARATOR = "-------------------------------------------------------------";

    public static String getSeparator() {
        return SEPARATOR;
    }

    public static String formatDecimal(Number val) {
        return DECIMAL_FORMAT.format(val);
    }
}
