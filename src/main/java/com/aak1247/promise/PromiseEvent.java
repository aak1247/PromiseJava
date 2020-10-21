package com.aak1247.promise;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventTranslatorThreeArg;
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

    public static final EventTranslatorThreeArg TRANSLATOR_REPEATED = (EventTranslatorThreeArg<PromiseEvent, Promise, PromiseStatus, Boolean>) (event, sequence, promise, after, repeated) -> {
        TRANSLATOR.translateTo(event, sequence, promise, after);
        event.repeated = repeated;
    };

    private PromiseStatus before;
    private PromiseStatus after;
    private Promise promise;
    private boolean repeated;

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
        return !this.repeated
                && o instanceof PromiseEvent
                && !((PromiseEvent) o).repeated
                && ((PromiseEvent) o).getAfter().equals(after)
                && ((PromiseEvent) o).getBefore().equals(before)
                && ((PromiseEvent) o).getPromise().equals(promise);
    }

    @Override
    public int hashCode() {
        return Objects.hash(before, after, promise);
    }
}
