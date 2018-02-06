package com.aak1247.promise;

@FunctionalInterface
public interface Generator<T> {
    T generate() throws  Throwable;
}
