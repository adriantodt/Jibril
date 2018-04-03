package jibril.utils;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public interface Resource<T> {
    enum LoadState {
        NOT_LOADED, LOADING, AVAILABLE, UNAVAILABLE
    }

    //Creators

    static <T> Resource<T> of(T value) {
        return new AvailableResource<>(value);
    }

    static <T> Resource<T> of(Callable<T> callable) {
        return new CallableResource<>(callable);
    }

    @SuppressWarnings("unchecked")
    static <T> Resource<T> unavailable() {
        return UnavailableResource.instance;
    }

    //Methods

    @NotNull
    LoadState getLoadState();

    @Nullable
    T getResource() throws IllegalStateException;

    @Nullable
    Exception getResourceError();

    boolean loadResource();

}

class AvailableResource<T> implements Resource<T> {
    private final T value;

    AvailableResource(T value) {
        this.value = value;
    }

    @NotNull
    @Override
    public LoadState getLoadState() {
        return LoadState.AVAILABLE;
    }

    @Nullable
    @Override
    public T getResource() throws IllegalStateException {
        return value;
    }

    @Override
    public boolean loadResource() {
        return true;
    }

    @Nullable
    @Override
    public Exception getResourceError() {
        return null;
    }
}

class CallableResource<T> implements Resource<T> {
    private final Callable<T> callable;
    private final Object resLock = new Object();
    private Exception ex;
    private T res;
    private LoadState state = LoadState.NOT_LOADED;

    CallableResource(Callable<T> callable) {
        this.callable = callable;
    }

    @NotNull
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

    @Nullable
    @Override
    public Exception getResourceError() {
        return ex;
    }
}

class UnavailableResource implements Resource {
    static Resource instance = new UnavailableResource();

    private UnavailableResource() {
    }

    @NotNull
    @Override
    public LoadState getLoadState() {
        return LoadState.UNAVAILABLE;
    }

    @Nullable
    @Override
    public Object getResource() throws IllegalStateException {
        throw new IllegalStateException("Resource is unavailable");
    }

    @Override
    public boolean loadResource() {
        return false;
    }

    @Nullable
    @Override
    public Exception getResourceError() {
        return null;
    }
}
