package com.aak1247.promise;

import java.util.Objects;

public class Data<T> {
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Data{" +
                "value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Data)) return false;
        Data<?> data = (Data<?>) object;
        return Objects.equals(getValue(), data.getValue());
    }
}
