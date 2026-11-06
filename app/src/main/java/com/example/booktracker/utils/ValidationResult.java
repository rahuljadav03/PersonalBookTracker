package com.example.booktracker.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ValidationResult {
    private final boolean valid;
    private final Map<String, String> errors;

    public ValidationResult(boolean valid, Map<String, String> errors) {
        this.valid = valid;
        this.errors = errors != null ? new HashMap<>(errors) : new HashMap<>();
    }

    public static ValidationResult success() {
        return new ValidationResult(true, Collections.emptyMap());
    }

    public static ValidationResult failure(Map<String, String> errors) {
        return new ValidationResult(false, errors);
    }

    public boolean isValid() {
        return valid;
    }

    public Map<String, String> getErrors() {
        return Collections.unmodifiableMap(errors);
    }

    public String getError(String field) {
        return errors.get(field);
    }

    public boolean hasError(String field) {
        return errors.containsKey(field);
    }

    @Override
    public String toString() {
        if (valid) {
            return "ValidationResult{isValid=true}";
        } else {
            return "ValidationResult{isValid=false, errors=" + errors + "}";
        }
    }
}
