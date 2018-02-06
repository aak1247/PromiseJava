package com.aak1247.promise;


import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PromiseManager {
    private int worker_num;
    ExecutorService executor ;
    Disruptor<PromiseEvent> disruptor ;
    RingBuffer<PromiseEvent> buffer;
    List<BatchEventProcessor<PromiseEvent>> processors;

    public PromiseManager() {
        this(1);
    }

    public PromiseManager(int worker_num) {
        this.worker_num = worker_num;
        executor = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);
        disruptor = new Disruptor<PromiseEvent>(PromiseEvent.PROMISE_EVENT_FACTORY,
                1024,
                DaemonThreadFactory.INSTANCE);
        buffer = disruptor.start();
        BatchEventProcessor[] processors = new BatchEventProcessor[1];
        processors[0] = new BatchEventProcessor(buffer, buffer.newBarrier(), new PromiseEventHandler());
        buffer.addGatingSequences(processors[0].getSequence());
        Arrays.asList(processors).forEach(processor ->executor.execute(processor));
        //todo
    }

    public void publishPromise(Promise promise) {
        buffer.publishEvent(PromiseEvent.TRANSLATOR, promise, promise.getStatus());

    }

    private static class PromiseEventHandler implements EventHandler<PromiseEvent> {
        @Override
        public void onEvent(PromiseEvent promiseEvent, long sequence, boolean endofBatch) {
            Status before = promiseEvent.getBefore();
            Status after = promiseEvent.getAfter();
            Promise promise = promiseEvent.getPromise();
            if (after.equals(Status.PENDING)) {
                try {
                    promise.onCreated();
                    promise.onResolved(promise.getData());
                } catch (Throwable throwable) {
                    promise.onRejected(throwable);
                }
            } else if (before != null && before.equals(Status.REJECTED)) {
                promise.onRejected(promise.getReason());
            }
        }
    }

    private static class PromiseEventProducer {

    }

}
