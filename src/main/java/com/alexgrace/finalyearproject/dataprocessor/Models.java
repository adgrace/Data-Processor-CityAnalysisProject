package com.alexgrace.finalyearproject.dataprocessor;

import com.alexgrace.finalyearproject.dataprocessor.entities.*;
import com.alexgrace.finalyearproject.dataprocessor.other.*;
//import com.alexgrace.finalyearproject.dataprocessor.other.VoronoiDiagramGenerator;
import com.clust4j.algo.KMedoids;
import com.clust4j.algo.KMedoidsParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.geojson.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;


public class Models {

    private static final Log LOG = LogFactory.getLog(Models.class);

    private Map<String, VenueObj> venueData = new HashMap<>();
    private Map<String, UserObj> userData = new HashMap<>();
    private Map<String, FeatureCollection> neighbourhoodData;
    private LocationFilter location;
    private Map<String, String> ChildtoParentCategoryMap = new HashMap<>();


    private GeometryFactory geometryFactory = new GeometryFactory();

    Map<String, Integer> venueIndex1 = new HashMap<>();
    Map<Integer, String> venueIndex2 = new HashMap<>();


    public Models(Map<String, VenueObj> venueData, Map<String, UserObj> userData, Map<String, FeatureCollection> neighbourhoodData, LocationFilter location) {
        this.neighbourhoodData = neighbourhoodData;
        this.location = location;
        this.userData = userData;

        // Venue Data Filter
        venueData.entrySet().parallelStream().forEach(entry -> {
            String key = entry.getKey();
            VenueObj venue = entry.getValue();
            Double centerLat = location.getLat();
            Double centerLng = location.getLng();
            Integer radius = location.getRadius();
            Double pointLat = venue.getVenueDetails().getLat();
            Double pointLng = venue.getVenueDetails().getLng();

            Double dist = Math.sin(Math.toRadians(pointLat)) * Math.sin(Math.toRadians(centerLat)) + Math.cos(Math.toRadians(pointLat)) * Math.cos(Math.toRadians(centerLat)) * Math.cos(Math.toRadians(pointLng - centerLng));
            dist = Math.toDegrees(Math.acos(dist)) * 60 * 1.1515 * 1.609344;

            if (dist < radius) {
                this.venueData.put(key, venue);
            }
        });

        // Generate venueIndex
        Integer loopIndex = 0;
        for (Map.Entry<String, VenueObj> entry : venueData.entrySet()) {
            venueIndex1.put(entry.getKey(), loopIndex);
            loopIndex= loopIndex+1;
        }
    }

