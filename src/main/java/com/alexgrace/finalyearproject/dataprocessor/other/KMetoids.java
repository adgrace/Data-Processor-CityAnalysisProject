package com.alexgrace.finalyearproject.dataprocessor.other;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.crypto.Data;
import java.util.*;

/**
 * Created by AlexGrace on 14/05/2017.
 */
public class KMetoids {

    private static final Log LOG = LogFactory.getLog(KMetoids.class);


    private int NumberOfClusters;
    private List<Datapoint> datapoints;
    private Random rand = new Random();

    public KMetoids(int NumberOfClusters, List<Datapoint> datapoints) {
        this.NumberOfClusters = NumberOfClusters;
        this.datapoints = datapoints;
    }

    public int[] getClusters() {
        boolean changed  = true;
        int[] metoids = new int[NumberOfClusters];
        List<List<Integer>> output = new ArrayList<>();

        // Random Assignment
        for (int i = 0; i < NumberOfClusters; i++) {
            //LOG.info(datapoints.size() + " : " + i);
            metoids[i] = rand.nextInt(datapoints.size());
            //LOG.info("metoid: " + metoids[i]);
        }

        //Iterative Calculate
        int count = 0;
        while(changed) {
            count++;
            LOG.info("Iteration count: " + count);
            int[] assignment = assign(metoids, datapoints);
            changed = recalculateMedoids(assignment, metoids, output, datapoints);
        }

        LOG.info("PAM Algo Done, iteration count: " + count);

        //return output;
        return metoids;
    }

    public int[] assign(int[] metoids, List<Datapoint> datapoints) {
        int[] out = new int[datapoints.size()];
        for (int i = 0; i < datapoints.size(); i++) {

            double bestDistance = calculateDistance(i, metoids[0]);
            int bestIndex = 0;
            for (int j = 1; j < metoids.length; j++) {
                double tempDist = calculateDistance(i,metoids[j]);
                if (tempDist < bestDistance) {
                    bestDistance = tempDist;
                    bestIndex = j;
                }
            }
            out[i] = bestIndex;

        }
        return out;
    }

    private boolean recalculateMedoids(int[] assignment, int[] metoids, List<List<Integer>> output, List<Datapoint> datapoints) {
        boolean changed = false;
        output.clear();
        for (int i = 0; i < NumberOfClusters; i++) {
            List<Integer> tempList = new ArrayList<>();
            for (int j = 0; j < assignment.length; j++) {
                if (assignment[j] == i) {
                    tempList.add(j);
                }
            }
            output.add(tempList);

            if (output.get(i).size() == 0) {
                metoids[i] = rand.nextInt(datapoints.size());
                changed = true;
                //LOG.info("not da same1");
            } else {
                //LOG.info("metoid: " + i + " count: " + output.get(i).size());
                int oldMetoid = metoids[i];

                //get centoid
                int instanceLength = datapoints.get(0).getInstanceList().size();
                double[] sumPosition = new double[instanceLength];

                for (int x=0; x < tempList.size(); x++) {
                    Datapoint datapointList = datapoints.get(tempList.get(x));
                    List<Instance> instanceList = datapointList.getInstanceList();
                    for (int y = 0; y<instanceList.size();y++) {
                        if (instanceList.get(y).type != "userlist") {
                            sumPosition[y] += instanceList.get(y).weight * instanceList.get(y).value;
                        }
                    }
                }

                List<Instance> instanceList = new ArrayList<>();
                for (int z = 0; z < instanceLength; z++) {
                    sumPosition[z] /= tempList.size();
                    Instance newInstance = new Instance();
                    newInstance.value=sumPosition[z];
                    newInstance.weight=1.0;
                    newInstance.type = "coordinate";
                    instanceList.add(newInstance);
                }
                Datapoint newDataPoint = new Datapoint(null, instanceList);

                //get closest
                int closest = tempList.get(0);
                double beatDistance = calculateDistance(closest, newDataPoint);

                for (int x=1; x < tempList.size(); x++) {
                    double tempDistance = calculateDistance(tempList.get(x),newDataPoint);
                    if (tempDistance < beatDistance) {
                        beatDistance = tempDistance;
                        closest=tempList.get(x);
                    }
                }

                metoids[i] = closest;

                if (metoids[i] != oldMetoid) {
                    //LOG.info("not da same2");
                    changed = true;
                }
            }

        }

        return changed;
    }

    private double calculateDistance(int A, int B) {
        Datapoint datapointA = datapoints.get(A);
        Datapoint datapointB = datapoints.get(B);
        double total = 0;

        int numberOfInstances = datapointA.getInstanceList().size();
        for (int i = 0; i < numberOfInstances; i++) {
            Instance instanceA = datapointA.getInstanceList().get(i);
            Instance instanceB = datapointB.getInstanceList().get(i);

            switch (instanceA.type) {
                case "coordinate":
                    total = total + (instanceA.weight*Math.pow((instanceA.value - instanceB.value),2));
                    break;
                case "userlist":
                    Set<String> userSet1 = new HashSet<>(instanceA.userIdSet);
                    Set<String> userSet2 = new HashSet<>(instanceB.userIdSet);
                    Set<String> shared = new HashSet<>(userSet1);
                    shared.retainAll(userSet2);
                    if (userSet1.size() == 0 || userSet2.size() == 0 || shared.size() == 0) {
                        //LOG.info("Instance UserID Size is 0. type: " + instanceA.type + ", userset1: " + userSet1.size() + ", userset2: " + userSet2.size() + ", shared: " + shared.size());
                        total = total + (instanceA.weight * 1);
                    } else {
                        /*if (shared.size() > 0 ) {
                            LOG.info("venue shared visitor: " + shared.size());
                        }*/
                        Integer commonality = Math.max((shared.size() / userSet1.size()), (shared.size()) / userSet2.size());
                        commonality = 1 - commonality;
                        total = total + (instanceA.weight * Math.pow(commonality, 2));
                    }
                    break;
            }
        }

        double distance = Math.sqrt(total);

        return distance;
    }

    private double calculateDistance(int A, Datapoint B) {
        Datapoint datapointA = datapoints.get(A);
        double total = 0;

        int numberOfInstances = B.getInstanceList().size();
        for (int i = 0; i < numberOfInstances; i++) {
            Instance instanceA = datapointA.getInstanceList().get(i);
            Instance instanceB = B.getInstanceList().get(i);

            total = total + Math.pow((instanceA.value - instanceB.value),2);
        }
        double distance = Math.sqrt(total);
        return distance;
    }
}
