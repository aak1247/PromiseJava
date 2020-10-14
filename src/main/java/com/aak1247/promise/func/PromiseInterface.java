package com.aak1247.promise.func;

@FunctionalInterface
public interface PromiseInterface<V, R, T extends Throwable> {
    void handle(ResolverFunction<V, T> resolver, Rejector<R, T> rejector);
}
