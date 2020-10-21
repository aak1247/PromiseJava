package com.aak1247.promise;

public class PromiseConfig {
    private static PromiseConfig promiseConfig = null;
    private PromiseScheduler promiseScheduler = new PromiseScheduler();

    public static PromiseConfig getInstance() {
        if (promiseConfig == null) {
            promiseConfig = new PromiseConfig();
            promiseConfig.config();
        }
        return promiseConfig;
    }

    public PromiseScheduler config(PromiseScheduler promiseScheduler1) {
        promiseScheduler = promiseScheduler1;
        return promiseScheduler;
    }

    private void config() {
        this.config(new PromiseScheduler());
    }

    public PromiseScheduler getPromiseScheduler() {
        return promiseScheduler;
    }
}
