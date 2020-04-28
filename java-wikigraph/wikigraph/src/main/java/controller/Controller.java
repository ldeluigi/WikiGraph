package controller;


interface Controller extends ViewEventListener {
    /**
     * Starts execution of the controller.
     */
    void start();
}
