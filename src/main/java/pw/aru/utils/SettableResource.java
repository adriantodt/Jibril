package pw.aru.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class SettableResource<T> implements Resource<T> {
    private Exception ex;
    private T res;
    private State state = State.NOT_LOADED;

    @Nonnull
    @Override
    public State getState() {
        return state;
    }

    @Nullable
    @Override
    public T getValue() throws IllegalStateException {
        if (state != State.AVAILABLE) throw new IllegalStateException("Resource is unavailable");
        return res;
    }

    @Nullable
    @Override
    public T getOrNull() {
        return res;
    }

    @Nullable
    @Override
    public Exception getLoadException() {
        return ex;
    }

    @Nonnull
    @Override
    public T getOrDefault(@Nonnull T defValue) {
        return Objects.requireNonNullElse(res, defValue);
    }

    @Override
    public boolean load() {
        return state == State.AVAILABLE;
    }

    public void setResourceAvailable(T resource) {
        res = resource;
        ex = null;
        state = State.AVAILABLE;
    }

    public void setResourceLoading() {
        res = null;
        ex = null;
        state = State.LOADING;
    }

    public void setResourceUnavailable(Exception e) {
        res = null;
        ex = e;
        state = State.UNAVAILABLE;
    }

    public void setResourceUnavailable() {
        res = null;
        ex = null;
        state = State.UNAVAILABLE;
    }

    @Override
    public void close() {
        setResourceUnavailable();
    }
}
