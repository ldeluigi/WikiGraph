package view;

public interface ViewEvent {
    EventType getType();

    default String getText() {
        return "";
    }

    default int getDepth() {
        return 0;
    }

    default void onComplete() {
    }

    enum EventType {
        EXIT, SEARCH, RANDOM_SEARCH, CLEAR
    }
}
