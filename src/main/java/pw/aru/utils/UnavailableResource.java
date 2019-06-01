package pw.aru.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class UnavailableResource implements Resource {
    static final Resource instance = new UnavailableResource();

    private UnavailableResource() {
    }

    @Nonnull
    @Override
    public State getState() {
        return State.UNAVAILABLE;
    }

    @Nullable
    @Override
    public Object getValue() throws IllegalStateException {
        throw new IllegalStateException("Resource is unavailable");
    }

    @Nullable
    @Override
    public Exception getLoadException() {
        return null;
    }

    @Override
    public boolean load() {
        return false;
    }

    @Nullable
    @Override
    public Object getOrNull() {
        return null;
    }

    @Nonnull
    @Override
    public Object getOrDefault(@Nonnull Object defValue) {
        return defValue;
    }

    @Override
    public void close() {
    }
}
