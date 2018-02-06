package com.aak1247.promise;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslatorTwoArg;

public class PromiseEvent {
    private Status before;
    private Status after;
    private Promise promise;

    public Status getBefore() {
        return before;
    }

    public void setBefore(Status before) {
        this.before = before;
    }

    public Status getAfter() {
        return after;
    }

    public void setAfter(Status after) {
        this.after = after;
    }

    public Promise getPromise() {
        return promise;
    }

    public void setPromise(Promise promise) {
        this.promise = promise;
    }

    public static final EventFactory<PromiseEvent> PROMISE_EVENT_FACTORY = new EventFactory<PromiseEvent>() {
        @Override
        public PromiseEvent newInstance() {
            return new PromiseEvent();
        }
    };

    public static final EventTranslatorTwoArg TRANSLATOR = (EventTranslatorTwoArg<PromiseEvent, Promise, Status>) (event, sequence, promise, after) -> {
        event.setBefore(promise.getStatus());
        promise.setStatus(after);
        event.setAfter(after);
        event.setPromise(promise);
    };

    @Override
    public boolean equals(Object o) {
        return o != null
                && o instanceof PromiseEvent
                && ((PromiseEvent) o).getAfter().equals(after)
                && ((PromiseEvent) o).getBefore().equals(before)
                && ((PromiseEvent) o).getPromise().equals(promise);
    }
}
