package com.aak1247.promise.func;

@FunctionalInterface
public interface ResolverFunction<V> {
    void resolve(V v);
}
