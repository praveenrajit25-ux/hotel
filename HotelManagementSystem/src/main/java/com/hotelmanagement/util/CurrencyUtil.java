package com.hotelmanagement.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtil {
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    public static String format(BigDecimal amount) {
        if (amount == null) return "$0.00";
        return CURRENCY_FORMAT.format(amount.setScale(2, RoundingMode.HALF_UP));
    }

    public static String format(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static BigDecimal round(BigDecimal value) {
        if (value == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
