package com.aak1247.promise;


import com.aak1247.promise.func.*;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

public class Promise<I, R, T extends Exception> implements IPromise<I, R, T> {
    private final CompletableFuture<R> valueFuture = new CompletableFuture<>();
    private final Queue<Promise<R, ?, ?>> resolverPromises = new ConcurrentLinkedQueue<Promise<R, ?, ?>>();
    private final Map<Class<T>, Promise<T, ?, ?>> catcherPromises = new ConcurrentHashMap<>();
    private final PromiseConfig promiseConfig = new PromiseConfig();
    private final DataWrapper<I> input = new DataWrapper<>();
    ResolverFunction<R> resolver = (r) -> {
        this.valueFuture.complete(r);
    };
    Rejector<T> rejector = this::setReason;
    private T reason;
    private PromiseStatus status;
    private Producer<R> job;

    /**
     * testing only
     */
    private Promise() {
    }

    /**
     * @param promiseInterface function that uses resolver and rejector to create a promise
     */
    public Promise(PromiseInterface<R, T> promiseInterface) {
        this.job = () -> {
            promiseInterface.handle(resolver, rejector);
            return this.valueFuture.get();
        };
        promiseConfig.getPromiseScheduler().publishPromise(this);
    }

    public static <I1, R1, T1 extends Exception> Promise<I1, R1, T1> resolve(R1 data) {
        if (data instanceof Promise) {
            return (Promise) data;
        } else {
            return new Promise<>(((resolver1, rejector1) -> {
                resolver1.resolve(data);
            }));
        }
    }

    /**
     * @param promises
     * @return
     */
    public static Promise<?, ?, ? extends Exception> race(Promise<?, ?, ? extends Throwable>... promises) {
        return new Promise<>();
    }

    /**
     * @param promises
     * @return
     */
    public static Promise<?, ?, ? extends Exception> all(Promise<?, ?, ? extends Throwable>... promises) {
        return new Promise<>();
    }

    @Override
    public <R1, T1 extends Exception> Promise<R, R1, T1> then(Resolver<R, R1, T1> resolver) {
        Promise<R, R1, T1> promise = new Promise<>();
        promise.setJob(() -> {
            R1 res = resolver.resolve(promise.input.getData());
            promise.valueFuture.complete(res);
            return res;
        });

        if (this.status == null || PromiseStatus.PENDING.equals(this.status)) {
            this.resolverPromises.add(promise);
        } else if (this.status.equals(PromiseStatus.RESOLVED)) {
            PromiseConsumer.handlePromiseResolved(promise, this);
        } else if (this.status.equals(PromiseStatus.REJECTED)) {
            PromiseConsumer.rejectResolver(promise, this.getReason());
        }
        return promise;
    }

    @Override
    public <R1, T1 extends Exception> Promise<R, R1, T1> then(Resolver<R, R1, T1> resolver, Catcher<T, R1> catcher, Class<T> tClass) {
        // resolver promise
        Promise<R, R1, T1> promise = new Promise<>();
        promise.setJob(() -> {
            R1 res = resolver.resolve(promise.input.getData());
            promise.valueFuture.complete(res);
            return res;
        });
        // error handler for current promise
        Promise<T, R1, T1> catcherPromise = new Promise<>();
        catcherPromise.setJob(() -> {
            R1 res = catcher.handle(this.getReason());
            catcherPromise.valueFuture.complete(res);
            return res;
        });

        if (this.status == null || this.status.equals(PromiseStatus.PENDING)) {
            this.resolverPromises.add(promise);
            this.catcherPromises.putIfAbsent(tClass, catcherPromise);
        } else if (this.status.equals(PromiseStatus.RESOLVED)) {
            PromiseConsumer.handlePromiseResolved(promise, this);
        } else if (this.status.equals(PromiseStatus.REJECTED)) {
            this.resolverPromises.add(promise);
            this.catcherPromises.putIfAbsent(tClass, catcherPromise);
            PromiseConsumer.rejectResolver(this, this.getReason());
        }
        return promise;
    }

    @Override
    public <R1, T1 extends Exception> Promise<T, R1, T1> onCatch(Catcher<T, R1> catcher, Class<T> tClass) {
        Promise<T, R1, T1> promise = new Promise();
        promise.setJob(() -> {
            R1 res = catcher.handle(this.getReason());
            promise.valueFuture.complete(res);
            return res;
        });
        this.catcherPromises.putIfAbsent(tClass, promise);
        if (this.status == null || this.status.equals(PromiseStatus.PENDING)) {
            // do nothing
        } else if (this.status.equals(PromiseStatus.RESOLVED)) {
            promise.setJob(() -> null);
        } else if (this.status.equals(PromiseStatus.REJECTED)) {
            // can be catch more than once
            PromiseConsumer.rejectResolver(this, this.getReason());
        }
        return promise;

    }

    public <R1, T1 extends Exception> Promise<T, R1, T1> onCatch(Catcher<T, R1> catcher) {
        return onCatch(catcher, (Class<T>) Exception.class);
    }

    @Override
    public <R1, T1 extends Exception> IPromise<R1, ?, T1> anyway(Resolver<?, R1, T1> resolver) {
        return null;
    }

    @Override
    public Future<R> result() {
        return this.valueFuture;
    }

    @Override
    public void cancel() {
        this.catcherPromises.clear();
        this.resolverPromises.clear();
    }

    public DataWrapper<I> getInput() {
        return input;
    }

    public void setInputData(I inputData) {
        this.input.setData(inputData);
    }

    public PromiseStatus getStatus() {
        return status;
    }

    void setStatus(PromiseStatus status) {
        this.status = status;
    }

    public T getReason() {
        return reason;
    }

    void setReason(T reason) {
        this.reason = reason;
    }

    Producer<R> getJob() {
        return job;
    }

    void setJob(Producer<R> job) {
        this.job = job;
    }

    CompletableFuture<R> getValueFuture() {
        return valueFuture;
    }

    Queue<Promise<R, ?, ?>> getResolverPromises() {
        return resolverPromises;
    }

    Map<Class<T>, Promise<T, ?, ?>> getCatcherPromises() {
        return catcherPromises;
    }
}
