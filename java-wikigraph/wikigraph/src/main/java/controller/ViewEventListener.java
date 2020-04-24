package controller;

import view.ViewEvent;

import java.util.EventListener;

interface ViewEventListener extends EventListener {
    void notifyEvent(ViewEvent event);
}
