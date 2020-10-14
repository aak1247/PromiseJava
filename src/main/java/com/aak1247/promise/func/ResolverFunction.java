package com.aak1247.promise.func;

@FunctionalInterface
public interface ResolverFunction<V, T extends Throwable> {
    void resolve(V v) throws T;
}
