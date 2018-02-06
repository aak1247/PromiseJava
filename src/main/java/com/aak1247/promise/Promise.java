package com.aak1247.promise;

public class Promise<T> implements PromiseRunnable {

    private PromiseRunnable<T> runnable;
    private Status status;
    private Resolver<Object, Object> resolver;
    private Rejector rejector;
    private Object data;
    private Throwable reason;

    public Promise(PromiseRunnable<T> runnable, Resolver resolver, Rejector rejector) {
        this.runnable = runnable;
        this.resolver = resolver;
        this.rejector = rejector;
        setStatus(Status.PENDING);
        promiseManager.publishPromise(this);
    }

    public Promise(Resolver resolver, Rejector rejector) {
        this.resolver = resolver;
        this.rejector = rejector;
        setStatus(Status.PENDING);
        promiseManager.publishPromise(this);
    }

    public Status getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    void setStatus(Status status) {
        this.status = status;
    }


    public Throwable getReason() {
        return reason;
    }

    public void setReason(Throwable reason) {
        this.reason = reason;
    }

    public Promise then(Resolver resolve, Rejector reject) throws Exception{
        Promise then = new Promise<>(resolve, reject);
        then.setData(this.data);
        then.setStatus(Status.PENDING);
        return then;
    }

    public Promise error(Catcher catcher) {
        catcher.error(reason);
        status = Status.RESOLVED;
        return this;
    }

    void onCreated() throws Throwable{
        if (runnable != null) {
            run();
        }
    }


    Promise onResolved(Object data) throws Throwable{
        if (status.equals(Status.PENDING)) {
            Object result = resolver.resolve(data);
            if (!(result instanceof Void)) {
                setData(result);
            }
            this.status = Status.RESOLVED;
        }
        return this;
    }

    Promise onRejected(Throwable reason) {
        if (status.equals(Status.PENDING)){
            this.status = Status.REJECTED;
            rejector.reject(reason);
        }
        return this;
    }

    @Override
    public T run() throws Throwable{
        T data = runnable.run();
        this.data = data;
        return data;
    }

    public static Promise[] all(Promise[] promises) {
        //todo: complete
        return null;
    }

    public static Promise[] race(Promise[] promises) {
        //todo: complete
        return null;
    }

    public static final PromiseManager promiseManager = new PromiseManager(3);
}
