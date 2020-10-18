package com.aak1247.promise;


import com.aak1247.promise.func.*;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Promise<I, R, T extends Exception> implements IPromise<I, R, T> {
    private final CompletableFuture<R> result = new CompletableFuture<>();
    private final Queue<Promise<R, ?, ?>> resolverPromises = new ConcurrentLinkedQueue<Promise<R, ?, ?>>();
    private final Map<Class<T>, Promise<T, ?, ?>> catcherPromises = new ConcurrentHashMap<>();
    private final PromiseConfig promiseConfig = new PromiseConfig();
    private final StrongReference<I> input = new StrongReference<>();
    private T reason;
    private PromiseStatus status = PromiseStatus.PENDING;
    ResolverFunction<R> resolver = (r) -> {
        this.result.complete(r);
        this.setStatus(PromiseStatus.RESOLVED);
    };
    Rejector<T> rejector = t -> {
        this.setReason(t);
        this.setStatus(PromiseStatus.REJECTED);
    };
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
            return this.result.get();
        };
        promiseConfig.getPromiseScheduler().publishPromise(this);
    }

    public static <I1, R1, T1 extends Exception> Promise<I1, R1, T1> resolve(R1 data) {
        if (data instanceof Promise) {
            return (Promise<I1, R1, T1>) data;
        } else {
            return new Promise<>(((resolver1, rejector1) -> {
                resolver1.resolve(data);
            }));
        }
    }

    /**
     * fixme
     *
     * @param promises
     * @return
     */
    public static Promise<?, ?, ? extends Exception> race(List<Promise> promises) {
        Promise promise = new Promise();
        AtomicInteger count = new AtomicInteger(0);
        promise.setJob(() -> {
            // only resolved once
            if (count.getAndIncrement() == 0) {
                promise.resolver.resolve(promise.input.getData());
            }
            return promise.getResult().get();
        });
        promises.forEach(p -> p.then(promise));
        return promise;
    }

    /**
     * fixme
     *
     * @param promises
     * @return
     */
    public static Promise<?, ?, ? extends Exception> all(List<Promise> promises) {
        Promise promise = new Promise();
        AtomicInteger count = new AtomicInteger(0);
        int l = promises.size() - 1;
        promise.setJob(() -> {
            // only resolved once
            if (count.getAndIncrement() == l) {
                promise.resolver.resolve(promise.input.getData());
            }
            Future res = promise.getResult();
            if (res.isDone()) return res.get();
            else return null;
        });
        promises.forEach(p -> p.then(promise));
        return promise;
    }

    /**
     * @param resolver process current result and returns next promise
     * @param <R1>
     * @param <T1>
     * @return
     */
    @Override
    public <R1, T1 extends Exception> Promise<R, R1, T1> then(Resolver<R, R1, T1> resolver) {
        Promise<R, R1, T1> promise = new Promise<>();
        promise.setJob(() -> {
            R1 res = resolver.resolve(promise.input.getData());
            promise.result.complete(res);
            return res;
        });
        return then(promise);
    }

    /**
     * @param resolver handle current result
     * @param catcher  handle current exception
     * @param tClass
     * @param <R1>
     * @param <T1>
     * @return
     */
    @Override
    public <R1, T1 extends Exception> Promise<R, R1, T1> then(Resolver<R, R1, T1> resolver, Catcher<T, R1> catcher, Class<T> tClass) {
        // resolver promise
        Promise<R, R1, T1> promise = new Promise<>();
        promise.setJob(() -> {
            R1 res = resolver.resolve(promise.input.getData());
            promise.result.complete(res);
            return res;
        });
        // error handler for current promise
        Promise<T, R1, T1> catcherPromise = new Promise<>();
        catcherPromise.setJob(() -> {
            R1 res = catcher.handle(this.getReason());
            catcherPromise.result.complete(res);
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
            return (Promise) catcherPromise;
        }
        return promise;
    }

    /**
     * @param resolver
     * @param catcher
     * @param <R1>
     * @param <T1>
     * @return
     */
    public <R1, T1 extends Exception> Promise<R, R1, T1> then(Resolver<R, R1, T1> resolver, Catcher<T, R1> catcher) {
        return then(resolver, catcher, (Class<T>) Exception.class);
    }

    /**
     * @param next
     * @param <R1>
     * @param <T1>
     * @return
     */
    public <R1, T1 extends Exception> Promise<R, R1, T1> then(Promise<R, R1, T1> next) {
        if (this.status == null || PromiseStatus.PENDING.equals(this.status)) {
            this.resolverPromises.add(next);
        } else if (this.status.equals(PromiseStatus.RESOLVED)) {
            PromiseConsumer.handlePromiseResolved(next, this);
        } else if (this.status.equals(PromiseStatus.REJECTED)) {
            PromiseConsumer.rejectResolver(next, this.getReason());
        }
        return next;
    }

    /**
     * @param catcher
     * @param tClass
     * @param <R1>
     * @param <T1>
     * @return
     */
    @Override
    public <R1, T1 extends Exception> Promise<T, R1, T1> onCatch(Catcher<T, R1> catcher, Class<T> tClass) {
        Promise<T, R1, T1> promise = new Promise<>();
        promise.setJob(() -> {
            R1 res = catcher.handle(this.getReason());
            promise.result.complete(res);
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

    /**
     * @param catcher
     * @param <R1>
     * @param <T1>
     * @return
     */
    public <R1, T1 extends Exception> Promise<T, R1, T1> onCatch(Catcher<T, R1> catcher) {
        return onCatch(catcher, (Class<T>) Exception.class);
    }

    /**
     * @param resolver
     * @param <R1>
     * @param <T1>
     * @return
     */
    @Override
    public <R1, T1 extends Exception> Promise<R, R1, T1> anyway(Resolver<Object, R1, T1> resolver) {
        return then((Resolver<R, R1, T1>) resolver, t -> {
            try {
                return resolver.resolve(t);
            } catch (Exception t1) {
                return null;
            }
        });
    }

    /**
     * @return
     */
    @Override
    public Future<R> result() {
        return this.result;
    }

    /**
     *
     */
    @Override
    public void cancel() {
        this.job = null;
        this.catcherPromises.clear();
        this.resolverPromises.clear();
    }

    /**
     * @return
     */
    public StrongReference<I> getInput() {
        return input;
    }

    /**
     * @param inputData
     */
    public void setInputData(I inputData) {
        this.input.setData(inputData);
    }

    /**
     * @return
     */
    public PromiseStatus getStatus() {
        return status;
    }

    /**
     * @param status
     */
    void setStatus(PromiseStatus status) {
        this.status = status;
    }

    /**
     * @return
     */
    public T getReason() {
        return reason;
    }

    /**
     * @param reason
     */
    void setReason(T reason) {
        this.reason = reason;
    }

    /**
     * @return
     */
    Producer<R> getJob() {
        return job;
    }

    /**
     * @param job
     */
    void setJob(Producer<R> job) {
        this.job = job;
    }

    /**
     * @return
     */
    CompletableFuture<R> getResult() {
        return result;
    }

    /**
     * @return
     */
    Queue<Promise<R, ?, ?>> getResolverPromises() {
        return resolverPromises;
    }

    /**
     * @return
     */
    Map<Class<T>, Promise<T, ?, ?>> getCatcherPromises() {
        return catcherPromises;
    }
}
