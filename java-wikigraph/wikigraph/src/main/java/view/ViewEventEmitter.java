package view;

import controller.ViewEventListener;


interface ViewEventEmitter {
    void addEventListener(final ViewEventListener listener);
}
