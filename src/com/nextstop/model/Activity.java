package com.nextstop.model;

public class Activity extends PlanItem {
    private static final long serialVersionUID = 1L;
    private int activityId;
    private String tripId;
    private int dayNumber;
    private ActivityCategory category;

    // Constructor for creating a new activity before DB insert
    public Activity(String tripId, int dayNumber, String title, ActivityCategory category, double cost) {
        this(0, tripId, dayNumber, title, category, cost);
    }

    // Constructor for loading an existing activity with its DB primary key
    public Activity(int activityId, String tripId, int dayNumber, String title, ActivityCategory category, double cost) {
        super(title, cost);
        this.activityId = activityId;
        this.tripId = tripId;
        this.dayNumber = dayNumber;
        this.category = category;
    }

    public int getActivityId() { return activityId; }
    public void setActivityId(int activityId) { this.activityId = activityId; }
    public String getTripId() { return tripId; }
    public int getDayNumber() { return dayNumber; }
    public ActivityCategory getCategory() { return category; }

    @Override
    public String getDetails() {
        return String.format("[#%d] Day %d | [%-11s] %-22s | Rs. %.2f",
                activityId, dayNumber, category.name(), getTitle(), getCost());
    }
}