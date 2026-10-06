package com.hotelmanagement.model;

public enum IdProofType {
    Passport,
    Driving_License("Driving License"),
    National_ID("National ID"),
    Voter_ID("Voter ID"),
    Aadhaar;

    private final String displayName;

    IdProofType() {
        this.displayName = this.name();
    }

    IdProofType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static IdProofType fromString(String text) {
        if (text == null) return null;
        for (IdProofType t : IdProofType.values()) {
            if (t.name().equalsIgnoreCase(text) || t.displayName.equalsIgnoreCase(text) || t.name().replace("_", " ").equalsIgnoreCase(text)) {
                return t;
            }
        }
        return valueOf(text);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
