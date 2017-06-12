package com.alexgrace.finalyearproject.dataprocessor.entities;

import java.util.Set;

/**
 * Created by AlexGrace on 23/04/2017.
 */
public class RelatedVenues {
    private Set<RelatedVenue> neighbourhoodRelatedVenues;
    private Set<RelatedVenue> venueRelatedVenues;
    private Set<RelatedVenue> userRelatedVenues;
    private Set<RelatedVenue> hybridRelatedVenues;

    public Set<RelatedVenue> getNeighbourhoodRelatedVenues() {
        return neighbourhoodRelatedVenues;
    }
    public Set<RelatedVenue> getHybridRelatedVenues() {
        return hybridRelatedVenues;
    }
    public Set<RelatedVenue> getUserRelatedVenues() {
        return userRelatedVenues;
    }
    public Set<RelatedVenue> getVenueRelatedVenues() {
        return venueRelatedVenues;
    }

    public void setNeighbourhoodRelatedVenues(Set<RelatedVenue> neighbourhoodRelatedVenues) {
        this.neighbourhoodRelatedVenues = neighbourhoodRelatedVenues;
    }
    public void setHybridRelatedVenues(Set<RelatedVenue> hybridRelatedVenues) {
        this.hybridRelatedVenues = hybridRelatedVenues;
    }
    public void setUserRelatedVenues(Set<RelatedVenue> userRelatedVenues) {
        this.userRelatedVenues = userRelatedVenues;
    }
    public void setVenueRelatedVenues(Set<RelatedVenue> venueRelatedVenues) {
        this.venueRelatedVenues = venueRelatedVenues;
    }
}
