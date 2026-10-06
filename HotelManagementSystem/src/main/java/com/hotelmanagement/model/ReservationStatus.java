package com.hotelmanagement.model;

public enum ReservationStatus {
    Pending,
    Confirmed,
    Checked_In("Checked-In"),
    Checked_Out("Checked-Out"),
    Cancelled;

    private final String displayName;

    ReservationStatus() {
        this.displayName = this.name();
    }

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ReservationStatus fromString(String text) {
        if (text == null) return null;
        for (ReservationStatus b : ReservationStatus.values()) {
            if (b.name().equalsIgnoreCase(text) || b.displayName.equalsIgnoreCase(text) || b.name().replace("_", "-").equalsIgnoreCase(text)) {
                return b;
            }
        }
        return valueOf(text);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
