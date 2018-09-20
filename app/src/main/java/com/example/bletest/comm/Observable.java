package com.example.bletest.comm;

import com.clj.fastble.data.BleDevice;


public interface Observable {

    void addObserver(Observer obj);
    void deleteObserver(Observer obj);
    void notifyObserver(BleDevice bleDevice);
}
