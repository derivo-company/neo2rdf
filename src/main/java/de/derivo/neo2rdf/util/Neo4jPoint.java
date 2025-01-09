package de.derivo.neo2rdf.util;

public enum Neo4jPoint {
    POINT_2D_CARTESIAN(7203),
    POINT_2D_WGS_84(4326),
    POINT_3D_CARTESIAN(9157),
    POINT_3D_WGS_84(4979),
    ;

    private final int srid;

    Neo4jPoint(int srid) {
        this.srid = srid;
    }

    public int getSRID() {
        return srid;
    }

    public static Neo4jPoint getPointType(int srid) {
        return switch (srid) {
            case 7203 -> POINT_2D_CARTESIAN;
            case 4326 -> POINT_2D_WGS_84;
            case 9157 -> POINT_3D_CARTESIAN;
            case 4979 -> POINT_3D_WGS_84;
            default -> throw new IllegalArgumentException("Unknown SRID: " + srid);
        };
    }
}
