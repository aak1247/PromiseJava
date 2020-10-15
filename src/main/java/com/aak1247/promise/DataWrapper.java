package com.aak1247.promise;

public class DataWrapper<T> {
    private T data;

    public DataWrapper() {
    }

    public DataWrapper(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
