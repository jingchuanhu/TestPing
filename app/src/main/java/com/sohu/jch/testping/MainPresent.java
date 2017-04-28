package com.sohu.jch.testping;

/**
 * Created by jch on 2017/4/28.
 */

public interface MainPresent {

    void startPing();

    void stopPing();

    void addViewListener(MainView view);

    void removeViewListener();
}
