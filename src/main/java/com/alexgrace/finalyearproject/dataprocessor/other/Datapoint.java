package com.alexgrace.finalyearproject.dataprocessor.other;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AlexGrace on 14/05/2017.
 */
public class Datapoint {
    private String id;
    private List<Instance> instanceList = new ArrayList<>();

    public Datapoint (String id, List<Instance> instanceList) {
        this.id = id;
        this.instanceList = instanceList;
    }

   public List<Instance> getInstanceList() {
        return instanceList;
   }

    public String getId() {
        return id;
    }

}
