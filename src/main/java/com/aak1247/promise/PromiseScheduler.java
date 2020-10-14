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
    ExecutorService executor;
    Disruptor<PromiseEvent> disruptor;
    RingBuffer<PromiseEvent> buffer;
    List<BatchEventProcessor<PromiseEvent>> processors;
    List<EventHandler> eventHandlers = new LinkedList<>();
    private int worker_num;

    public PromiseScheduler() {
        this(3, 16);
    }

    public PromiseScheduler(int workerNum, int bufferSize) {
        ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;
        WaitStrategy waitStrategy = new BusySpinWaitStrategy();
        this.disruptor
                = new Disruptor<>(
                PromiseEvent.PROMISE_EVENT_FACTORY,
                bufferSize,
                threadFactory,
                ProducerType.SINGLE,
                waitStrategy);
//        this.disruptor.handleEventsWith((e, seq, isEnd) -> {
//            System.out.println(seq + e.toString() + e.getBefore() + e.getAfter());
//        });
        PromiseConsumer consumer = new PromiseConsumer();
        this.disruptor.handleEventsWith(consumer);
        this.buffer = disruptor.start();
    }

    public void registerEventHandler(EventHandler eventHandler) {
        eventHandlers.add(eventHandler);
    }

    public synchronized void publishPromise(Promise promise) {
        if (promise != null) {
            this.buffer.publishEvent(PromiseEvent.TRANSLATOR, promise, PromiseStatus.PENDING);
        }
    }
}
