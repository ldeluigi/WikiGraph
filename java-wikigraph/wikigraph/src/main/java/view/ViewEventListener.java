package view;

import java.util.EventListener;

public interface ViewEventListener extends EventListener {
    void notifyEvent(final ViewEvent event);
}
