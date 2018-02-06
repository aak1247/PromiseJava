package com.aak1247.promise;

@FunctionalInterface
public interface Thenable {
    Promise then(Object...objects);
}
