package com.example.bletest.comm;

import com.clj.fastble.data.BleDevice;

/**
 * 被观察者
 * 当Observable发生改变时就会通知Observer做相应的操作
 * 一个Observable可以被很多Observer同时观察
 */
public interface Observable {

    void addObserver(Observer obj);
    void deleteObserver(Observer obj);
    void notifyObserver(BleDevice bleDevice);
}
