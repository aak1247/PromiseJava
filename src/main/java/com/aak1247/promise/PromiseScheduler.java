package com.aak1247.promise;


import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class PromiseScheduler {
    Disruptor<PromiseEvent> disruptor;
    RingBuffer<PromiseEvent> buffer;
    List<BatchEventProcessor<PromiseEvent>> processors;
    List<EventHandler<Promise<?, ?, ?>>> eventHandlers = new LinkedList<>();

    public PromiseScheduler() {
        this(10, 65536);
    }

    public PromiseScheduler(int workerNum, int bufferSize) {
        ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;
        Executor executor = Executors.newFixedThreadPool(workerNum, threadFactory);
        WaitStrategy waitStrategy = new SleepingWaitStrategy();
        this.disruptor
                = new Disruptor<>(PromiseEvent.PROMISE_EVENT_FACTORY, bufferSize, executor, ProducerType.MULTI, waitStrategy);
        PromiseConsumer consumer = new PromiseConsumer();
        PromiseExceptionHandler exceptionHandler = new PromiseExceptionHandler();
        this.disruptor.handleEventsWith(consumer);
        this.disruptor.setDefaultExceptionHandler(exceptionHandler);
        this.buffer = disruptor.start();
    }

    void registerEventHandler(EventHandler eventHandler) {
        eventHandlers.add(eventHandler);
    }

    synchronized void publishPromise(Promise promise) {
        publishPromise(promise, promise.getStatus());
    }

    synchronized void publishPromise(Promise promise, PromiseStatus after) {
        if (promise != null) {
            this.buffer.publishEvent(PromiseEvent.TRANSLATOR, promise, after);
        }
    }

    synchronized void publishPromise(Promise promise, PromiseStatus after, boolean repeated) {
        if (promise != null) {
            this.buffer.publishEvent(PromiseEvent.TRANSLATOR_REPEATED, promise, after, repeated);
        }
    }
}
