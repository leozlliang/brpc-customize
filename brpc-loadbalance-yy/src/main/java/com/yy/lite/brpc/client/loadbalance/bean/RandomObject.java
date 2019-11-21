package com.yy.lite.brpc.client.loadbalance.bean;

public interface RandomObject<T> {

    double getWeight();

    T get();
}
