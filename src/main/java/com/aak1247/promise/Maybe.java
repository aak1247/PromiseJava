package com.aak1247.promise;

public class Maybe<A, B> {
    private boolean isSet = false;
    private A a = null;
    private B b = null;

    private Maybe() {

    }

    public static <A, B> Maybe<A, B> of(Class<A> aClass, Class<B> bClass) {
        return new Maybe<>();
    }

    public boolean isAPresent() {
        return a != null;
    }

    public boolean isBPresent() {
        return b != null;
    }

    public A getA() {
        return a;
    }

    public void setA(A obj) {
        if (isSet) {
            throw new RuntimeException("set before");
        }
        this.a = obj;
        isSet = true;
    }

    public B getB() {
        return b;
    }

    public void setB(B obj) {
        if (isSet) {
            throw new RuntimeException("set before");
        }
        this.b = obj;
        isSet = true;
    }
}
