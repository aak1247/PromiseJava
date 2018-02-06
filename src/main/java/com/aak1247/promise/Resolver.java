package com.aak1247.promise;

@FunctionalInterface
public interface Resolver<S, T> {
    T resolve(S s) throws Throwable;
}
