package com.aak1247.promise;


import com.aak1247.promise.func.*;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Promise<I, R, T extends Throwable> extends StatusAware implements IPromise<I, R, T> {
    private final CompletableFuture<R> valueFuture = new CompletableFuture<>();
    private final List<Promise<R, ?, ?>> resolverPromises = new LinkedList<>();
    private final Map<Class<T>, Promise<T, ?, ?>> catcherPromises = new HashMap<>();
    private final PromiseConfig promiseConfig = new PromiseConfig();
    private I input;
    private PromiseStatus status;
    Rejector<R, T> rejector = (o) -> {
        Throwable e = null;
        Class<T> expClass = null;
        try {
            expClass = (Class<T>) this.rejector.getClass().getGenericInterfaces()[1];
            Constructor<T> constructor = expClass.getConstructor((Class) this.rejector.getClass().getGenericInterfaces()[0]);
            e = expClass.newInstance();
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e1) {
            e = new RuntimeException(e1);
        } finally {
            try {
                this.onRejected(e);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                if (expClass.isInstance(e)) {
                    throw (T) throwable;
                }
            }
        }
    };
    ResolverFunction<R, T> resolver = (object) -> {
        this.valueFuture.complete(object);
        this.onResolved();
    };
    private T reason;
    private Callable<R> job;

    /**
     * testing only
     */
    public Promise() {
        promiseConfig.getConfig().publishPromise(this);
    }

    /**
     * @param promiseInterface function that uses resolver and rejector to create a promise
     */
    public Promise(PromiseInterface<R, R, T> promiseInterface) {
        this.onCreated();
        this.job = () -> {
            promiseInterface.handle(resolver, rejector);
            return this.valueFuture.get();
        };
        promiseConfig.getConfig().publishPromise(this);
    }

    public static <I1, R1, T1 extends Throwable> Promise<I1, R1, T1> resolve(Object data) {
        if (data instanceof Promise) {
            return (Promise<I1, R1, T1>) data;
        } else {
            return new Promise<I1, R1, T1>();
        }
    }

    /**
     * @param promises
     * @return
     */
    public static Promise<?, ?, ? extends Throwable> race(Promise<?, ?, ? extends Throwable>... promises) {
        return new Promise<>();
    }

    /**
     * @param promises
     * @return
     */
    public static Promise<?, ?, ? extends Throwable> all(Promise<?, ?, ? extends Throwable>... promises) {
        return new Promise<>();
    }

    @Override
    public <R1, T1 extends Throwable> Promise<R, R1, T1> then(Resolver<R, R1, T1> resolver) {
        Promise promise = new Promise((resolverF, rejectorF) -> {
            try {
                resolverF.resolve(this.valueFuture.get());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        this.resolverPromises.add(promise);
        return promise;
    }

    @Override
    public <R1, T1 extends Throwable> Promise<R, R1, T1> then(Resolver<R, R1, T1> resolver, Catcher<T, R1> catcher) {
        return null;
    }

    @Override
    public <R1, R2, T2 extends Throwable> Promise<R1, R2, T2> onCatch(T t) {
        return null;
    }

    @Override
    public <R1, T1 extends Throwable> IPromise<R1, ?, T1> anyway(Resolver<?, R1, T1> resolver) {
        return null;
    }

    @Override
    public Future<R> result() {
        return this.valueFuture;
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

    Callable<R> getJob() {
        return job;
    }

    void setJob(Callable<R> job) {
        this.job = job;
    }

    @Override
    void onCreated() {
        this.status = PromiseStatus.PENDING;
    }

    @Override
    void onResolved() {
        this.status = PromiseStatus.RESOLVED;
        // todo: 传递数据
        this.resolverPromises.forEach(resolverPromise -> promiseConfig.getConfig().publishPromise(resolverPromise));
    }

    @Override
    void onRejected(Throwable throwable) throws Throwable {
        this.status = PromiseStatus.REJECTED;
        //todo: 传递数据
        Promise catcherPromise = this.catcherPromises.get(throwable.getClass());
        if (catcherPromise != null) {
            promiseConfig.getConfig().publishPromise(catcherPromise);
        } else {
            throw throwable;
        }
    }
}
