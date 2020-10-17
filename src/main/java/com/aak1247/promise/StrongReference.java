package com.aak1247.promise;

public class StrongReference<T> {
    private T data;

    public StrongReference() {
    }

    public StrongReference(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
