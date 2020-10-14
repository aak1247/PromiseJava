package com.aak1247.promise;

public class PromiseConfig {
    private static PromiseScheduler promiseScheduler = new PromiseScheduler();

    public synchronized PromiseScheduler config(PromiseScheduler promiseScheduler1) {
        promiseScheduler = promiseScheduler1;
        return promiseScheduler;
    }

    public synchronized PromiseScheduler getConfig() {
        return promiseScheduler;
    }
}
