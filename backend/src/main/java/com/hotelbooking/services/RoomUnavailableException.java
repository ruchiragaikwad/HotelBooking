package com.hotelbooking.services;

public class RoomUnavailableException extends RuntimeException {

    public RoomUnavailableException() {
        super("No rooms are available for the selected dates.");
    }
}
