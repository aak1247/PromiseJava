# PromiseJava
a java promise project

## Introduction
version: 0.0.1

This project is aimed to provide a easy and effcient way to use promise in java

## Usage

```java
        Promise<Integer> promise = new Promise<>(
                ()->100,
                i-> {
                    System.out.println(i);
                    return i;
                },
                err-> System.out.println(err)
        );
        promise.then(i->{
            System.out.println(promise.getStatus().toString());
            System.out.println(i);
            Integer res = (Integer)i;
            res +=1 ;
            return res;
        },System.out::println)
        .then(i -> {
              System.out.println(i);
              throw new Throwable("xxxxx");
//              return i;
          }, System.out::println)
        .then(
                i->{
                   return i;
                },
                e->((Throwable)e).printStackTrace()
        );

```
