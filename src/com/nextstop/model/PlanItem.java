package com.nextstop.model;

import java.io.Serializable;

public abstract class PlanItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String title;
    private double cost;

    public PlanItem(String title, double cost) {
        this.title = title;
        this.cost = cost;
    }

    public String getTitle() { return title; }
    public double getCost() { return cost; }

    public abstract String getDetails();
}