package pw.aru.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * A Resource, in sum, is an object capable of holding a state regarding the object or calculation it holds.
 * <br>
 * The entry methods are {@link Resource#load()},
 * which returns true if the value is available at the end of the calculation,
 * {@link Resource#getValue()},
 * which throws if there's no object available,
 * and {@link Resource#getOrNull()}, which is guaranteed to return even if the resource is unavailable.
 * <br>
 * <h3>Implementation Notes:</h3>
 * If you're willing to implement this interface, be sure to apply the following contracts:
 * <ul>
 * <li>{@link Resource#load()} must return true if <code>{@link Resource#getOrNull()} != null</code>.</li>
 * <li>{@link Resource#close()} must clear any held resources and set the state to {@link State#UNAVAILABLE}.</li>
 * <li>{@link Resource#getValue()} must throw an {@link IllegalStateException} if the resource is unavailable.</li>
 * <li>{@link Resource#getOrNull()} and {@link Resource#getOrDefault(Object)} must NOT throw even if the resource is unavailable.</li>
 * <li>{@link Resource#getOrDefault(Object)} may never return null.</li>
 * <li>If your {@link Resource#load()} implementation throws, override the default implementation of {@link Resource#getOrNull()} and {@link Resource#getOrDefault(Object)}.</li>
 * </ul>
 *
 * @param <T> the type of the returned object or the result of the calculation.
 */
public interface Resource<T> extends Closeable {
    @Nonnull
    State getState();

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

    static <T> Resource<T> ofFuture(Future<T> future) {
        return new FutureResource<>(future);
    }

    @SuppressWarnings("unchecked")
    static <T> Resource<T> unavailable() {
        return UnavailableResource.instance;
    }

    //Methods

    @Nullable
    T getValue() throws IllegalStateException;

    @Nullable
    Exception getLoadException();

    boolean load();

    @Nullable
    default T getOrNull() {
        if (load()) return getValue();
        return null;
    }

    @Nonnull
    default T getOrDefault(@Nonnull T defValue) {
        return Objects.requireNonNullElse(getValue(), defValue);
    }

    @Override
    void close();

    enum State {
        NOT_LOADED, LOADING, AVAILABLE, UNAVAILABLE
    }
}

