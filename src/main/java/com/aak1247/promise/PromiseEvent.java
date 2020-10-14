package com.aak1247.promise;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslatorTwoArg;

import java.util.Objects;

public class PromiseEvent {
    public static final EventFactory<PromiseEvent> PROMISE_EVENT_FACTORY = PromiseEvent::new;
    public static final EventTranslatorTwoArg TRANSLATOR = (EventTranslatorTwoArg<PromiseEvent, Promise, PromiseStatus>) (event, sequence, promise, after) -> {
        event.setBefore(promise.getStatus());
        if (promise != null) {
            promise.setStatus(after);
        }
        event.setAfter(after);
        event.setPromise(promise);
    };
    private PromiseStatus before;
    private PromiseStatus after;
    private Promise promise;

    public PromiseStatus getBefore() {
        return before;
    }

    public void setBefore(PromiseStatus before) {
        this.before = before;
    }

    public PromiseStatus getAfter() {
        return after;
    }

    public void setAfter(PromiseStatus after) {
        this.after = after;
    }

    public Promise getPromise() {
        return promise;
    }

    public void setPromise(Promise promise) {
        this.promise = promise;
    }

    @Override
    public boolean equals(Object o) {
        return o != null
                && o instanceof PromiseEvent
                && ((PromiseEvent) o).getAfter().equals(after)
                && ((PromiseEvent) o).getBefore().equals(before)
                && ((PromiseEvent) o).getPromise().equals(promise);
    }

    @Override
    public int hashCode() {
        return Objects.hash(before, after, promise);
    }
}
