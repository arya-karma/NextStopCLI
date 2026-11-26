package com.nextstop.model;

public class PackingItem {
    private int itemId;
    private String tripId;
    private String itemName;
    private int quantity;
    private boolean packed;

    public PackingItem(String tripId, String itemName, int quantity) {
        this(0, tripId, itemName, quantity, false);
    }

    public PackingItem(int itemId, String tripId, String itemName, int quantity, boolean packed) {
        this.itemId = itemId;
        this.tripId = tripId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.packed = packed;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public String getTripId() { return tripId; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public boolean isPacked() { return packed; }
    public void setPacked(boolean packed) { this.packed = packed; }

    public String getStatusLine() {
        String check = packed ? "[X]" : "[ ]";
        return String.format("%s (#%d) %-22s (Qty: %d)", check, itemId, itemName, quantity);
    }
}