package de.derivo.neo2rdf.util;

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
