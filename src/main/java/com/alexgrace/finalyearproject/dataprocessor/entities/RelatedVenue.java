package com.alexgrace.finalyearproject.dataprocessor.entities;

/**
 * Created by AlexGrace on 24/04/2017.
 */
public class RelatedVenue {
    private String id;
    private String listNum;
    private Double percentageSimilarity;

    public String getId() {
        return id;
    }
    public String getListNum() {
        return listNum;
    }
    public Double getPercentageSimilarity() {
        return percentageSimilarity;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setListNum(String listNum) {
        this.listNum = listNum;
    }
    public void setPercentageSimilarity(Double percentageSimilarity) {
        this.percentageSimilarity = percentageSimilarity;
    }
}
