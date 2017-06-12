/*
 * Developed by Alex Grace for research purposes only. (ag00248@surrey.ac.uk)
 */

package com.alexgrace.finalyearproject.dataprocessor.entities;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LocationFilter {
    public String getName() { return name; }
    public double getLat() {
        return lat;
    }
    public double getLng() {
        return lng;
    }
    public int getRadius() {
        return radius;
    }
    public String getNeighbourhoodDataURL() { return neighbourhoodDataURL; }

    private String name;
    private double lat;
    private double lng;
    private int radius;
    private String neighbourhoodDataURL;
}
