package com.miniecommerce.orderservice.exception;

public class ServiceCommunicationException extends RuntimeException {
    public ServiceCommunicationException(String message) {
        super(message);
    }

    public ServiceCommunicationException(String serviceName, String message) {
        super(String.format("Error communicating with %s: %s", serviceName, message));
    }
}