package com.aak1247.promise.func;

@FunctionalInterface
public interface PromiseInterface<V, T extends Exception> {
    void handle(ResolverFunction<V> resolver, Rejector<T> rejector);
}
