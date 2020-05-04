package controller;


import view.ViewEventListener;

public interface Controller extends ViewEventListener {
    /**
     * Starts execution of the controller.
     */
    void start();
}
