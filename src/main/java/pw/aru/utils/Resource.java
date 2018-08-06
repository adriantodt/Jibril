package pw.aru.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

public interface Resource<T> extends AutoCloseable {
    enum LoadState {
        NOT_LOADED, LOADING, AVAILABLE, UNAVAILABLE
    }

    //Creators

    static <T> Resource<T> of(T value) {
        return new AvailableResource<>(value);
    }

    static <T> Resource<T> of(Callable<T> callable) {
        return new CallableResource<>(callable, false);
    }

    static <T> Resource<T> ofReloadable(Callable<T> callable) {
        return new CallableResource<>(callable, true);
    }

    static <T> SettableResource<T> settable() {
        return new SettableResource<>();
    }

    static <T> SettableResource<T> ofSettable(T value) {
        SettableResource<T> resource = settable();
        resource.setResourceAvailable(value);
        return resource;
    }

    @SuppressWarnings("unchecked")
    static <T> Resource<T> unavailable() {
        return UnavailableResource.instance;
    }

    //Methods

    @Nonnull
    LoadState getLoadState();

    @Nullable
    T getResource() throws IllegalStateException;

    @Nullable
    Exception getResourceError();

    boolean loadResource();

    @Nullable
    default T getResourceOrNull() {
        if (loadResource()) return getResource();
        return null;
    }

    @Override
    void close();
}

