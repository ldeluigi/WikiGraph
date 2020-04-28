package view;

public interface ViewEvent {
    EventType getType();

    default String getText() {
        return "";
    }

    default int getDepth() {
        return 0;
    }

    enum EventType {
        EXIT, SEARCH, RANDOM_SEARCH, CLEAR
    }

    default Runnable onComplete() {
        return () -> {};
    }
}
