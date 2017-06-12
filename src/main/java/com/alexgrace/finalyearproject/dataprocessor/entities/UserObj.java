package com.alexgrace.finalyearproject.dataprocessor.entities;

/**
 * Created by AlexGrace on 11/06/2017.
 */
public class UserObj {
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserModel() {
        return userModel;
    }

    public void setUserModel(String userModel) {
        this.userModel = userModel;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public Activity getVenueActivity() {
        return venueActivity;
    }

    public void setVenueActivity(Activity venueActivity) {
        this.venueActivity = venueActivity;
    }

    private String userId;
    private String userModel;
    private UserDetails userDetails = new UserDetails();
    private Activity venueActivity = new Activity();
}
