package com.aak1247.promise;

import com.lmax.disruptor.EventHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

public class PromiseConsumer implements EventHandler<PromiseEvent> {
    public static void rejectResolver(Promise curResolverPromise, Exception reason) {
        curResolverPromise.setReason(reason);
        PromiseConfig.getInstance().getPromiseScheduler().publishPromise(curResolverPromise, PromiseStatus.REJECTED);
    }

    public static void handlePromiseResolved(Promise next, Promise current) {
        try {
            next.getInput().setData(current.getResult().get());
            PromiseConfig.getInstance().getPromiseScheduler().publishPromise(next, PromiseStatus.PENDING, true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param promiseEvent
     * @param l
     * @param b
     * @throws Exception
     */
    @Override
    public void onEvent(PromiseEvent promiseEvent, long l, boolean b) throws Exception {
        System.out.println(promiseEvent.getBefore() + ", " + promiseEvent.getPromise().toString() + ", " + promiseEvent.getAfter());
        Promise promise = promiseEvent.getPromise();
        if (promise == null) return;
        switch (promiseEvent.getAfter()) {
            default:
            case PENDING:
                try {
                    // run current job and publish resolved event
                    promise.getJob().run();
                    System.out.println("after run " + promise.getStatus());
                    if (PromiseStatus.RESOLVED.equals(promise.getStatus())) {
                        PromiseConfig.getInstance().getPromiseScheduler().publishPromise(promise, PromiseStatus.RESOLVED);
                    }
                } catch (Exception t) {
                    rejectResolver(promise, t);
                }
                break;

            case RESOLVED: {
                // publish upcoming resolver
                ConcurrentLinkedQueue<Promise> resolverPromises = (ConcurrentLinkedQueue<Promise>) promise.getResolverPromises();
                for (Promise curResolverPromise = resolverPromises.poll(); curResolverPromise != null; curResolverPromise = resolverPromises.poll()) {
                    handlePromiseResolved(curResolverPromise, promise);
                }
                break;
            }

            case REJECTED:
                Map<Throwable, Promise> catchers = promise.getCatcherPromises();
                Promise catcherPromise = catchers.get(promise.getReason().getClass());
                if (catcherPromise != null) {
                    // caught by current catcher
                    catcherPromise.setInputData(promise.getReason());
                    // replace current resolver with catcher(make all upcoming resolver and catcher related to current catcher)
                    promise.getResolverPromises().forEach(p -> {
                        Promise curResolverPromise = (Promise) p;
                        catcherPromise.getResolverPromises().addAll(curResolverPromise.getResolverPromises());
                        for (Object catcherOfResolver :
                                curResolverPromise.getCatcherPromises().entrySet()) {
                            Map.Entry catcherOfResolverEntry = (Map.Entry) catcherOfResolver;
                            curResolverPromise.getCatcherPromises().putIfAbsent(catcherOfResolverEntry.getKey(), catcherOfResolverEntry.getValue());
                        }
                    });
                    // publish catcher
                    PromiseConfig.getInstance().getPromiseScheduler().publishPromise(catcherPromise, PromiseStatus.PENDING);
                } else {
                    // exception not caught by current catcher
                    // pass exception to upcoming resolver
                    ConcurrentLinkedQueue<Promise> resolverPromises = (ConcurrentLinkedQueue<Promise>) promise.getResolverPromises();
                    for (Promise curResolverPromise = resolverPromises.poll(); curResolverPromise != null; curResolverPromise = resolverPromises.poll()) {
                        // reject all resolver
                        rejectResolver(curResolverPromise, promise.getReason());
                    }
                }
                // whether catch or not, catcher should be clear
                promise.getCatcherPromises().clear();
                promise.getResolverPromises().clear();
                break;
        }
    }
}
