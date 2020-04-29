package controller.paradigm.eventloop;

import controller.Controller;
import view.View;
import view.ViewEvent;

public class EventLoopController implements Controller {

    private final View view;

    public EventLoopController(View view){
        this.view=view;
        view.addEventListener(this);

    }

    @Override
    public void notifyEvent(ViewEvent event) {

    }

    @Override
    public void start() {
        view.start();
    }
}
