package com.javaprep.backend.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entity, String id) {
        return new ResourceNotFoundException(entity + " not found with id: " + id);
    }
}
