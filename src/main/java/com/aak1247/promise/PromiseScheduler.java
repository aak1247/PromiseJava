package com.aak1247.promise;


import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

public class PromiseScheduler {
    private final int workerNum;
    ExecutorService executor;
    Disruptor<PromiseEvent> disruptor;
    RingBuffer<PromiseEvent> buffer;
    List<BatchEventProcessor<PromiseEvent>> processors;
    List<EventHandler> eventHandlers = new LinkedList<>();

    public PromiseScheduler() {
        this(3, 65536);
    }

    public PromiseScheduler(int workerNum, int bufferSize) {
        this.workerNum = workerNum;
        ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;
        WaitStrategy waitStrategy = new SleepingWaitStrategy();
        this.disruptor
                = new Disruptor<>(
                PromiseEvent.PROMISE_EVENT_FACTORY,
                bufferSize,
                threadFactory,
                ProducerType.SINGLE,
                waitStrategy);
        PromiseConsumer consumer = new PromiseConsumer();
        PromiseExceptionHandler exceptionHandler = new PromiseExceptionHandler();
        this.disruptor.handleEventsWith(consumer);
        this.disruptor.setDefaultExceptionHandler(exceptionHandler);
        this.buffer = disruptor.start();
    }

    public void registerEventHandler(EventHandler eventHandler) {
        eventHandlers.add(eventHandler);
    }

    public synchronized void publishPromise(Promise promise) {
        publishPromise(promise, PromiseStatus.PENDING);
    }

    public synchronized void publishPromise(Promise promise, PromiseStatus after) {
        if (promise != null) {
            this.buffer.publishEvent(PromiseEvent.TRANSLATOR, promise, after);
        }
    }
}
