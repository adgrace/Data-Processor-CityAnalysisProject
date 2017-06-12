package com.alexgrace.finalyearproject.dataprocessor.other;

import com.vividsolutions.jts.geom.Coordinate;

import java.awt.*;

/**
 * Created by AlexGrace on 05/05/2017.
 */
public class EuclideanDistance {
    public Double VenueModel(Coordinate coordinateA, Coordinate coordinateB) {
        Double x = coordinateA.x - coordinateB.x;
        Double y = coordinateA.y - coordinateB.y;

        return Math.sqrt((x*x)+(y*y));
    }

    public Double UserModel(Coordinate coordinateA, Coordinate coordinateB) {
        Double x = coordinateA.x - coordinateB.x;
        Double y = coordinateA.y - coordinateB.y;

        return Math.sqrt((x*x)+(y*y));
    }

    public Double HybridModel(Coordinate coordinateA, Coordinate coordinateB) {
        Double x = coordinateA.x - coordinateB.x;
        Double y = coordinateA.y - coordinateB.y;

        return Math.sqrt((x*x)+(y*y));
    }
}
