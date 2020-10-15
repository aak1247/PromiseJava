package com.aak1247.promise;

import com.aak1247.promise.func.Catcher;
import com.aak1247.promise.func.Resolver;

import java.util.concurrent.Future;

/**
 * @param <I> type of input elements
 * @param <R> type of return elements
 * @param <T> type of throws elements
 */
public interface IPromise<I, R, T extends Exception> {
    /**
     * next promise
     *
     * @param resolver process current result and returns next promise
     * @param <R1>     next result
     * @param <T1>     exception thrown by resolver
     * @return
     */
    <R1, T1 extends Exception> IPromise<R, R1, T1> then(Resolver<R, R1, T1> resolver);

    /**
     * nextPromise
     *
     * @param resolver handle current result
     * @param catcher  handle current exception
     * @param <R1>     result of resolver
     * @param <T1>     exception thrown by resolver
     * @return
     */
    <R1, T1 extends Exception> IPromise<R, R1, T1> then(Resolver<R, R1, T1> resolver, Catcher<T, R1> catcher, Class<T> tClass);

    /**
     * handle current exception
     *
     * @param catcher
     * @param tClass
     * @param <R1>
     * @param <T2>
     * @return
     */
    <R1, T2 extends Exception> IPromise<T, R1, T2> onCatch(Catcher<T, R1> catcher, Class<T> tClass);

    /**
     *
     */
    <R1, T1 extends Exception> IPromise<R1, ?, T1> anyway(Resolver<?, R1, T1> resolver);

    /**
     * @return
     */
    Future<R> result();

    /**
     * cancel current promise
     */
    void cancel();
}
