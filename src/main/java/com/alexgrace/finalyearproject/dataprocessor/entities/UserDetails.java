package com.alexgrace.finalyearproject.dataprocessor.entities;

/**
 * Created by AlexGrace on 11/06/2017.
 */
public class UserDetails {

    public String getTwitterhandle() {
        return twitterhandle;
    }

    public void setTwitterhandle(String twitterhandle) {
        this.twitterhandle = twitterhandle;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoto_prefix() {
        return photo_prefix;
    }

    public void setPhoto_prefix(String photo_prefix) {
        this.photo_prefix = photo_prefix;
    }

    public String getPhoto_suffix() {
        return photo_suffix;
    }

    public void setPhoto_suffix(String photo_suffix) {
        this.photo_suffix = photo_suffix;
    }

    private String twitterhandle;
    private String firstname;
    private String lastname;
    private String gender;
    private String photo_prefix;
    private String photo_suffix;
}
