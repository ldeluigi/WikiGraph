package view;

import controller.ViewEventListener;


interface ViewEventEmitter {
    void addEventListener(ViewEventListener listener);
}