    public DataReturnValues neighbourhood() {

        // Get feature collection for the relevant location
        FeatureCollection featureCollection = neighbourhoodData.get(location.getName());

        Set<Feature> featureSet = new HashSet<>();
        Map<String, PolygonObj> polygonMap = new HashMap<>();
        Map<String, String> venueNeighbourhoodMap = new HashMap<>();

        String featureCollectionName = "featureCollection/" + location.getName() + "-neighbourhoodmodel-data.geojson";

        // Loop through each Feature in the feature collection
        featureCollection.forEach(feature -> {

            //Variables
            GeoJsonObject geoObject = feature.getGeometry();
            Map<String, Object> properties = feature.getProperties();
            com.vividsolutions.jts.geom.Geometry polygon;
            Set<String> polygonUsers = new HashSet<>();     // Users within feature
            Set<String> polygonVenues = new HashSet<>();    // Venues within feature
            Map<Integer, Integer> dayhourCheckinCount = new HashMap<>();
            PolygonCheckinCount polygonCheckinCount = new PolygonCheckinCount();

            LOG.info("Neighbourhood Processing Feature: " + properties.get("name") + ", " + location.getName());

            String polygonName = "polygonDetails/" + properties.get("cartodb_id") + "-" + properties.get("name").toString().replaceAll("\\s+","") + "-" + location.getName() + "-neighbourhoodmodel-polygondata.json";

            // Convert Feature Coordinates into polygon(s)
            if (geoObject instanceof org.geojson.Polygon) {
                polygon = convert((org.geojson.Polygon) geoObject);
            } else {
                polygon = convert((org.geojson.MultiPolygon) geoObject);
            }

            // For each venue
            venueData.entrySet().parallelStream().forEach(entry -> {
                VenueObj venueValue = entry.getValue();
                String venueKey = entry.getKey();

                // Create point for the venues location
                com.vividsolutions.jts.geom.Point point = geometryFactory.createPoint(new Coordinate(venueValue.getVenueDetails().getLat(), venueValue.getVenueDetails().getLng()));

                // Test if venue is within the polygon
                if (point.within(polygon)) {

                    // Add venues to polygons
                    polygonVenues.add(venueKey);

                    // Add Users to Polygon
                    polygonUsers.addAll(venueValue.getVenueActivity().getUserIds());

                    // Add Polygon Activity - dayhourCheckinCount
                    Map<Integer, Integer> venueActivityMap = venueValue.getVenueActivity().getDayhourCheckinCount();
                    venueActivityMap.entrySet().parallelStream().forEach(venueActivity -> {
                        Integer venueActivityKey = venueActivity.getKey();
                        Integer venueActivityValue = venueActivity.getValue();
                        Integer currentValue = dayhourCheckinCount.get(venueActivityKey);
                        if (currentValue != null) {
                            dayhourCheckinCount.replace(venueActivityKey, currentValue + venueActivityValue);
                        } else {
                            dayhourCheckinCount.put(venueActivityKey, venueActivityValue);
                        }
                    });

                    // Add Polygon Activity - numberOfCheckins
                    polygonCheckinCount.incrementPolygonCheckinCount(venueValue.getVenueActivity().getCheckinCount());

                    // Add neighbourhood to venue data
                    venueNeighbourhoodMap.put(venueKey, polygonName);
                }
            }); // End of Venue Loop

            //Set polygonMap Data
            PolygonObj polygonObj = new PolygonObj();
            polygonObj.setPolygonId(polygonName);
            polygonObj.setPolygonName(properties.get("name").toString());
            polygonObj.setPolygonCountry(location.getName());
            Activity polygonActivity = new Activity();
            polygonActivity.setCheckinCount(polygonCheckinCount.getPolygonCheckinCount());
            polygonActivity.setDayhourCheckinCount(dayhourCheckinCount);
            polygonActivity.setUserIds(polygonUsers);
            polygonObj.setVenuesInPolygon(polygonVenues);
            polygonObj.setPolygonActivity(polygonActivity);
            polygonMap.put(polygonName , polygonObj);


            // Add current feature
            featureSet.add(cleanupFeature(feature, polygonName));

        }); // End of Feature Loop

        FeatureCollection newFeatureCollection = new FeatureCollection();
        newFeatureCollection.addAll(featureSet);

        DataReturnValues returnObject = new DataReturnValues(featureCollectionName, newFeatureCollection, polygonMap, venueNeighbourhoodMap);
        return returnObject;
    }



