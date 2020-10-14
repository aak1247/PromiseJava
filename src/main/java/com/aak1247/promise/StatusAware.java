package com.aak1247.promise;

public abstract class StatusAware {
    abstract void onCreated();

    abstract void onResolved();

    abstract void onRejected(Throwable e) throws Throwable;
}
