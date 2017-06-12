package com.alexgrace.finalyearproject.dataprocessor.entities;

public class VenueDetails {
    private String name;
    private double lat;
    private double lng;
    private String contact_phone;
    private String contact_formattedPhone;
    private String contact_twitter;
    private String contact_instagram;
    private String contact_facebook;
    private String contact_facebookusername;
    private String contact_facebookname;
    private String location_address;
    private String location_formattedaddress;
    private String location_crossstreet;
    private String location_postalcode;
    private String location_cc;
    private String location_neighbourhood;
    private String location_city;
    private String location_state;
    private String location_country;
    private Boolean verified;
    private int stats_checkincount;
    private int stats_usercount;
    private String url;
    private String categoryId;

    public String getName() { return name; }
    public double getLat() {
        return lat;
    }
    public double getLng() {
        return lng;
    }
    public String getContact_phone() { return contact_phone; }
    public String getContact_formattedPhone() { return contact_formattedPhone; }
    public String getContact_twitter() { return contact_twitter; }
    public String getContact_instagram() { return contact_instagram; }
    public String getContact_facebook() { return contact_facebook; }
    public String getContact_facebookusername() { return contact_facebookusername; }
    public String getContact_facebookname() { return contact_facebookname; }
    public String getLocation_address() { return location_address; }
    public String getLocation_formattedaddress() { return location_formattedaddress; }
    public String getLocation_crossstreet() { return location_crossstreet; }
    public String getLocation_postalcode() { return location_postalcode; }
    public String getLocation_cc() { return location_cc; }
    public String getLocation_neighbourhood() { return location_neighbourhood; }
    public String getLocation_city() { return location_city; }
    public String getLocation_state() { return location_state; }
    public String getLocation_country() { return location_country; }
    public Boolean getVerified() { return verified; }
    public int getStats_checkincount() { return stats_checkincount; }
    public int getStats_usercount() { return stats_usercount; }
    public String getUrl() { return url; }
    public String getCategoryId() { return categoryId; }

    public void setName(String x) { this.name = x; }
    public void setLat(double x) { this.lat = x; }
    public void setLng(double x) { this.lng = x; }
    public void setContact_phone(String x) { this.contact_phone = x; }
    public void setContact_formattedPhone(String x) { this.contact_formattedPhone = x; }
    public void setContact_twitter(String x) { this.contact_twitter = x; }
    public void setContact_instagram(String x) { this.contact_instagram = x; }
    public void setContact_facebook(String x) { this.contact_facebook = x; }
    public void setContact_facebookusername(String x) { this.contact_facebookusername = x; }
    public void setContact_facebookname(String x) { this.contact_facebookname = x; }
    public void setLocation_address(String x) { this.location_address = x; }
    public void setLocation_formattedaddress(String x) { this.location_formattedaddress = x; }
    public void setLocation_crossstreet(String x) { this.location_crossstreet = x; }
    public void setLocation_postalcode(String x) { this.location_postalcode = x; }
    public void setLocation_cc(String x) { this.location_cc = x; }
    public void setLocation_neighbourhood(String x) { this.location_neighbourhood = x; }
    public void setLocation_city(String x) { this.location_city = x; }
    public void setLocation_state(String x) { this.location_state = x; }
    public void setLocation_country(String x) { this.location_country = x; }
    public void setVerified(Boolean x) { this.verified = x; }
    public void setStats_checkincount(int x) { this.stats_checkincount = x; }
    public void setStats_usercount(int x) { this.stats_usercount = x; }
    public void setUrl(String x) { this.url = x; }
    public void setCategoryId(String x) { this.categoryId = x; }
}
