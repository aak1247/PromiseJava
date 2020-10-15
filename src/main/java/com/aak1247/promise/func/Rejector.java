package com.aak1247.promise.func;

@FunctionalInterface
public interface Rejector<T extends Exception> {
    void reject(T t);
}