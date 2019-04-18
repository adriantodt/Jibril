package pw.aru.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class FutureResource<T> implements Resource<T> {
    private final Future<T> future;

    private final SettableResource<T> resource = new SettableResource<>();

    FutureResource(Future<T> future) {
        this.future = future;
    }


    @Nonnull
    @Override
    public State getState() {
        return resource.getState();
    }

    @Nullable
    @Override
    public T getValue() throws IllegalStateException {
        return resource.getValue();
    }

    @Nullable
    @Override
    public Exception getLoadException() {
        return resource.getLoadException();
    }

    @Override
    public boolean load() {
        try {
            resource.setResourceLoading();
            resource.setResourceAvailable(future.get());
            return true;
        } catch (InterruptedException | ExecutionException e) {
            resource.setResourceUnavailable(e);
            return false;
        }
    }

    @Override
    public void close() {
        resource.close();
    }
}
