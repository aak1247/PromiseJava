package com.aak1247.promise.func;

@FunctionalInterface
public interface Rejector<I, T extends Throwable> {
    Rejector<Object, Throwable> defaultRejector = (o) -> {
        System.out.println(o.toString());
    };

    void reject(I input) throws T;
}