    public DataReturnValues venuebased() {
        //Calculate Distance & cluster
        LOG.info("venueData size: " + venueData.size());
        List<Datapoint> datapointList = generateDataPoints(venueData);
        LOG.info("DatapointList size: " + datapointList.size());
        KMetoids kMetoids = new KMetoids(40, datapointList);
        int[] kMetoidsClusters = kMetoids.getClusters();

        //Voronoi Diagram
        VoronoiDiagramGenerator voronoiDiagramGenerator = new VoronoiDiagramGenerator(datapointList, venueData, kMetoidsClusters, location);
        GeometryCollection geometryCollection = voronoiDiagramGenerator.createVoronoi();

        Set<Feature> featureSet = new HashSet<>();
        Map<String, PolygonObj> polygonMap = new HashMap<>();
        Map<String, String> venueNeighbourhoodMap = new HashMap<>();

        String featureCollectionName = "featureCollection/" + location.getName() + "-venuemodel-data.geojson";

        for (int i=0; i < geometryCollection.getNumGeometries(); i++) {
            Geometry geometry = geometryCollection.getGeometryN(i);
            Set<String> polygonUsers = new HashSet<>();     // Users within feature
            Set<String> polygonVenues = new HashSet<>();    // Venues within feature
            Map<Integer, Integer> dayhourCheckinCount = new HashMap<>();
            PolygonCheckinCount polygonCheckinCount = new PolygonCheckinCount();

            String polygonName = "polygonDetails/" + i + "-" + location.getName() + "-venuemodel-polygondata.json";
            LOG.info("Venue Model Processing Feature: " + polygonName);

            // For each venue
            venueData.entrySet().parallelStream().forEach(entry -> {
                VenueObj venueValue = entry.getValue();
                String venueKey = entry.getKey();

                // Create point for the venues location
                com.vividsolutions.jts.geom.Point point = geometryFactory.createPoint(new Coordinate(venueValue.getVenueDetails().getLat(), venueValue.getVenueDetails().getLng()));

                // Test if venue is within the polygon
                if (point.within(geometry)) {
                    // Add venues to polygons
                    polygonVenues.add(venueKey);

                    // Add Users to Polygon
                    polygonUsers.addAll(venueValue.getVenueActivity().getUserIds());

                    // Add Polygon Activity - dayhourCheckinCount
                    Map<Integer, Integer> venueActivityMap = venueValue.getVenueActivity().getDayhourCheckinCount();
                    venueActivityMap.entrySet().parallelStream().forEach(venueActivity -> {
                        Integer venueActivityKey = venueActivity.getKey();
                        Integer venueActivityValue = venueActivity.getValue();
                        Integer currentValue = dayhourCheckinCount.get(venueActivityKey);
                        if (currentValue != null) {
                            dayhourCheckinCount.replace(venueActivityKey, currentValue + venueActivityValue);
                        } else {
                            dayhourCheckinCount.put(venueActivityKey, venueActivityValue);
                        }
                    });

                    // Add Polygon Activity - numberOfCheckins
                    polygonCheckinCount.incrementPolygonCheckinCount(venueValue.getVenueActivity().getCheckinCount());

                    // Add neighbourhood to venue data
                    venueNeighbourhoodMap.put(venueKey, polygonName);
                };
            }); // End of Venue Loop

            //Set polygonMap Data
            PolygonObj polygonObj = new PolygonObj();
            polygonObj.setPolygonId(polygonName);
            polygonObj.setPolygonName(Integer.toString(i));
            polygonObj.setPolygonCountry(location.getName());
            Activity polygonActivity = new Activity();
            polygonActivity.setCheckinCount(polygonCheckinCount.getPolygonCheckinCount());
            polygonActivity.setDayhourCheckinCount(dayhourCheckinCount);
            polygonActivity.setUserIds(polygonUsers);
            polygonObj.setVenuesInPolygon(polygonVenues);
            polygonObj.setPolygonActivity(polygonActivity);
            polygonMap.put(polygonName , polygonObj);


            // Add current feature
            featureSet.add(cleanupFeature(geometry, polygonName));

        }

        FeatureCollection newFeatureCollection = new FeatureCollection();
        newFeatureCollection.addAll(featureSet);

        DataReturnValues returnObject = new DataReturnValues(featureCollectionName, newFeatureCollection, polygonMap, venueNeighbourhoodMap);
        return returnObject;
    }



    public DataReturnValues userbased() {

        //Calculate Distance & cluster
        LOG.info("userData size: " + userData.size());
        List<Datapoint> datapointList = generateUserPoints(userData);
        LOG.info("Userbased datapoint List Size: " + datapointList.size());
        LOG.info("Userbased number of descriptors: " + datapointList.get(0).getInstanceList().size());
        KMetoids kMetoids = new KMetoids(20, datapointList);
        LOG.info("Starting K-Metoids clustering");
        int[] kMetoidsClusters = kMetoids.getClusters();
        LOG.info("Finished K-Metoids clustering, " + kMetoidsClusters.length + "clusters. Values: " + kMetoidsClusters);

        int[] assignment = kMetoids.assign(kMetoidsClusters, datapointList);
        Map<Integer, Set<String>> clustergroupings = new HashMap<>();
        for (int i = 0; i < assignment.length; i++) {
            Set<String> userIdSet = new HashSet<>();
            if (clustergroupings.containsKey(assignment[i])) {
                userIdSet = clustergroupings.get(assignment[i]);
            }
            userIdSet.add(datapointList.get(i).getId());
            clustergroupings.put(assignment[i],userIdSet);
        }
        for (int j = 0; j < clustergroupings.size(); j++) {
            String userString = "";
            for(String item : clustergroupings.get(j)){
                if (userString.equals("")) {
                    userString = item;
                } else {
                    userString = userString + ", " + item;
                }
            }
            LOG.info("User Cluster Num: " + j + ", Size: " + clustergroupings.get(j).size() + ", Centroid: " + datapointList.get(kMetoidsClusters[j]).getId() + ", List: " + userString);
        }


        System.exit(0);
        DataReturnValues returnObject = new DataReturnValues(null, null, null, null);
        return returnObject;
    }

