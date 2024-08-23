package dev.mcloudtw.rf.exceptions;

public class WrongGamemodeException extends Exception {
    public WrongGamemodeException() {
        super("specified player is not in survival or adventure mode");
    }
}
