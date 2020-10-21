# PromiseJava
a java promise project

## Introduction
version: 0.0.1

This project is aimed to provide a easy and effcient way to use promise in java

## Usage

### Basic Usage

#### create a promise & promise.then

```java
Promise<?, Integer, Exception> promise = Promise.resolve(1);
Promise<Integer, String, Exception> strPromise = promise.then(i -> {
   System.out.println(i);
    return i+"1";
});
Promise<?, Integer, IOException> promise2 = new Promise<>((resolver, rejector) -> {
    try{
        // some io or time consuming job
        resolver.resolve(0);
    }catch(IOException e) {
        // catch and pass it to the proceeding catch function
        rejector.reject(e);
    }
});
// equals to catch in js
Promise<Integer, String, Exception> promise3 = promise2.onCatch(e -> {
    System.out.println(e);
    return e.getMessage();
}, IOException.class);
promise3.then(s -> {
    return 0;
}, e -> {
    return 1;
}, Exception.class);
```

#### Promise.all

```java
AtomicInteger allCounter = new AtomicInteger(0); 
List<Promise> promiseList = Stream.iterate(0, i -> i + 1).limit(5).map(i -> {
     return new Promise<>((resolver, rejector) -> {
         resolver.resolve(i);
     });
}).collect(Collectors.toList());
Promise allPromise = Promise.all(promiseList);
allPromise.then(i -> {
    allCounter.incrementAndGet();
    System.out.println("alled " + i);
    return 0;
});
```

#### Promise.race

```java
AtomicInteger racedCounter = new AtomicInteger(0);
List<Promise> promiseList = Stream.iterate(0, i -> i + 1).limit(5).map(i -> {
    return new Promise<>((resolver, rejector) -> {
        resolver.resolve(i);
    });
}).collect(Collectors.toList());
Promise racedPromise = Promise.race(promiseList);
racedPromise.then(i -> {
    racedCounter.incrementAndGet();
    System.out.println("raced " + i);
    return 0;
});
```

#### Cancel a Promise

```java
Promise<?, Integer, IOException> promise = new Promise<>((resolver, rejector) -> {
    try{
        // some io or time consuming job
        resolver.resolve(0);
    }catch(IOException e) {
        // catch and pass it to the proceeding catch function
        rejector.reject(e);
    }
});
promise.cancel();
```

### Configuration

```java
int workerNum = 5;
int bufferSize = 65536; // to make sure byte alignment, this size must be 2^x
PromiseScheduler promiseScheduler = new PromiseScheduler(workerNum, bufferSize);
PromiseConfig.getInstance().config(promiseScheduler);
```

