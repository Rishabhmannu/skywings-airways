package com.skywings.exception;

public class BookingNotCancellableException extends RuntimeException {

    public BookingNotCancellableException(String message) {
        super(message);
    }
}
