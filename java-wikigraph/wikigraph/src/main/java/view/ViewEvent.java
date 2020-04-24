package view;

public interface ViewEvent {
    enum EventType {
        EXIT, SEARCH, RANDOM_SEARCH, OTHER
    }
    EventType getType();
    default String getText() {
        return "";
    }
}
