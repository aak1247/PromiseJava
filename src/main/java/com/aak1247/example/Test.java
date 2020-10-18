package com.aak1247.example;


import com.aak1247.promise.Promise;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {
    public static void main(String[] args) throws Exception {
//        for (int t = 0; t < 100; ++t) {
//            int m = t;
//            Promise<Integer, Integer, RuntimeException> promise = Promise.resolve(m);//new Promise<>((resolver, rejector) -> resolver.resolve(m));
//            promise.then(i -> {
//                System.out.println("first thenable of " + i);
//                return i;
//            });
//            Promise<String, Object, Exception> exceptionPromise = promise.then((i) -> {
//                System.out.println("second thenable of " + i);
//                return "" + i;
//            }).then(s -> {
//                System.out.println(s);
//                Thread.sleep(1);
//                throw new Exception(s);
//            });
//            exceptionPromise.then(o -> {
//                System.out.println("will not run");
//                return 0;
//            });
//            exceptionPromise.onCatch(e -> {
//                return "error of " + e.getMessage();
//            }, Exception.class).then(s -> {
//                System.out.println("caught: " + s);
//                return "after caught " + s;
//            }).then(n -> {
//                System.out.println(n);
//                return n;
//            });
//        }
//        Promise<?, Integer, Exception> p = Promise.resolve(0);
//        p.then(i -> i++)
//                .then(s -> {
//                    System.out.println(s);
//                    return 0;
//                });
        AtomicInteger allCounter = new AtomicInteger(0);
        AtomicInteger racedCounter = new AtomicInteger(0);
        for (int k = 0; k < 10; k++) {
            for (int t = 0; t < 10; ++t) {
                List<Promise> promiseList = Stream.iterate(0, i -> i + 1).limit(5).map(i -> {
                    return new Promise<>((resolver, rejector) -> {
                        resolver.resolve(i);
                    });
                }).collect(Collectors.toList());
                Promise allPromise = Promise.all(promiseList);
                Promise racedPromise = Promise.race(promiseList);
                int finalK = k;
                int finalT = t;
                allPromise.then(i -> {
                    allCounter.incrementAndGet();
                    System.out.println("alled " + (finalK * 10 + finalT) + " " + i);
                    return 0;
                });
                racedPromise.then(i -> {
                    racedCounter.incrementAndGet();
                    System.out.println("raced " + (finalK * 10 + finalT) + " " + i);
                    return 0;
                });
            }
        }
        Thread.sleep(1000);
        System.out.println(allCounter.get());
    }
}
