package com.investments.tracker.application.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * Maps to HTTP 409 Conflict.
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    private final String resourceType;
    private final String fieldName;
    private final String fieldValue;

    public ResourceAlreadyExistsException(String resourceType, String fieldName, String fieldValue) {
        super(String.format("%s already exists with %s: %s", resourceType, fieldName, fieldValue));
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
