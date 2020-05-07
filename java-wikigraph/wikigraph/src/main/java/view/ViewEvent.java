package view;

public interface ViewEvent {
    EventType getType();

    default String getText() {
        return "";
    }

    default int getInt() {
        return 0;
    }

    default void onComplete(final boolean success) {
    }

    enum EventType {
        EXIT, SEARCH, RANDOM_SEARCH, CLEAR, LANGUAGE, AUTO_UPDATE
    }
}
