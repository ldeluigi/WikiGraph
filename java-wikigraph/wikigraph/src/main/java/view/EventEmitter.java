package view;

import controller.ViewEventListener;


interface EventEmitter {
    void addEventListener(ViewEventListener listener);
}
