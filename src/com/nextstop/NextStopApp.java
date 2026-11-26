package com.nextstop;

import com.nextstop.exception.BudgetExceededException;
import com.nextstop.exception.InvalidTripDataException;
import com.nextstop.model.Activity;
import com.nextstop.model.ActivityCategory;
import com.nextstop.model.PackingItem;
import com.nextstop.model.Trip;
import com.nextstop.model.User;
import com.nextstop.service.NextStopService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class NextStopApp {
    public static void main(String[] args) {
        NextStopService service = new NextStopService();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("====================================================");
        System.out.println("           NEXTSTOP - CLI TRAVEL PLANNER            ");
        System.out.println("  Made by Arya Vishwakarma (2510990511) & Khushi Goel (2510990885)");
        System.out.println("====================================================");

        while (running) {
            if (service.getCurrentUser() == null) {
                System.out.println("\n--- WELCOME TO NEXTSTOP ---");
                System.out.println("[1] Sign In");
                System.out.println("[2] Sign Up (Create New Account)");
                System.out.println("[3] Exit");
                System.out.print("Enter choice (1-3): ");

                String authChoice = scanner.nextLine().trim();
                try {
                    switch (authChoice) {
                        case "1" -> {
                            System.out.print("Username: ");
                            String user = scanner.nextLine();
                            System.out.print("Password: ");
                            String pass = scanner.nextLine();
                            User loggedIn = service.signIn(user, pass);
                            System.out.println("-> Welcome back, " + loggedIn.getUsername() + "!");
                        }
                        case "2" -> {
                            System.out.print("Choose Username: ");
                            String user = scanner.nextLine();
                            System.out.print("Choose Password (min 4 chars): ");
                            String pass = scanner.nextLine();
                            User created = service.signUp(user, pass);
                            System.out.println("-> Account created! Signed in as " + created.getUsername() + ".");
                        }
                        case "3" -> {
                            running = false;
                            System.out.println("Thank you for using NextStop. Safe travels!");
                        }
                        default -> System.out.println("-> Invalid choice. Please enter 1, 2, or 3.");
                    }
                } catch (InvalidTripDataException e) {
                    System.out.println("[Auth Error] " + e.getMessage());
                } catch (SQLException e) {
                    System.out.println("[MySQL Error] " + e.getMessage());
                }
            } else {
                System.out.println("\n[Logged in as: " + service.getCurrentUser().getUsername() + "]");
                System.out.println("[1] Create New Trip");
                System.out.println("[2] Add Activity to Trip");
                System.out.println("[3] View Itinerary, Analytics & Sorting");
                System.out.println("[4] List All Saved Trips");
                System.out.println("[5] Trip Packing Checklist");
                System.out.println("[6] Delete a Trip");
                System.out.println("[7] Sign Out");
                System.out.print("Enter choice (1-7): ");

                String choice = scanner.nextLine().trim();

                try {
                    switch (choice) {
                        case "1" -> {
                            System.out.print("Trip ID (e.g., NS01): ");
                            String id = scanner.nextLine();
                            System.out.print("Destination: ");
                            String dest = scanner.nextLine();
                            System.out.print("Duration (Days): ");
                            int days = Integer.parseInt(scanner.nextLine().trim());
                            System.out.print("Total Budget (Rs.): ");
                            double budget = Double.parseDouble(scanner.nextLine().trim());

                            service.createTrip(id, dest, days, budget);
                            System.out.println("-> Trip created and saved to MySQL.");
                        }
                        case "2" -> {
                            System.out.print("Trip ID: ");
                            String id = scanner.nextLine();
                            System.out.print("Day Number: ");
                            int day = Integer.parseInt(scanner.nextLine().trim());
                            System.out.print("Activity Title: ");
                            String title = scanner.nextLine();
                            System.out.print("Category (TRANSPORT / STAY / FOOD / SIGHTSEEING / OTHER): ");
                            ActivityCategory category = ActivityCategory.fromString(scanner.nextLine());
                            System.out.print("Cost (Rs.): ");
                            double cost = Double.parseDouble(scanner.nextLine().trim());

                            service.addActivity(id, day, title, category, cost);
                            System.out.println("-> Activity added under [" + category + "] and saved.");
                        }
                        case "3" -> {
                            System.out.print("Enter Trip ID: ");
                            Trip trip = service.getTrip(scanner.nextLine());
                            if (trip == null) {
                                System.out.println("-> Trip not found.");
                            } else {
                                System.out.print("Sort activities by: [1] Day Order  [2] Highest Cost First : ");
                                String sortChoice = scanner.nextLine().trim();
                                List<Activity> list = sortChoice.equals("2")
                                        ? trip.getActivitiesSortedByCostDesc()
                                        : trip.getActivities();

                                System.out.println("\n--------------------------------------------------");
                                System.out.printf("TRIP: %s (%s) | %d Days\n",
                                        trip.getDestination().toUpperCase(), trip.getTripId(), trip.getDurationDays());
                                System.out.printf("Budget: Rs. %.2f | Spent: Rs. %.2f | Balance: Rs. %.2f\n",
                                        trip.getTotalBudget(), trip.getTotalSpent(), trip.getRemainingBudget());
                                System.out.println("--------------------------------------------------");

                                if (list.isEmpty()) {
                                    System.out.println("No activities scheduled yet.");
                                } else {
                                    for (Activity act : list) {
                                        System.out.println("  " + act.getDetails());
                                    }
                                    System.out.println("\n--- CATEGORY EXPENSE ANALYTICS ---");
                                    for (Map.Entry<ActivityCategory, Double> entry : trip.getCategoryExpenseBreakdown().entrySet()) {
                                        double pct = (entry.getValue() / trip.getTotalBudget()) * 100;
                                        System.out.printf("  %-12s : Rs. %8.2f (%.1f%% of budget)\n",
                                                entry.getKey(), entry.getValue(), pct);
                                    }
                                }
                            }
                        }
                        case "4" -> {
                            if (service.getAllTrips().isEmpty()) {
                                System.out.println("-> No trips saved in your account yet.");
                            } else {
                                System.out.println("\n--- YOUR SAVED TRIPS ---");
                                for (Trip t : service.getAllTrips()) {
                                    System.out.printf("[%s] %-15s | %d Days | Balance: Rs. %.2f\n",
                                            t.getTripId(), t.getDestination(), t.getDurationDays(), t.getRemainingBudget());
                                }
                            }
                        }
                        case "5" -> {
                            System.out.print("Enter Trip ID: ");
                            String tripId = scanner.nextLine();
                            Trip trip = service.getTrip(tripId);
                            if (trip == null) {
                                System.out.println("-> Trip not found.");
                            } else {
                                System.out.println("\n--- PACKING CHECKLIST (" + trip.getDestination().toUpperCase() + ") ---");
                                if (trip.getPackingList().isEmpty()) {
                                    System.out.println("  Checklist is empty.");
                                } else {
                                    for (PackingItem item : trip.getPackingList()) {
                                        System.out.println("  " + item.getStatusLine());
                                    }
                                }
                                System.out.print("\nOptions: [1] Add Item  [2] Mark Item Packed  [3] Back : ");
                                String sub = scanner.nextLine().trim();
                                if (sub.equals("1")) {
                                    System.out.print("Item Name: ");
                                    String itemName = scanner.nextLine();
                                    System.out.print("Quantity: ");
                                    int qty = Integer.parseInt(scanner.nextLine().trim());
                                    service.addPackingItem(tripId, itemName, qty);
                                    System.out.println("-> Item added to packing checklist.");
                                } else if (sub.equals("2")) {
                                    System.out.print("Enter Item ID (#) to mark packed: ");
                                    int itemId = Integer.parseInt(scanner.nextLine().trim());
                                    service.markItemPacked(tripId, itemId);
                                    System.out.println("-> Item #" + itemId + " marked as packed [X]!");
                                }
                            }
                        }
                        case "6" -> {
                            System.out.print("Enter Trip ID to delete: ");
                            boolean deleted = service.deleteTrip(scanner.nextLine());
                            System.out.println(deleted ? "-> Trip deleted from MySQL." : "-> Trip ID not found.");
                        }
                        case "7" -> {
                            System.out.println("-> Signed out of " + service.getCurrentUser().getUsername() + ".");
                            service.signOut();
                        }
                        default -> System.out.println("-> Invalid choice. Please enter 1 to 7.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[Input Error] Please enter valid numeric values.");
                } catch (InvalidTripDataException | BudgetExceededException e) {
                    System.out.println("[Validation Error] " + e.getMessage());
                } catch (SQLException e) {
                    System.out.println("[MySQL Error] " + e.getMessage());
                }
            }
        }
        scanner.close();
    }
}