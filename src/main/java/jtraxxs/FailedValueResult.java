package jtraxxs;

import java.util.Objects;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class FailedValueResult<V, E> extends ValueResult<V, E> {

    private final E error;

    FailedValueResult(E error) {
        this.error = error;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FailedValueResult)) {
            return false;
        }
        FailedValueResult<?, ?> other = (FailedValueResult<?, ?>) obj;
        return Objects.equals(error, other.error);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(error);
    }

    @Override
    public String toString() {
        return String.format("FailedValueResult{error=%s}", error);
    }

    @Override
    public boolean isSuccessful() {
        return false;
    }

    @Override
    public V value() {
        throw new IllegalStateException("Failed ValueResult has no value");
    }

    @Override
    public E error() {
        return error;
    }

    @Override
    public Stream<V> stream() {
        return Stream.empty();
    }

    @Override
    public Stream<E> errorStream() {
        return Stream.of(error);
    }

    @Override
    public ValueResult<V, E> onSuccess(Runnable runnable) {
        return this;
    }

    @Override
    public ValueResult<V, E> onSuccess(Consumer<? super V> consumer) {
        return this;
    }

    @Override
    public ValueResult<V, E> onFailure(Runnable runnable) {
        requireNonNull(runnable, "runnable must not be null");
        runnable.run();
        return this;
    }

    @Override
    public ValueResult<V, E> onFailure(Consumer<? super E> consumer) {
        requireNonNull(consumer, "consumer must not be null");
        consumer.accept(error());
        return this;
    }

    @Override
    public ValueResult<V, E> ensure(boolean flag, E error) {
        return this;
    }

    @Override
    public ValueResult<V, E> ensure(Supplier<Boolean> supplier, E error) {
        return this;
    }

    @Override
    public ValueResult<V, E> ensure(Predicate<? super V> predicate, E error) {
        return this;
    }

    @Override
    public ValueResult<V, E> ensure(Result<E> result) {
        return this;
    }

    @Override
    public ValueResult<V, E> ensure(Supplier<? extends Result<? extends E>> supplier) {
        return this;
    }

    @Override
    public ValueResult<V, E> ensure(Function<V, ? extends Result<? extends E>> function) {
        return this;
    }

    @Override
    public <W> ValueResult<W, E> take(ValueResult<W, E> result) {
        return fail(error);
    }

    @Override
    public <W> ValueResult<W, E> take(Supplier<ValueResult<W, E>> supplier) {
        return fail(error);
    }

    @Override
    public <W> ValueResult<W, E> take(Function<V, ValueResult<W, E>> function) {
        return fail(error);
    }

    // These methods may execute a given function depending on the state of the underlying ValueResult
    // ensure may return a different ValueResult object depending on its outcome.

    @Override
    public <S> ValueResult<S, E> map(Function<? super V, ? extends S> mapper) {
        return fail(this.error);
    }

    @Override
    public <S> ValueResult<V, S> mapError(Function<? super E, ? extends S> mapper) {
        requireNonNull(mapper, "mapper must not be null");
        return fail(mapper.apply(this.error()));
    }

    @Override
    public <W> ValueResult<W, E> flatMap(Function<? super V, ? extends ValueResult<? extends W, ? extends E>> function) {
        return fail(this.error());
    }

    @Override
    public <W, X> ValueResult<X, E> combine(BiFunction<V, W, X> function, ValueResult<W, E> other) {
        return fail(this.error());
    }

    @Override
    public V orElse(V other) {
        return other;
    }

    @Override
    public V orElseGet(Function<? super E, ? extends V> function) {
        requireNonNull(function, "function must not be null");
        return function.apply(error);
    }

    @Override
    public <X extends Throwable> V orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        requireNonNull(exceptionSupplier, "exceptionSupplier must not be null");
        throw exceptionSupplier.get();
    }

    @Override
    public Optional<V> toOptional() {
        return Optional.empty();
    }

    @Override
    public <T> T fold(Function<? super V, ? extends T> success, Function<? super E, ? extends T> failure) {
        requireNonNull(failure, "failure must not be null");
        return failure.apply(error);
    }

    @Override
    public ValueResult<V, E> onBoth(Consumer<? super V> success, Consumer<? super E> failure) {
        requireNonNull(failure, "failure must not be null");
        failure.accept(error);
        return this;
    }
}
