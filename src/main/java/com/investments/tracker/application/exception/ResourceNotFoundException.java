package com.investments.tracker.application.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String fieldName;
    private final String fieldValue;

    public ResourceNotFoundException(String resourceType, String fieldName, String fieldValue) {
        super(String.format("%s not found with %s: %s", resourceType, fieldName, fieldValue));
        this.resourceType = resourceType;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }
}
