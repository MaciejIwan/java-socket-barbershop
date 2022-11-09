package org.example.server.exception;

public class UsernameIsAlreadyTakenException extends Exception{
    public UsernameIsAlreadyTakenException(String message) {
        super(message);
    }
}
