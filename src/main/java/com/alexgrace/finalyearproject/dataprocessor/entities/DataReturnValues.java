package com.alexgrace.finalyearproject.dataprocessor.entities;

import org.geojson.Feature;
import org.geojson.FeatureCollection;

import java.util.Map;
import java.util.Set;

/**
 * Created by AlexGrace on 27/04/2017.
 */
public class DataReturnValues {
    public final String featureCollectionName;
    public final FeatureCollection featureCollection;
    public final Map<String, PolygonObj> polygonMap;
    public final Map<String, String> venueNeighbourhoodMap;

    public DataReturnValues(String featureCollectionName, FeatureCollection featureCollection, Map<String, PolygonObj> polygonMap, Map<String, String> venueNeighbourhoodMap) {
        this.featureCollectionName = featureCollectionName;
        this.featureCollection = featureCollection;
        this.polygonMap = polygonMap;
        this.venueNeighbourhoodMap = venueNeighbourhoodMap;
    }
}
