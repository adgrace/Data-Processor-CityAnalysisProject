package com.alexgrace.finalyearproject.dataprocessor.entities;

import java.util.List;

/**
 * Created by AlexGrace on 12/06/2017.
 */
public class categories {
    public List<FoursquareVenueCategories> getCategories() {
        return categories;
    }

    public void setCategories(List<FoursquareVenueCategories> categories) {
        this.categories = categories;
    }

    private List<FoursquareVenueCategories> categories;
}
