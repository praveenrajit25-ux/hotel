package com.hotelmanagement.model;

public enum PaymentStatus {
    Unpaid,
    Partially_Paid("Partially Paid"),
    Paid,
    Refunded;

    private final String displayName;

    PaymentStatus() {
        this.displayName = this.name();
    }

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentStatus fromString(String text) {
        if (text == null) return null;
        for (PaymentStatus s : PaymentStatus.values()) {
            if (s.name().equalsIgnoreCase(text) || s.displayName.equalsIgnoreCase(text) || s.name().replace("_", " ").equalsIgnoreCase(text)) {
                return s;
            }
        }
        return valueOf(text);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
