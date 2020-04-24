package controller;

import view.ViewEvent;

interface ViewEventListener {
    void notifyEvent(ViewEvent event);
}
