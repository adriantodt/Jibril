package pw.aru.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class AvailableResource<T> implements Resource<T> {
    private final T value;

    AvailableResource(T value) {
        this.value = value;
    }

    @Nonnull
    @Override
    public LoadState getLoadState() {
        return LoadState.AVAILABLE;
    }

    @Nullable
    @Override
    public T getResource() throws IllegalStateException {
        return value;
    }

    @Nullable
    @Override
    public Exception getResourceError() {
        return null;
    }

    @Override
    public boolean loadResource() {
        return true;
    }
}
