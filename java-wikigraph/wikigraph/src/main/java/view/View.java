package view;

public interface View extends ViewEventEmitter, GraphDisplay {
    void start();

    void doSearch(Runnable then);

    void doClear(Runnable then);

    void doExit();

    void prepareSearch(String query);
}
