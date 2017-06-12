package com.alexgrace.finalyearproject.dataprocessor.entities;

import java.util.HashSet;
import java.util.Set;


/**
 * Created by AlexGrace on 22/04/2017.
 */
public class PolygonObj {

    private String polygonId;
    private String polygonName;
    private String polygonCountry;
    private Activity polygonActivity = new Activity();
    private int numberOfVenues;
    private transient Set<String> venuesInPolygon = new HashSet<>();

    public String getPolygonId() {
        return polygonId;
    }
    public String getPolygonName() {
        return polygonName;
    }
    public String getPolygonCountry() {
        return polygonCountry;
    }
    public Activity getPolygonActivity() {
        return polygonActivity;
    }
    public int getNumberOfVenues() {
        return venuesInPolygon.size();
    }
    public Set<String> getVenuesInPolygon() {
        return venuesInPolygon;
    }

    public void setPolygonId(String polygonId) {
        this.polygonId = polygonId;
    }
    public void setPolygonName(String polygonName) {
        this.polygonName = polygonName;
    }
    public void setPolygonCountry(String polygonCountry) {
        this.polygonCountry = polygonCountry;
    }
    public void setPolygonActivity(Activity polygonActivity) {
        this.polygonActivity = polygonActivity;
    }
    public void setVenuesInPolygon(Set<String> venuesInPolygon) {
        this.venuesInPolygon = venuesInPolygon;
    }
}
