package view;

import java.util.EventListener;

interface EventEmitter {
    void addEventListener(EventListener listener);
}
