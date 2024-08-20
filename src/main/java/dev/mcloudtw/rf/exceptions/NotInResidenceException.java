package dev.mcloudtw.rf.exceptions;

public class NotInResidenceException extends Exception {
    public NotInResidenceException() {
        super("You are not in a residence");
    }
}