    /*public DataReturnValues hybrid() {
        //Calculate Distance & cluster
        EuclideanDistance euclideanDistance = new EuclideanDistance();
        Array2DRowRealMatrix mat = new Array2DRowRealMatrix(generateDistanceMatrix(venueData, (coordinateA, coordinateB) -> euclideanDistance.HybridModel(coordinateA, coordinateB)));
        KMedoids km = new KMedoidsParameters(50).fitNewModel(mat);
        int[] results = km.getLabels();
        LOG.info(results);

        DataReturnValues returnObject = new DataReturnValues(null, null, null, null);
        return returnObject;
    }*/

    public class PolygonCheckinCounter {
        private Integer count = 0;

        public void addToCount(Integer count) {
            this.count =+ count;
        }

        public Integer getCount() {
            return count;
        }
    }

    org.geojson.Polygon reduce(org.geojson.Polygon polygon) {
        org.geojson.Polygon newPolygon = new org.geojson.Polygon();

        List<List<LngLatAlt>> reduced = reducePolygon(polygon.getCoordinates());
        newPolygon.setCoordinates(reduced);
        return newPolygon;
    }

    org.geojson.MultiPolygon reduce(org.geojson.MultiPolygon multiPolygon) {
        int size = multiPolygon.getCoordinates().size();
        List<List<List<LngLatAlt>>> polygons = new ArrayList<>();

        org.geojson.MultiPolygon newMultiPolygon = new org.geojson.MultiPolygon();
        for (int i = 0; i < size; i++) {
            polygons.add(reducePolygon(multiPolygon.getCoordinates().get(i)));
        }
        newMultiPolygon.setCoordinates(polygons);
        return newMultiPolygon;
    }

    List<List<LngLatAlt>> reducePolygon(List<List<LngLatAlt>> polygon) {
        List<List<LngLatAlt>> reducedPolygon = new ArrayList<>();

        int size1 = polygon.size();
        for (int i = 0; i < size1; i++) {
            List<LngLatAlt> List1 = new ArrayList<>();
            int size2 = polygon.get(i).size();
            double previousLat = 0.0;
            double previousLng = 0.0;
            for (int j = 0; j < size2; j++) {
                LngLatAlt lngLatAlt = new LngLatAlt();
                double lat = round(polygon.get(i).get(j).getLatitude(), 3);
                double lng = round(polygon.get(i).get(j).getLongitude(),3);
                lngLatAlt.setLatitude(lat);
                lngLatAlt.setLongitude(lng);
                if ((previousLat != lat) || (previousLng != lng)) {
                    List1.add(lngLatAlt);
                    previousLat = lat;
                    previousLng = lng;
                }
            }
            if (List1.size() != 0) {
                reducedPolygon.add(List1);
            }
        }

        return reducedPolygon;
    }

    double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    com.vividsolutions.jts.geom.Geometry convert(org.geojson.Polygon polygon) {
        return convertToPolygon(polygon.getCoordinates());
    }

    com.vividsolutions.jts.geom.Geometry convert(org.geojson.MultiPolygon multiPolygon) {
        int size = multiPolygon.getCoordinates().size();
        com.vividsolutions.jts.geom.Polygon[] polygons = new com.vividsolutions.jts.geom.Polygon[size];
        for (int i = 0; i < size; i++) {
            polygons[i] = convertToPolygon(multiPolygon.getCoordinates().get(i));
        }
        return geometryFactory.createMultiPolygon(polygons);
    }

