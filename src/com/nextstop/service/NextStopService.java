package com.nextstop.service;

import com.nextstop.dao.ActivityDAO;
import com.nextstop.dao.PackingItemDAO;
import com.nextstop.dao.TripDAO;
import com.nextstop.dao.UserDAO;
import com.nextstop.exception.BudgetExceededException;
import com.nextstop.exception.InvalidTripDataException;
import com.nextstop.model.Activity;
import com.nextstop.model.ActivityCategory;
import com.nextstop.model.PackingItem;
import com.nextstop.model.Trip;
import com.nextstop.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NextStopService {
    private final Map<String, Trip> tripRegistry = new HashMap<>();
    private final UserDAO userDAO;
    private final TripDAO tripDAO;
    private final ActivityDAO activityDAO;
    private final PackingItemDAO packingItemDAO;
    private User currentUser;

    public NextStopService() {
        this.userDAO = new UserDAO();
        this.tripDAO = new TripDAO();
        this.activityDAO = new ActivityDAO();
        this.packingItemDAO = new PackingItemDAO();
    }

    public User signUp(String username, String password) throws InvalidTripDataException, SQLException {
        if (username == null || username.isBlank() || password == null || password.length() < 4) {
            throw new InvalidTripDataException("Username cannot be blank and password must be at least 4 characters.");
        }
        String cleanUser = username.trim().toLowerCase();
        if (userDAO.findByUsername(cleanUser) != null) {
            throw new InvalidTripDataException("Username '" + cleanUser + "' is already taken.");
        }

        User newUser = new User(cleanUser, hashPassword(password));
        userDAO.save(newUser);
        this.currentUser = newUser;
        tripRegistry.clear();
        return newUser;
    }

    public User signIn(String username, String password) throws InvalidTripDataException, SQLException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new InvalidTripDataException("Username and password are required.");
        }
        User user = userDAO.findByUsername(username.trim().toLowerCase());
        if (user == null || !user.getPasswordHash().equals(hashPassword(password))) {
            throw new InvalidTripDataException("Invalid username or password.");
        }

        this.currentUser = user;
        loadUserTrips();
        return user;
    }

    public void signOut() {
        this.currentUser = null;
        this.tripRegistry.clear();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private void loadUserTrips() throws SQLException {
        tripRegistry.clear();
        for (Trip trip : tripDAO.findByUserId(currentUser.getUserId())) {
            for (Activity act : activityDAO.findByTripId(trip.getTripId())) {
                trip.addActivity(act);
            }
            for (PackingItem item : packingItemDAO.findByTripId(trip.getTripId())) {
                trip.addPackingItem(item);
            }
            tripRegistry.put(trip.getTripId(), trip);
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }

    public void createTrip(String tripId, String destination, int days, double budget)
            throws InvalidTripDataException, SQLException {
        if (tripId == null || tripId.isBlank() || destination == null || destination.isBlank()) {
            throw new InvalidTripDataException("Trip ID and Destination cannot be blank.");
        }
        if (days <= 0 || budget <= 0) {
            throw new InvalidTripDataException("Duration (days) and Budget must be positive.");
        }
        String normalizedId = tripId.trim().toUpperCase();
        if (tripDAO.existsById(normalizedId)) {
            throw new InvalidTripDataException("Trip ID '" + normalizedId + "' already exists.");
        }

        Trip newTrip = new Trip(normalizedId, currentUser.getUserId(), destination.trim(), days, budget);
        tripDAO.save(newTrip);
        tripRegistry.put(normalizedId, newTrip);
    }

    public void addActivity(String tripId, int day, String title, ActivityCategory category, double cost)
            throws InvalidTripDataException, BudgetExceededException, SQLException {
        String normalizedId = tripId.trim().toUpperCase();
        Trip trip = tripRegistry.get(normalizedId);
        if (trip == null) {
            throw new InvalidTripDataException("No trip found with ID: " + tripId);
        }
        if (day < 1 || day > trip.getDurationDays()) {
            throw new InvalidTripDataException("Day must be between 1 and " + trip.getDurationDays() + ".");
        }
        if (title == null || title.isBlank()) {
            throw new InvalidTripDataException("Activity title cannot be empty.");
        }
        if (cost < 0) {
            throw new InvalidTripDataException("Activity cost cannot be negative.");
        }
        if (trip.getTotalSpent() + cost > trip.getTotalBudget()) {
            throw new BudgetExceededException(String.format(
                    "Cost Rs. %.2f exceeds remaining budget of Rs. %.2f for %s.",
                    cost, trip.getRemainingBudget(), trip.getDestination()));
        }

        Activity activity = new Activity(normalizedId, day, title.trim(), category, cost);
        activityDAO.save(activity);
        trip.addActivity(activity);
    }

    public void addPackingItem(String tripId, String itemName, int quantity)
            throws InvalidTripDataException, SQLException {
        String normalizedId = tripId.trim().toUpperCase();
        Trip trip = tripRegistry.get(normalizedId);
        if (trip == null) {
            throw new InvalidTripDataException("No trip found with ID: " + tripId);
        }
        if (itemName == null || itemName.isBlank()) {
            throw new InvalidTripDataException("Item name cannot be blank.");
        }
        if (quantity <= 0) {
            throw new InvalidTripDataException("Quantity must be at least 1.");
        }

        PackingItem item = new PackingItem(normalizedId, itemName.trim(), quantity);
        packingItemDAO.save(item);
        trip.addPackingItem(item);
    }

    public void markItemPacked(String tripId, int itemId)
            throws InvalidTripDataException, SQLException {
        Trip trip = tripRegistry.get(tripId.trim().toUpperCase());
        if (trip == null) {
            throw new InvalidTripDataException("No trip found with ID: " + tripId);
        }
        if (!packingItemDAO.markPacked(itemId, true)) {
            throw new InvalidTripDataException("No packing item found with ID #" + itemId);
        }
        for (PackingItem item : trip.getPackingList()) {
            if (item.getItemId() == itemId) {
                item.setPacked(true);
                break;
            }
        }
    }

    public Trip getTrip(String tripId) {
        return tripRegistry.get(tripId.trim().toUpperCase());
    }

    public Collection<Trip> getAllTrips() {
        return tripRegistry.values();
    }

    public boolean deleteTrip(String tripId) throws SQLException {
        String normalizedId = tripId.trim().toUpperCase();
        if (tripDAO.deleteByIdAndUser(normalizedId, currentUser.getUserId())) {
            tripRegistry.remove(normalizedId);
            return true;
        }
        return false;
    }
}