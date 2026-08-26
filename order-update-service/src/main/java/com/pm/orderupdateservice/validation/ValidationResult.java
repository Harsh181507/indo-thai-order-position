package com.pm.orderupdateservice.validation;

public record ValidationResult(
        boolean valid,
        String reason
) {

    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult invalid(String reason) {
        return new ValidationResult(false, reason);
    }
}