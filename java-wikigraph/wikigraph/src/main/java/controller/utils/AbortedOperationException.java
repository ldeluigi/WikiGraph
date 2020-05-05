package controller.utils;

public class AbortedOperationException extends Throwable {
    @Override
    public String toString() {
        return "Operation was aborted.";
    }
}
