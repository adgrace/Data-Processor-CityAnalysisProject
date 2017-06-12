package com.alexgrace.finalyearproject.dataprocessor.entities;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Activity {
    private int checkinCount;
    private int distinctUsers;
    private Map<Integer, Integer> dayhourCheckinCount;

    public Map<String, Integer> getCategoryCount() {
        return CategoryCount;
    }

    public void setCategoryCount(Map<String, Integer> categoryCount) {
        CategoryCount = categoryCount;
    }

    private Map<String, Integer> CategoryCount;
    private transient Set<String> userIds = new HashSet<>();

    public int getCheckinCount() {
        return checkinCount;
    }
    public int getDistinctUsers() { return userIds.size(); }
    public Map<Integer, Integer> getDayhourCheckinCount() {
        return dayhourCheckinCount;
    }
    public Set<String> getUserIds() {
        return userIds;
    }

    public void setCheckinCount( int checkinCount) {
        this.checkinCount = checkinCount;
    }
    public void setDayhourCheckinCount( Map<Integer, Integer> dayhourCheckinCount ) {
        this.dayhourCheckinCount = dayhourCheckinCount;
    }
    public void setUserIds(Set<String> userIds) {
        this.userIds = userIds;
    }
}
