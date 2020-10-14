package com.aak1247.promise.func;

@FunctionalInterface
public interface Catcher<T extends Throwable, R> {
    R handle(T input);
}
