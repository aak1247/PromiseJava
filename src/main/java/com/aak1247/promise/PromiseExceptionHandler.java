package com.aak1247.promise;

import com.lmax.disruptor.ExceptionHandler;

public class PromiseExceptionHandler implements ExceptionHandler<PromiseEvent> {
    @Override
    public void handleEventException(Throwable throwable, long l, PromiseEvent event) {
        throwable.printStackTrace();
        System.out.println(event.toString());
    }

    @Override
    public void handleOnStartException(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void handleOnShutdownException(Throwable throwable) {
        throwable.printStackTrace();
    }
}
