package com.nextstop.model;

public enum ActivityCategory {
    TRANSPORT,
    STAY,
    FOOD,
    SIGHTSEEING,
    OTHER;

    public static ActivityCategory fromString(String input) {
        if (input == null || input.isBlank()) {
            return OTHER;
        }
        try {
            return ActivityCategory.valueOf(input.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}