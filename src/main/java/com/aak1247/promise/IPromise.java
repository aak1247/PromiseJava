package com.aak1247.promise;

import com.aak1247.promise.func.Catcher;
import com.aak1247.promise.func.Resolver;

import java.util.concurrent.Future;

/**
 * @param <I> type of input elements
 * @param <R> type of return elements
 * @param <T> type of throws elements
 */
public interface IPromise<I, R, T extends Throwable> {
    /**
     * next promise
     *
     * @param resolver process current result and returns next promise
     * @param <R1>     next result
     * @param <T1>     exception thrown by resolver
     * @return
     */
    <R1, T1 extends Throwable> IPromise<R, R1, T1> then(Resolver<R, R1, T1> resolver);

    /**
     * nextPromise
     *
     * @param resolver handle current result
     * @param catcher  handle current exception
     * @param <R1>     result of resolver
     * @param <T1>     exception thrown by resolver
     * @return
     */
    <R1, T1 extends Throwable> IPromise<R, R1, T1> then(Resolver<R, R1, T1> resolver, Catcher<T, R1> catcher);

    /**
     * handle current exception
     *
     * @param t
     * @param <R1>
     * @return
     */
    <R1, R2, T2 extends Throwable> IPromise<R1, R2, T2> onCatch(T t);

    /**
     *
     */
    <R1, T1 extends Throwable> IPromise<R1, ?, T1> anyway(Resolver<?, R1, T1> resolver);

    /**
     * @return
     */
    Future<R> result();
}
