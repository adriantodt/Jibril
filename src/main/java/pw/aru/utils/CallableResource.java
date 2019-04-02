package pw.aru.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

class CallableResource<T> implements Resource<T> {
    private final Callable<T> callable;
    private final boolean reloadable;
    private final Object resLock = new Object();
    private Exception ex;
    private T res;
    private State state = State.NOT_LOADED;

    CallableResource(Callable<T> callable, boolean reloadable) {
        this.callable = callable;
        this.reloadable = reloadable;
    }

    @Nonnull
    @Override
    public State getState() {
        return state;
    }

    @Nullable
    @Override
    public T getValue() throws IllegalStateException {
        synchronized (resLock) {
            if (!load()) throw new IllegalStateException("Resource is unavailable");
            return res;
        }
    }

    @Nullable
    @Override
    public Exception getLoadException() {
        return ex;
    }

    @Override
    public boolean load() {
        synchronized (resLock) {
            if (state == State.LOADING) throw new IllegalStateException("Invalid 'LOADING' state before loading.");
            if (state == State.AVAILABLE) return true;
            if (state == State.UNAVAILABLE && !reloadable) return false;

            state = State.LOADING;

            try {
                res = callable.call();
                state = State.AVAILABLE;
                return true;
            } catch (Exception e) {
                ex = e;
                state = State.UNAVAILABLE;
                return false;
            } finally {
                if (state == State.LOADING) {
                    state = State.NOT_LOADED;
                }
            }
        }
    }

    @Override
    public void close() {
        state = State.UNAVAILABLE;
        res = null;
        ex = null;
    }
}
