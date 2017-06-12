package com.alexgrace.finalyearproject.dataprocessor.entities;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by AlexGrace on 22/04/2017.
 */
public class VenueObj {
    private String venueId;
    private String city;
    private VenueDetails venueDetails = new VenueDetails();
    private Activity venueActivity = new Activity();
    private RelatedVenues relatedVenues = new RelatedVenues();
    private String neighbourhoodModel;
    private String venueModel;
    private String userModel;
    private String hybridModel;


    public String getVenueId() {
        return venueId;
    }
    public String getCity() {
        return city;
    }
    public VenueDetails getVenueDetails() {
        return venueDetails;
    }
    public Activity getVenueActivity() {
        return venueActivity;
    }
    public RelatedVenues getRelatedVenues() {
        return relatedVenues;
    }
    public String getNeighbourhoodModel() {
        return neighbourhoodModel;
    }
    public String getVenueModel() {
        return venueModel;
    }
    public String getUserModel() {
        return userModel;
    }
    public String getHybridModel() {
        return hybridModel;
    }


    public void setVenueId( String venueId) {
        this.venueId = venueId;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public void setVenueDetails( VenueDetails venueDetails) {
        this.venueDetails = venueDetails;
    }
    public void setVenueActivity( Activity venueActivity) {
        this.venueActivity = venueActivity;
    }
    public void setRelatedVenues( RelatedVenues relatedVenues) {
        this.relatedVenues = relatedVenues;
    }
    public void setNeighbourhoodModel( String neighbourhoodModel) {
        this.neighbourhoodModel = neighbourhoodModel;
    }
    public void setVenueModel( String venueModel) {
        this.venueModel = venueModel;
    }
    public void setUserModel( String userModel) {
        this.userModel = userModel;
    }
    public void setHybridModel( String hybridModel) {
        this.hybridModel = hybridModel;
    }
}
