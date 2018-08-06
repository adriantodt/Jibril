package pw.aru.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SettableResource<T> implements Resource<T> {
    private Exception ex;
    private T res;
    private LoadState state = LoadState.NOT_LOADED;

    @Nonnull
    @Override
    public LoadState getLoadState() {
        return state;
    }

    @Nullable
    @Override
    public T getResource() throws IllegalStateException {
        if (state != LoadState.AVAILABLE) throw new IllegalStateException("Resource is unavailable");
        return res;
    }

    @Nullable
    @Override
    public Exception getResourceError() {
        return ex;
    }

    @Override
    public boolean loadResource() {
        return state != LoadState.AVAILABLE;
    }

    public void setResourceAvailable(T resource) {
        res = resource;
        ex = null;
        state = LoadState.AVAILABLE;
    }

    public void setResourceLoading() {
        res = null;
        ex = null;
        state = LoadState.LOADING;
    }

    public void setResourceUnavailable(Exception e) {
        res = null;
        ex = e;
        state = LoadState.UNAVAILABLE;
    }

    public void setResourceUnavailable() {
        res = null;
        ex = null;
        state = LoadState.UNAVAILABLE;
    }

    @Override
    public void close() {
        setResourceUnavailable();
    }
}
