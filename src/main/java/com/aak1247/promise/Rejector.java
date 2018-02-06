package com.aak1247.promise;

@FunctionalInterface
public interface Rejector<T> {
    void reject(T t);
}