package com.aak1247.promise.func;

@FunctionalInterface
public interface Resolver<I, R, T extends Throwable> {
    R resolve(I input) throws T;
}
