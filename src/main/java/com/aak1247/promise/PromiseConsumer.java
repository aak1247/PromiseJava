package com.aak1247.promise;

import com.lmax.disruptor.EventHandler;

public class PromiseConsumer implements EventHandler<PromiseEvent> {


    @Override
    public void onEvent(PromiseEvent promiseEvent, long l, boolean b) throws Exception {
        System.out.println(promiseEvent.getBefore());
        Promise promise = promiseEvent.getPromise();
        if (promise != null) {
            promise.getJob().call();
        }
        System.out.println(promiseEvent.getAfter());
        if (promise != null) System.out.println(promise.result().get());
    }
}
