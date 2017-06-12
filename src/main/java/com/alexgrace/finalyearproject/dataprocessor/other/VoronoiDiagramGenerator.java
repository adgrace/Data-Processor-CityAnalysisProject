package com.alexgrace.finalyearproject.dataprocessor.other;

import com.alexgrace.finalyearproject.dataprocessor.entities.LocationFilter;
import com.alexgrace.finalyearproject.dataprocessor.entities.VenueObj;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;


/**
 * Created by AlexGrace on 02/05/2017.
 */
public class VoronoiDiagramGenerator {

    private static final Log LOG = LogFactory.getLog(VoronoiDiagramGenerator.class);

    private VoronoiDiagramBuilder voronoiDiagramGenerator = new VoronoiDiagramBuilder();

    private List<Datapoint> datapointList;
    private Map<String, VenueObj> venueData;
    private int[] Clusters;
    private GeometryCollection output;
    private LocationFilter location;



    public VoronoiDiagramGenerator(List<Datapoint> datapointList, Map<String, VenueObj> venueData, int[] Clusters, LocationFilter location) {
        this.datapointList = datapointList;
        this.venueData = venueData;
        this.Clusters = Clusters;
        this.location = location;
    }

    public GeometryCollection createVoronoi() {
        // clusters to coordinates 4 sites
        List<Coordinate> coordinateList = new ArrayList<>();
        for (int i=0; i < Clusters.length; i++) {
            String id = datapointList.get(Clusters[i]).getId();
            Coordinate coordinate = new Coordinate(venueData.get(id).getVenueDetails().getLat(), venueData.get(id).getVenueDetails().getLng());
            coordinateList.add(coordinate);
        }

        voronoiDiagramGenerator.setSites(coordinateList);

        double kmInLongitudeDegree = 111.320 * Math.cos( (double)location.getLat() / 180.0 * Math.PI);
        double radius = (double)location.getRadius();
        double deltaLat = radius / (111.13*2);
        double deltaLng = radius / (kmInLongitudeDegree*2);

        LOG.info(deltaLat + " " + deltaLng);

        double maxx = location.getLat() + deltaLat;
        double minx = location.getLat() - deltaLat;
        double maxy = location.getLng() + deltaLng;
        double miny = location.getLng() - deltaLng;
        LOG.info(maxx + " " + minx + " " + maxy + " " + miny);
        Envelope envelope = new Envelope(maxx, minx, maxy, miny);

        voronoiDiagramGenerator.setClipEnvelope(envelope);
        GeometryCollection geometryCollection = (GeometryCollection)voronoiDiagramGenerator.getDiagram(new GeometryFactory());

        return geometryCollection;
    }
}
