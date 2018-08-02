package pw.aru.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

class CallableResource<T> implements Resource<T> {
    private final Callable<T> callable;
    private final Object resLock = new Object();
    private Exception ex;
    private T res;
    private LoadState state = LoadState.NOT_LOADED;

    CallableResource(Callable<T> callable) {
        this.callable = callable;
    }

    @Nonnull
    @Override
    public LoadState getLoadState() {
        return state;
    }

    @Nullable
    @Override
    public T getResource() throws IllegalStateException {
        synchronized (resLock) {
            if (!loadResource()) throw new IllegalStateException("Resource is unavailable");
            return res;
        }
    }

    @Nullable
    @Override
    public Exception getResourceError() {
        return ex;
    }

    @Override
    public boolean loadResource() {
        synchronized (resLock) {
            if (state == LoadState.LOADING) throw new AssertionError("Impossible to happen?");
            if (state == LoadState.AVAILABLE) return true;
            if (state == LoadState.UNAVAILABLE) return false;

            state = LoadState.LOADING;

            try {
                res = callable.call();
                state = LoadState.AVAILABLE;
                return true;
            } catch (Exception e) {
                ex = e;
                state = LoadState.UNAVAILABLE;
                return false;
            }
        }
    }
}
