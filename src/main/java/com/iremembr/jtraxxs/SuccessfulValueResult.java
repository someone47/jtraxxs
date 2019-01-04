package com.iremembr.jtraxxs;

import java.util.Objects;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

final class SuccessfulValueResult<V, E> extends ValueResult<V, E> {

    private final V value;

    SuccessfulValueResult(V value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SuccessfulValueResult)) {
            return false;
        }
        SuccessfulValueResult<?, ?> other = (SuccessfulValueResult<?, ?>) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return String.format("SuccessfulValueResult{value=%s}", value);
    }

    @SuppressWarnings("unchecked")
    public <W> ValueResult<W, E> castValue(Class<W> clazz) {
        if (!clazz.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException(String.format(
                    "Can not cast the value to the given type. The given type is not a superclass of the"
                    + " type of the value. value type ='%s', given type = '%s', value = '%s'",
                    value.getClass(), clazz, value
            ));
        }
        return (ValueResult<W, E>) this;
    }

    @SuppressWarnings("unchecked")
    public <F> ValueResult<V, F> castError(Class<F> clazz) {
        return (ValueResult<V, F>) this;
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public V value() {
        return value;
    }

    @Override
    public E error() {
        throw new IllegalStateException("Successful ValueResult has no error");
    }

    @Override
    public Stream<V> stream() {
        return Stream.of(value);
    }

    @Override
    public Stream<E> errorStream() {
        return Stream.empty();
    }

    @Override
    public ValueResult<V, E> onSuccess(Runnable runnable) {
        requireNonNull(runnable, "runnable must not be null");
        runnable.run();
        return this;
    }

    @Override
    public ValueResult<V, E> onSuccess(Consumer<? super V> consumer) {
        requireNonNull(consumer, "consumer must not be null");
        consumer.accept(value);
        return this;
    }

    @Override
    public ValueResult<V, E> onFailure(Runnable runnable) {
        return this;
    }

    @Override
    public ValueResult<V, E> onFailure(Consumer<? super E> consumer) {
        return this;
    }

    @Override
    public ValueResult<V, E> onBoth(Consumer<? super V> success, Consumer<? super E> failure) {
        requireNonNull(success, "success must not be null");
        success.accept(value);
        return this;
    }

    @Override
    public ValueResult<V, E> ensure(boolean flag, E error) {
        return flag ? this : fail(error);
    }

    @Override
    public ValueResult<V, E> ensure(Supplier<Boolean> supplier, E error) {
        requireNonNull(supplier, "supplier must not be null");
        return ensure(supplier.get(), error);
    }

    @Override
    public ValueResult<V, E> ensure(Predicate<? super V> predicate, E error) {
        requireNonNull(predicate, "runnable must not be null");
        return predicate.test(value()) ? this : fail(error);
    }

    @Override
    public ValueResult<V, E> ensure(Result<? extends E> result) {
        requireNonNull(result, "result must not be null");
        return result.hasFailed() ? fail(result.error()) : this;
    }

    @Override
    public ValueResult<V, E> ensure(Supplier<? extends Result<? extends E>> supplier) {
        requireNonNull(supplier, "supplier must not be null");
        Result<? extends E> result = supplier.get();
        return result.hasFailed() ? fail(result.error()) : this;
    }

    @Override
    public ValueResult<V, E> ensure(Function<? super V, ? extends Result<? extends E>> function) {
        requireNonNull(function, "function must not be null");
        Result<? extends E> result = function.apply(value);
        return result.hasFailed() ? fail(result.error()) : this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> ValueResult<S, E> take(ValueResult<? extends S, ? extends E> other) {
        requireNonNull(other, "other must not be null");
        return (ValueResult<S, E>) other;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> ValueResult<S, E> take(Supplier<? extends ValueResult<? extends S, ? extends E>> supplier) {
        requireNonNull(supplier, "supplier must not be null");
        return (ValueResult<S, E>) supplier.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> ValueResult<S, E> take(Function<? super V, ? extends ValueResult<? extends S, ? extends E>> function) {
        requireNonNull(function, "function must not be null");
        return (ValueResult<S, E>) function.apply(value());
    }

    @Override
    public <W> ValueResult<W, E> map(Function<? super V, ? extends W> mapper) {
        requireNonNull(mapper, "mapper must not be null");
        return ok(mapper.apply(value()));
    }

    @Override
    public <W> ValueResult<V, W> mapError(Function<? super E, ? extends W> mapper) {
        return ok(this.value());
    }

    @Override
    public <W> ValueResult<W, E> flatMap(Function<? super V, ? extends ValueResult<? extends W, ? extends E>> mapper) {
        requireNonNull(mapper, "mapper must not be null");
        @SuppressWarnings("unchecked")
        ValueResult<W, E> result = (ValueResult<W, E>) mapper.apply(value());
        return requireNonNull(result);
    }

    @Override
    public <W, X> ValueResult<X, E> combine(BiFunction<? super V, ? super W, ? extends X> function, ValueResult<? extends W, ? extends E> other) {
        requireNonNull(function, "function must not be null");
        requireNonNull(other, "other must not be null");
        return other.isSuccessful()
                ? ok(function.apply(value(), other.value()))
                : fail(other.error());
    }

    @Override
    public V orElse(V other) {
        return value;
    }

    @Override
    public V orElseGet(Function<? super E, ? extends V> function) {
        return value;
    }

    @Override
    public <X extends Throwable> V orElseThrow(Supplier<? extends X> exceptionSupplier) {
        return value;
    }

    @Override
    public <T> T fold(Function<? super V, ? extends T> success, Function<? super E, ? extends T> failure) {
        requireNonNull(success, "success must not be null");
        return success.apply(value);
    }

    @Override
    public Optional<V> toOptional() {
        return Optional.ofNullable(value);
    }

    @Override
    public VoidResult<E> toVoidResult() {
        return VoidResult.ok();
    }
}