    com.vividsolutions.jts.geom.Polygon convertToPolygon(List<List<LngLatAlt>> coordinates) {
        LinearRing shell = geometryFactory.createLinearRing(convert(coordinates.get(0)));

        if (coordinates.size() > 1) {
            int size = coordinates.size() - 1;
            LinearRing[] holes = new LinearRing[size];
            for (int i = 0; i < size; i++) {
                holes[i] = geometryFactory.createLinearRing(convert(coordinates.get(i + 1)));
            }
            return geometryFactory.createPolygon(shell, holes);
        } else {
            return geometryFactory.createPolygon(shell);
        }
    }

    com.vividsolutions.jts.geom.Coordinate[] convert(List<LngLatAlt> coordinateList) {
        Coordinate[] coordinates = new Coordinate[coordinateList.size()];
        for (int i = 0; i < coordinateList.size(); i++) {
            coordinates[i] = new Coordinate(round(coordinateList.get(i).getLatitude(),6), round(coordinateList.get(i).getLongitude(),6));
        }
        return coordinates;
    }

    Feature cleanupFeature(Feature feature, String polygonDataUrl) {
        Map<String, Object> properties = feature.getProperties();

        GeoJsonObject featureGeometry = feature.getGeometry();
        if (featureGeometry instanceof org.geojson.Polygon) {
            featureGeometry = reduce((org.geojson.Polygon) featureGeometry);
        } else {
            featureGeometry = reduce((org.geojson.MultiPolygon) featureGeometry);
        }
        feature.setGeometry(featureGeometry);

        properties.put("polygonDataUrl", polygonDataUrl);
        properties.remove("name");
        properties.remove("cartodb_id");
        properties.remove("created_at");
        properties.remove("updated_at");
        feature.setProperties(properties);
        return feature;
    }

    Feature cleanupFeature(Geometry geometry, String polygonDataUrl) {
        GeoJsonObject featureGeometry = null;

        if (geometry.getGeometryType() == "Polygon") {
            Coordinate[] geometryCoordinates = geometry.getCoordinates();
            org.geojson.Polygon polygon = new org.geojson.Polygon();

            List<LngLatAlt> lngLatAltList = new ArrayList<>();
            for (int i=0; i < geometryCoordinates.length; i++) {
                LngLatAlt lngLatAlt = new LngLatAlt();
                lngLatAlt.setLatitude(geometryCoordinates[i].x);
                lngLatAlt.setLongitude(geometryCoordinates[i].y);
                lngLatAltList.add(lngLatAlt);
            }
            polygon.add(lngLatAltList);
            featureGeometry = polygon;
        } else {
            LOG.error("Geometry type: " + geometry.getGeometryType());
        }

        Feature feature = new Feature();

        if (featureGeometry instanceof org.geojson.Polygon) {
            featureGeometry = reduce((org.geojson.Polygon) featureGeometry);
        } else {
            featureGeometry = reduce((org.geojson.MultiPolygon) featureGeometry);
        }
        feature.setGeometry(featureGeometry);

        Map<String, Object> properties = new HashMap<>();
        properties.put("polygonDataUrl", polygonDataUrl);
        feature.setProperties(properties);
        feature.setGeometry(featureGeometry);
        return feature;
    }

    private class PolygonCheckinCount {
        private Integer polygonCheckinCount = 0;

        public Integer getPolygonCheckinCount() {
            return polygonCheckinCount;
        }

        public void incrementPolygonCheckinCount(Integer addPolygonCheckinCount) {
            this.polygonCheckinCount = this.polygonCheckinCount + addPolygonCheckinCount;
        }
    }

    private void recursivelyAddCategories(FoursquareVenueCategories foursquareVenueCategories, String parent) {
        ChildtoParentCategoryMap.put(foursquareVenueCategories.getId(),parent);
        foursquareVenueCategories.getCategories().forEach(item->{
            recursivelyAddCategories(item, parent);
        });
    }

