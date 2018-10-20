package jtraxxs;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class FailedVoidResult<E> extends VoidResult<E> {

    private final E error;

    FailedVoidResult(E error) {
        this.error = error;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FailedVoidResult)) {
            return false;
        }
        FailedVoidResult<?> other = (FailedVoidResult<?>) obj;
        return Objects.equals(error, other.error);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(error);
    }

    @Override
    public String toString() {
        return String.format("FailedVoidResult{error=%s}", error);
    }

    @Override
    public boolean isSuccessful() {
        return false;
    }

    @Override
    public E error() {
        return error;
    }

    @Override
    public Stream<E> errorStream() {
        return Stream.of(error);
    }

    @Override
    public VoidResult<E> onSuccess(Runnable runnable) {
        return this;
    }

    @Override
    public VoidResult<E> onFailure(Runnable runnable) {
        requireNonNull(runnable, "runnable must not be null");
        runnable.run();
        return this;
    }

    @Override
    public VoidResult<E> onFailure(Consumer<? super E> consumer) {
        requireNonNull(consumer, "consumer must not be null");
        consumer.accept(error());
        return this;
    }

    @Override
    public VoidResult<E> onBoth(Runnable success, Consumer<? super E> failure) {
        requireNonNull(failure, "failure must not be null");
        failure.accept(error);
        return this;
    }

    @Override
    public VoidResult<E> ensure(boolean flag, E error) {
        return this;
    }

    @Override
    public VoidResult<E> ensure(Supplier<Boolean> supplier, E error) {
        return this;
    }

    @Override
    public VoidResult<E> ensure(Result<E> result) {
        return this;
    }

    @Override
    public VoidResult<E> ensure(Supplier<? extends Result<? extends E>> supplier) {
        return this;
    }

    @Override
    public <F> VoidResult<F> mapError(Function<? super E, ? extends F> mapper) {
        requireNonNull(mapper, "mapper must not be null");
        return fail(mapper.apply(this.error()));
    }

    @Override
    public <T> T fold(Supplier<? extends T> success, Function<? super E, ? extends T> failure) {
        requireNonNull(failure, "failure must not be null");
        return failure.apply(error);
    }
}
