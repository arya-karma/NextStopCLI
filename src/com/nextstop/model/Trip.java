package com.nextstop.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Trip {
    private String tripId;
    private int userId;
    private String destination;
    private int durationDays;
    private double totalBudget;
    private List<Activity> activities;
    private List<PackingItem> packingList;

    public Trip(String tripId, int userId, String destination, int durationDays, double totalBudget) {
        this.tripId = tripId;
        this.userId = userId;
        this.destination = destination;
        this.durationDays = durationDays;
        this.totalBudget = totalBudget;
        this.activities = new ArrayList<>();
        this.packingList = new ArrayList<>();
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
        activities.sort(Comparator.comparingInt(Activity::getDayNumber));
    }

    public void addPackingItem(PackingItem item) {
        packingList.add(item);
    }

    public List<Activity> getActivitiesSortedByCostDesc() {
        List<Activity> sorted = new ArrayList<>(activities);
        sorted.sort(Comparator.comparingDouble(Activity::getCost).reversed());
        return sorted;
    }

    public Map<ActivityCategory, Double> getCategoryExpenseBreakdown() {
        Map<ActivityCategory, Double> breakdown = new EnumMap<>(ActivityCategory.class);
        for (Activity act : activities) {
            ActivityCategory cat = act.getCategory();
            double current = breakdown.getOrDefault(cat, 0.0);
            breakdown.put(cat, current + act.getCost());
        }
        return breakdown;
    }

    public double getTotalSpent() {
        double total = 0.0;
        for (Activity act : activities) {
            total += act.getCost();
        }
        return total;
    }

    public double getRemainingBudget() {
        return totalBudget - getTotalSpent();
    }

    public String getTripId() { return tripId; }
    public int getUserId() { return userId; }
    public String getDestination() { return destination; }
    public int getDurationDays() { return durationDays; }
    public double getTotalBudget() { return totalBudget; }
    public List<Activity> getActivities() { return activities; }
    public List<PackingItem> getPackingList() { return packingList; }
}