    private List<Datapoint> generateUserPoints(Map<String, UserObj> userData) {
        List<Datapoint> datapointList = new ArrayList<>();
        for (Map.Entry<String, UserObj> entry : userData.entrySet()) {
            List<Instance> instanceList = new ArrayList<>();

            // Categories
            //userData.get(entry.getKey()).getVenueActivity().getCategoryCount();
            ObjectMapper mapper = new ObjectMapper();
            categories objectMap = new categories();
            try {
                objectMap = mapper.readValue(new File("foursquare-categories.json"), categories.class);
            } catch (IOException e)  {
                e.printStackTrace();
            }
            Map<String,Double> parentCategoryCount = new HashMap<>();
            objectMap.getCategories().forEach(item->{
                parentCategoryCount.put(item.getId(),0.0);
                ChildtoParentCategoryMap.put(item.getId(),item.getId());
                item.getCategories().forEach(item2->{
                    recursivelyAddCategories(item2, item.getId());
                });
            });
            Integer Total = 0;
            Map<String,Integer> categoryCountPairs = new HashMap<>(userData.get(entry.getKey()).getVenueActivity().getCategoryCount());
            for (Map.Entry<String,Integer> entry1 : categoryCountPairs.entrySet()) {
                String catKey  = entry1.getKey();
                Integer catValue = entry1.getValue();
                String parent = ChildtoParentCategoryMap.get(catKey);
                Double oldCount = parentCategoryCount.get(parent);
                parentCategoryCount.put(parent, (oldCount + catValue));
                Total = Total + catValue;
            }

            for (Map.Entry<String,Double> entry0 : parentCategoryCount.entrySet()) {
                if (entry0.getValue() != 0) {
                    parentCategoryCount.put(entry0.getKey(), (entry0.getValue()/Total));
                }
                Instance category = new Instance();
                category.value = parentCategoryCount.get(entry0.getKey());
                category.weight = 1.0;
                category.type = "coordinate";
                instanceList.add(category);
            }


            // Checkin Times - Gaussian Distributed
            // 0 = 0.4, 1 = 0.24, 2 = 0.07, 3 = 0.01
            Map<Integer,Double> weightedtimecountpair = new HashMap<>();
            for(int x = 0; x < 24; x = x + 1) {
                weightedtimecountpair.put(x,0.0);
            }

            Map<Integer,Integer> timecountpair = new HashMap<>(userData.get(entry.getKey()).getVenueActivity().getDayhourCheckinCount());
            for (Map.Entry<Integer,Integer> entry2 : timecountpair.entrySet()) {
                Integer key0  = entry2.getKey();
                Integer value = entry2.getValue();

                Double weight0 = 0.4;
                Double weight1 = 0.24;
                Double weight2 = 0.07;
                Double weight3 = 0.01;

                Integer keyPlus1, keyMinus1, keyPlus2, keyMinus2, keyPlus3, keyMinus3;
                if (key0 == 0 ) {
                     keyPlus1 = 1;
                     keyMinus1 = 23;
                } else if (key0 == 23) {
                     keyPlus1 = 0;
                     keyMinus1 = 22;
                } else {
                     keyPlus1 = key0 + 1;
                     keyMinus1 = key0 - 1;
                }
                if (keyMinus1 == 0 ) {
                     keyPlus2 = keyPlus1 + 1;
                     keyMinus2 = 23;
                } else if (keyPlus1 == 23) {
                     keyPlus2 = 0;
                     keyMinus2 = keyMinus1 - 1;
                } else {
                     keyPlus2 = keyPlus1 + 1;
                     keyMinus2 = keyMinus1 - 1;
                }
                if (keyMinus2 == 0 ) {
                     keyPlus3 = keyPlus2 + 1;
                     keyMinus3 = 23;
                } else if (keyPlus2 == 23) {
                     keyPlus3 = 0;
                     keyMinus3 = keyMinus2 - 1;
                } else {
                    keyPlus3 = keyPlus2 + 1;
                    keyMinus3 = keyMinus2 - 1;
                }
                Double valueMinus3 = weightedtimecountpair.get(keyMinus3) + (value * weight3);
                Double valueMinus2 = weightedtimecountpair.get(keyMinus2) + (value * weight2);
                Double valueMinus1 = weightedtimecountpair.get(keyMinus1) + (value * weight1);
                Double value0 = weightedtimecountpair.get(key0) + (value * weight0);
                Double valuePlus1 = weightedtimecountpair.get(keyPlus1) + (value * weight1);
                Double valuePlus2 = weightedtimecountpair.get(keyPlus2) + (value * weight2);
                Double valuePlus3 = weightedtimecountpair.get(keyPlus3) + (value * weight3);

                weightedtimecountpair.put(keyMinus3,valueMinus3);
                weightedtimecountpair.put(keyMinus2,valueMinus2);
                weightedtimecountpair.put(keyMinus1,valueMinus1);
                weightedtimecountpair.put(key0,value0);
                weightedtimecountpair.put(keyPlus1,valuePlus1);
                weightedtimecountpair.put(keyPlus2,valuePlus2);
                weightedtimecountpair.put(keyPlus3,valuePlus3);
            }

            //Total
            Double total = 0.0;
            for(int x = 0; x < 24; x = x + 1) {
                total = total + weightedtimecountpair.get(x);
            }
            //Normalise
            for(int x = 0; x < 24; x = x + 1) {
                Double recalculated = weightedtimecountpair.get(x) / total;
                weightedtimecountpair.put(x,recalculated);
            }

            for(int x = 0; x < 24; x = x + 1) {
                Instance time = new Instance();
                time.value = weightedtimecountpair.get(x);
                time.weight = 1.0;
                time.type = "coordinate";
                instanceList.add(time);
            }
            Datapoint datapoint = new Datapoint(userData.get(entry.getKey()).getUserId(), instanceList);
            datapointList.add(datapoint);
        }
        return datapointList;
    }


