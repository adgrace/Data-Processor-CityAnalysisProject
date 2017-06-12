package com.alexgrace.finalyearproject.dataprocessor;

import com.alexgrace.finalyearproject.dataprocessor.entities.*;
import com.alexgrace.finalyearproject.dataprocessor.other.s3push;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.LngLatAlt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class Processor {

    private static final Log LOG = LogFactory.getLog(Processor.class);

    private ObjectMapper mapper = new ObjectMapper();

    private Connection connect;
    private Statement statement;
    private ResultSet rs;

    private String dbHost;
    private int dbPort;
    private String dbUser;
    private String dbPass;
    private List<LocationFilter> locationFilter;
    private List<String> modelList = new ArrayList<>( Arrays.asList("neighbourhood", "venuebased", "userbased"/*, "hybrid"*/));

    private Map<String, FeatureCollection> featureCollectionMap = new HashMap<>();
    private FeatureCollection venueFeatureCollection = new FeatureCollection();
    private Map<String, PolygonObj> polygonMap = new HashMap<>();
    private Map<String, VenueObj> venueMap = new HashMap<>();
    private Map<String, UserObj> userMap = new HashMap<>();


    private Map<String, FeatureCollection> neighbourhoodsDownload = new HashMap<>();

    public Processor(String dbHost, int dbPort, String dbUser, String dbPass, List<LocationFilter> locationFilter) {
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
        this.locationFilter = locationFilter;
    }


    public void Run(s3push s3) {

        // Clear maps for new cycle
        featureCollectionMap.clear();
        polygonMap.clear();
        venueMap.clear();

        // Start timestamp
        Timestamp timestamp;
        timestamp = new Timestamp(System.currentTimeMillis());
        LOG.info("Running data processing cycle: " + timestamp);

        // Download Neighbourhood Data
        if (neighbourhoodsDownload.isEmpty()) {
            getNeighbourhoodData();
        }

        // Make Database Connection
        LOG.info("Connecting to Database");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort , dbUser, dbPass);
            statement = connect.createStatement();
            LOG.info("Connected to database");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get Venue Data from Database
        locationFilter.forEach((location) -> {
            LOG.info("Getting Data from database: " + location.getName());
            try {
                if (location.getName().equals("london")) {
                    rs = statement.executeQuery("SELECT * FROM finalyearproject.Venue_Detailed where location_cc='GB';");
                } else if (location.getName().equals("newyork")) {
                    rs = statement.executeQuery("SELECT * FROM finalyearproject.Venue_Detailed where location_cc='US';");
                }

                LOG.info("Data recieved, processing object: " + location.getName());
                while (rs.next()) {
                    addVenue(venueMap, rs);
                }
            } catch (SQLException e) {
                LOG.error(e);
            }
            LOG.info("Finished getting and processing venue data: " + location.getName());
        });

        // Get Venue Data from Database
        LOG.info("Getting Users from database");
        try {
            rs = statement.executeQuery("SELECT * FROM finalyearproject.User_Detailed where Checkins > 19;");

            LOG.info("Data recieved, processing users");
            while (rs.next()) {
                addUser(userMap, rs);
            }
        } catch (SQLException e) {
            LOG.error(e);
        }
        LOG.info("Finished getting and processing user data, number of users: " + userMap.size());

        // Process Data
        locationFilter.parallelStream().forEach((location) -> {
            LOG.info("Running data processing cycle");
            Models model = new Models(venueMap, userMap, neighbourhoodsDownload, location);
            modelList.parallelStream().forEach((modelname) -> {
                DataReturnValues data = null;
                switch (modelname) {
                    case "neighbourhood" :
                        LOG.info("Running Neighbourhood");
                        data = model.neighbourhood();
                        break;
                    case "userbased" :
                        model.userbased();
                        break;
                    case "venuebased" :
                        LOG.info("Running Venue Based");
                        data = model.venuebased();
                        break;
                    case "hybrid":
                        //data = model.hybrid();
                        break;
                }
                if (modelname != "userbased") {
                    featureCollectionMap.put(data.featureCollectionName, data.featureCollection);
                    polygonMap.putAll(data.polygonMap);
                    Map<String, String> venueNeighbourhood = data.venueNeighbourhoodMap;
                    venueNeighbourhood.entrySet().parallelStream().forEach(entry -> {
                        venueMap.get(entry.getKey()).setNeighbourhoodModel(entry.getValue());
                    });
                }
            });
        });

        //Prepare Venues
        venueMap.entrySet().parallelStream().forEach(entry -> {
            VenueObj venueValue = entry.getValue();
            String venueKey = entry.getKey();
            LngLatAlt lnglat = new LngLatAlt(venueValue.getVenueDetails().getLng(), venueValue.getVenueDetails().getLat());
            org.geojson.Point venuepoint = new org.geojson.Point(lnglat);
            Feature venuefeature = new Feature();
            venuefeature.setGeometry(venuepoint);
            venuefeature.setProperty("polygonDataUrl", venueValue.getVenueId());
            venueFeatureCollection.add(venuefeature);
        });

        //Push Data
        LOG.info("Now pushing venueFeatureCollection");
        s3.push("venueFeatureCollection.geoJson", getJSON(venueFeatureCollection));


        LOG.info("Now pushing featureCollection data, featureCollectionMap size: " + featureCollectionMap.size());
        featureCollectionMap.entrySet().parallelStream().forEach(entry -> {
            s3.push(entry.getKey(), getJSON(entry.getValue()));
        });

        LOG.info("Now pushing polygon data, polygonMap size: " + polygonMap.size());
        polygonMap.entrySet().parallelStream().forEach(entry -> {
            s3.push(entry.getKey(), getJSON(entry.getValue()));
        });

        LOG.info("Now pushing venues data, venueMap size: " + venueMap.size());
        venueMap.entrySet().parallelStream().forEach(entry -> {
            s3.push(entry.getKey(), getJSON(entry.getValue()));
        });

        //Close Database Connections
        LOG.info("Closing Database connections");
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) { LOG.error(e); }
        }
        if (connect != null) {
            try {
                connect.close();
            } catch (SQLException e) { LOG.error(e); }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) { LOG.error(e); }
        }
        LOG.info("Database connections closed");


        timestamp = new Timestamp(System.currentTimeMillis());
        LOG.info("Finished data processing cycle:  " + timestamp);
    }


    private void getNeighbourhoodData() {
        Timestamp timestamp = null;
        timestamp = new Timestamp(System.currentTimeMillis());
        LOG.info("Getting Neighbourhood Data - " + timestamp);
        locationFilter.parallelStream().forEach((location) -> {

            StringBuilder result = new StringBuilder();
            String line;

            try {
                URL url = new URL(location.getNeighbourhoodDataURL());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FeatureCollection featureCollection = null;
            try {
                featureCollection = new ObjectMapper().readValue(result.toString(), FeatureCollection.class);
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            neighbourhoodsDownload.put(location.getName(), featureCollection);
        });
        timestamp = new Timestamp(System.currentTimeMillis());
        LOG.info("Finished Getting Neighbourhood Data - " + timestamp);
    }

    private void addUser(Map<String, UserObj> userMap, ResultSet rs) {
        try {
            UserObj user = new UserObj();
            user.setUserId("userDetails/" + rs.getString("id") + "-userdata.json");

            UserDetails userDetails = new UserDetails();
            userDetails.setTwitterhandle(rs.getString("twitterhandle"));
            userDetails.setFirstname(rs.getString("firstname"));
            userDetails.setLastname(rs.getString("lastname"));
            userDetails.setGender(rs.getString("gender"));
            userDetails.setPhoto_prefix(rs.getString("photo_prefix"));
            userDetails.setPhoto_suffix(rs.getString("photo_suffix"));
            user.setUserDetails(userDetails);

            Activity activity = new Activity();
            activity.setCheckinCount(rs.getInt("Checkins"));

            List<String> DayHour_Checkin_Count = Arrays.asList(rs.getString("DayHour_Checkin_Count").split(","));
            Map<Integer, Integer> map1 = new HashMap<>();
            DayHour_Checkin_Count.parallelStream().forEach((item) -> {
                List<String> DayHour_Data = Arrays.asList(item.split(":"));
                try {
                    map1.put( Integer.parseInt(DayHour_Data.get(0)), Integer.parseInt(DayHour_Data.get(1)));
                } catch (Exception e){
                    try {
                        LOG.error(rs.getString("DayHour_Checkin_Count") + " " + item + " " + DayHour_Data + " " + rs.getString("id"));
                    } catch (Exception e1) {
                        LOG.error(e1);
                    }
                }
            });
            activity.setDayhourCheckinCount(map1);

            List<String> Category_Count = Arrays.asList(rs.getString("Category_Count").split(","));
            Map<String, Integer> map2 = new HashMap<>();
            Category_Count.parallelStream().forEach((item) -> {
                List<String> CategoryCount_Data = Arrays.asList(item.split(":"));
                try {
                    map2.put(CategoryCount_Data.get(0), Integer.parseInt(CategoryCount_Data.get(1)));
                } catch (Exception e){
                    try {
                        LOG.error(rs.getString("DayHour_Checkin_Count") + " " + item + " " + CategoryCount_Data + " " + rs.getString("id"));
                    } catch (Exception e1) {
                        LOG.error(e1);
                    }
                }
            });
            activity.setCategoryCount(map2);

            user.setVenueActivity(activity);

            userMap.put(user.getUserId(), user);
        } catch (SQLException e ) {
            LOG.error(e);
        }
    }

    private void addVenue(Map<String, VenueObj> venueMap, ResultSet rs) {
        try {
            VenueObj venue = new VenueObj();
            venue.setVenueId("venueDetails/" + rs.getString("id") + "-venuedata.json");

            VenueDetails venueDetails = new VenueDetails();
            venueDetails.setName(rs.getString("name"));
            venueDetails.setLat(rs.getDouble("lat"));
            venueDetails.setLng(rs.getDouble("lng"));
            venueDetails.setContact_phone(rs.getString("contact_phone"));
            venueDetails.setContact_formattedPhone(rs.getString("contact_formattedPhone"));
            venueDetails.setContact_twitter(rs.getString("contact_twitter"));
            venueDetails.setContact_instagram(rs.getString("contact_instagram"));
            venueDetails.setContact_facebook(rs.getString("contact_facebook"));
            venueDetails.setContact_facebookusername(rs.getString("contact_facebookusername"));
            venueDetails.setContact_facebookname(rs.getString("contact_facebookname"));
            venueDetails.setLocation_address(rs.getString("location_address"));
            venueDetails.setLocation_formattedaddress(rs.getString("location_formattedaddress"));
            venueDetails.setLocation_crossstreet(rs.getString("location_crossstreet"));
            venueDetails.setLocation_postalcode(rs.getString("location_postalcode"));
            venueDetails.setLocation_cc(rs.getString("location_cc"));
            venueDetails.setLocation_neighbourhood(rs.getString("location_neighbourhood"));
            venueDetails.setLocation_city(rs.getString("location_city"));
            venueDetails.setLocation_state(rs.getString("location_state"));
            venueDetails.setLocation_country(rs.getString("location_country"));
            venueDetails.setVerified(rs.getBoolean("verified"));
            venueDetails.setStats_checkincount(rs.getInt("stats_checkincount"));
            venueDetails.setStats_usercount(rs.getInt("stats_usercount"));
            venueDetails.setUrl(rs.getString("url"));
            venueDetails.setCategoryId(rs.getString("Category"));
            venue.setVenueDetails(venueDetails);

            Activity activity = new Activity();
            activity.setCheckinCount(rs.getInt("Checkins"));
            List<String> DayHour_SplitList = Arrays.asList(rs.getString("DayHour_Checkin_Count").split(","));
            Map<Integer, Integer> map = new HashMap<>();
            DayHour_SplitList.parallelStream().forEach((item) -> {
                List<String> DayHour_Data = Arrays.asList(item.split(":"));
                try {
                    map.put( Integer.parseInt(DayHour_Data.get(0)), Integer.parseInt(DayHour_Data.get(1)));
                } catch (Exception e){
                    try {
                        LOG.error(rs.getString("DayHour_Checkin_Count") + " " + item + " " + DayHour_Data + " " + rs.getString("id"));
                    } catch (Exception e1) {
                        LOG.error(e1);
                    }
                }
            });
            activity.setDayhourCheckinCount(map);
            Set<String> Venue_Users = new HashSet<>(Arrays.asList(rs.getString("Venue_Users").split(",")));
            activity.setUserIds(Venue_Users);
            venue.setVenueActivity(activity);

            venueMap.put(venue.getVenueId(), venue);
        } catch (SQLException e ) {
            LOG.error(e);
        }
    }


    private String getJSON(Object obj) {
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(obj);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonInString;
    }

    public void setVenueNeighbourhood(String id, String neighbourhood){
        venueMap.get(id).setNeighbourhoodModel(neighbourhood);
    }
}
