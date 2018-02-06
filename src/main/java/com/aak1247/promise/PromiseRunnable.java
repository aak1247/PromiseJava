package com.aak1247.promise;

@FunctionalInterface
public interface PromiseRunnable<T> {
     T run() throws Throwable;
}