    private List<Datapoint> generateDataPoints(Map<String, VenueObj> venueData) {
        List<Datapoint> datapointList = new ArrayList<>();
        for (Map.Entry<String, VenueObj> entry : venueData.entrySet()) {
            List<Instance> instanceList = new ArrayList<>();
            Instance xcoord = new Instance();
            xcoord.value = venueData.get(entry.getKey()).getVenueDetails().getLat();
            xcoord.weight = 1.0;
            xcoord.type = "coordinate";
            instanceList.add(xcoord);

            Instance ycoord = new Instance();
            ycoord.value = venueData.get(entry.getKey()).getVenueDetails().getLng();
            ycoord.weight = 1.0;
            ycoord.type = "coordinate";
            instanceList.add(ycoord);

            Instance userlist = new Instance();
            userlist.weight = 0.0001;
            userlist.type = "userlist";
            userlist.userIdSet = venueData.get(entry.getKey()).getVenueActivity().getUserIds();
            instanceList.add(userlist);

            Datapoint datapoint = new Datapoint(venueData.get(entry.getKey()).getVenueId(), instanceList);
            datapointList.add(datapoint);
        }
        return datapointList;
    }



    private double[][] generateDistanceMatrix(Map<String, VenueObj> venueData, I methodInterface) {
        double[][] distanceMatrix =  new double[venueData.size()][venueData.size()];

        venueData.entrySet().parallelStream().forEach(entry1 -> {
            VenueObj venue1 = entry1.getValue();
            String key1 = entry1.getKey();
            Coordinate coordinate1 = new Coordinate(venue1.getVenueDetails().getLat(), venue1.getVenueDetails().getLng());

            venueData.entrySet().parallelStream().forEach(entry2 -> {
                VenueObj venue2 = entry2.getValue();
                String key2 = entry2.getKey();
                Coordinate coordinate2 = new Coordinate(venue2.getVenueDetails().getLat(), venue2.getVenueDetails().getLng());

                Double distance = methodInterface.modelMethod(coordinate1, coordinate2);
                distanceMatrix[venueIndex1.get(key1)][venueIndex1.get(key2)]=distance;
            });
        });

      return distanceMatrix;
    }
}

interface I {
    Double modelMethod(Coordinate coordinateA, Coordinate coordinateB);
}
