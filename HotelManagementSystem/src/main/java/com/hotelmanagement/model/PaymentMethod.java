package com.hotelmanagement.model;

public enum PaymentMethod {
    Cash,
    Card,
    UPI,
    Bank_Transfer("Bank Transfer"),
    Other;

    private final String displayName;

    PaymentMethod() {
        this.displayName = this.name();
    }

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PaymentMethod fromString(String text) {
        if (text == null) return null;
        for (PaymentMethod p : PaymentMethod.values()) {
            if (p.name().equalsIgnoreCase(text) || p.displayName.equalsIgnoreCase(text) || p.name().replace("_", " ").equalsIgnoreCase(text)) {
                return p;
            }
        }
        return valueOf(text);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
