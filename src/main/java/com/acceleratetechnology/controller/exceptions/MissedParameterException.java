package com.acceleratetechnology.controller.exceptions;

/**
 * Thrown when entered parameter or argument is wrong or missed.
 */
public class MissedParameterException extends Exception {
    public MissedParameterException(String message){
        super(message);
    }
}
