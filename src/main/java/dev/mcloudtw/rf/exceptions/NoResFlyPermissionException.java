package dev.mcloudtw.rf.exceptions;

public class NoResFlyPermissionException extends Exception {
    public NoResFlyPermissionException() {
        super("You do not have permission to use ResidenceFly");
    }
}
