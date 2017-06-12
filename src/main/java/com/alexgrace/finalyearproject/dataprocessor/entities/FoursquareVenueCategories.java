package com.alexgrace.finalyearproject.dataprocessor.entities;

import java.util.List;

/**
 * Created by AlexGrace on 12/06/2017.
 */
public class FoursquareVenueCategories {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPluralName() {
        return pluralName;
    }

    public void setPluralName(String pluralName) {
        this.pluralName = pluralName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public List<FoursquareVenueCategories> getCategories() {
        return categories;
    }

    public void setCategories(List<FoursquareVenueCategories> categories) {
        this.categories = categories;
    }

    private String id;
    private String name;
    private String pluralName;
    private String shortName;
    private Icon icon;
    private List<FoursquareVenueCategories> categories;
}
