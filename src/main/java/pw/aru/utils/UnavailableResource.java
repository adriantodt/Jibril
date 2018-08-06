package pw.aru.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class UnavailableResource implements Resource {
    static Resource instance = new UnavailableResource();

    private UnavailableResource() {
    }

    @Nonnull
    @Override
    public LoadState getLoadState() {
        return LoadState.UNAVAILABLE;
    }

    @Nullable
    @Override
    public Object getResource() throws IllegalStateException {
        throw new IllegalStateException("Resource is unavailable");
    }

    @Nullable
    @Override
    public Exception getResourceError() {
        return null;
    }

    @Override
    public boolean loadResource() {
        return false;
    }

    @Override
    public void close() {
    }
}
