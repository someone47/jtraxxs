package jtraxxs;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class SuccessfulVoidResult<E> extends VoidResult<E> {

    SuccessfulVoidResult() {
    }

    @Override
    public String toString() {
        return "SuccessfulVoidResult";
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof SuccessfulVoidResult;
    }

    @Override
    public int hashCode() {
        return 4711;
    }


    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public E error() {
        throw new IllegalStateException("Successful ValueResult has no error");
    }

    @Override
    public Stream<E> errorStream() {
        return Stream.empty();
    }

    @Override
    public VoidResult<E> onSuccess(Runnable runnable) {
        requireNonNull(runnable, "runnable must not be null");
        runnable.run();
        return this;
    }

    @Override
    public VoidResult<E> onFailure(Runnable runnable) {
        return this;
    }

    @Override
    public VoidResult<E> onFailure(Consumer<? super E> consumer) {
        return this;
    }

    @Override
    public VoidResult<E> onBoth(Runnable success, Consumer<? super E> failure) {
        success.run();
        return this;
    }

    @Override
    public VoidResult<E> ensure(boolean flag, E error) {
        return flag ? this : fail(error);
    }

    @Override
    public VoidResult<E> ensure(Supplier<Boolean> supplier, E error) {
        requireNonNull(supplier, "supplier must not be null");
        return ensure(supplier.get(), error);
    }

    @Override
    public VoidResult<E> ensure(Result<E> result) {
        requireNonNull(result, "result must not be null");
        return result.hasFailed() ? fail(result.error()) : this;
    }

    @Override
    public VoidResult<E> ensure(Supplier<? extends Result<? extends E>> supplier) {
        requireNonNull(supplier, "supplier must not be null");
        Result<? extends E> result = supplier.get();
        return result.hasFailed() ? fail(result.error()) : this;
    }

    @Override
    public <S> VoidResult<S> mapError(Function<? super E, ? extends S> function) {
        return ok();
    }

    @Override
    public <T> T fold(Supplier<? extends T> success, Function<? super E, ? extends T> failure) {
        requireNonNull(success, "success must not be null");
        return success.get();
    }
}
