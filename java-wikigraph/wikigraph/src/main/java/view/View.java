package view;

public interface View extends ViewEventEmitter, GraphDisplay {
    void start();

    void doSearch(final Runnable then);

    void doClear(final Runnable then);

    void doExit();

    void prepareSearch(final String query);
}
