package pw.aru.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class AvailableResource<T> implements Resource<T> {
    private boolean closed = false;
    private T value;

    AvailableResource(T value) {
        this.value = value;
    }

    @Nonnull
    @Override
    public State getState() {
        return closed ? State.UNAVAILABLE : State.AVAILABLE;
    }

    @Nullable
    @Override
    public T getValue() throws IllegalStateException {
        if (closed) throw new IllegalStateException("Resource is unavailable");
        return value;
    }

    @Nullable
    @Override
    public T getOrNull() {
        return value;
    }

    @Nullable
    @Override
    public Exception getLoadException() {
        return null;
    }

    @Override
    public boolean load() {
        return !closed;
    }

    @Override
    public void close() {
        closed = true;
        value = null;
    }
}
