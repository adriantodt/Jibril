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
    public LoadState getLoadState() {
        return closed ? LoadState.UNAVAILABLE : LoadState.AVAILABLE;
    }

    @Nullable
    @Override
    public T getResource() throws IllegalStateException {
        if (closed) throw new IllegalStateException("Resource is unavailable");
        return value;
    }

    @Nullable
    @Override
    public Exception getResourceError() {
        return null;
    }

    @Override
    public boolean loadResource() {
        return !closed;
    }

    @Override
    public void close() {
        closed = true;
        value = null;
    }
}
