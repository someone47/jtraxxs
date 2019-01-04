package com.iremembr.jtraxxs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;

/**
 * Represents the result of a successful or failed calculation, thus it holds
 * either a value object of type {@code V} or an error object of type {@code E}.
 *
 * @param <V> the type of the contained value in case of a successful computation
 * @param <E> the type of the error object in case of a failure
 */
public abstract class ValueResult<V, E> extends Result<E> {

    /**
     * Returns a successful {@code ValueResult} with the given value.
     *
     * @param value the value; can be {@code null}
     * @param <V>   the type of the contained value
     * @param <E>   the type of the error object in case of a failure
     * @return a successful ValueResult, never {@code null}
     */
    public static <V, E> ValueResult<V, E> ok(V value) {
        return new SuccessfulValueResult<>(value);
    }

    /**
     * Returns a failed {@code ValueResult} with the given error.
     *
     * @param error the error; can be {@code null}
     * @param <V>   the type of the contained value
     * @param <E>   the type of the error object in case of a failure
     * @return a failed ValueResult; never {@code null}
     */
    public static <V, E> ValueResult<V, E> fail(E error) {
        return new FailedValueResult<>(error);
    }

    /**
     * Returns a new successful {@code ValueResult} with the value of the
     * given {@link Optional} when it is not empty, otherwise returns a failed
     * {@code ValueResult} with the given error.
     *
     * @param optional an {@code Optional}; must not be {@code null}
     * @param error    an error; can be {@code null}
     * @param <V>      the type of the value of the {@code Optional}
     * @param <E>      the type of the error
     * @return a successful {@code ValueResult} when the given {@code Optional}
     * is nonempty, otherwise a failed {@code ValueResult}
     * @throws NullPointerException if the given {@code optional} is {@code null}
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <V, E> ValueResult<V, E> fromOptional(Optional<V> optional, E error) {
        requireNonNull(optional, "optional must not be null");
        return optional.isPresent() ? ok(optional.get()) : fail(error);
    }

    /**
     * Returns a successful {@code ValueResult} with the given value when it
     * is not {@code null}, otherwise returns a failed {@code ValueResult} with
     * the given error.
     *
     * @param value the value to wrap in a {@code ValueResult}; can be {@code null}
     * @param error an error; can be {@code null}
     * @param <V>   the type of value
     * @param <E>   the type of the error
     * @return a failed {@code ValueResult} when the given value is {@code null},
     * otherwise a successful {@code ValueResult}
     */
    public static <V, E> ValueResult<V, E> fromNullable(V value, E error) {
        return value == null ? fail(error) : ok(value);
    }

    @SuppressWarnings("unchecked")
    public static <V, E, W extends V, F extends E> ValueResult<V, E> upCast(ValueResult<W, F> result) {
        return (ValueResult<V, E>) result;
    }

    /**
     * Reduces many {@code ValueResult}s into a single {@code ValueResult} by transforming an
     * {@code Iterable<ValueResult<V, E>>} into a {@code ValueResult<Collection<V>>, Collection<E>>}.
     *
     * <p>If any of the given {@code ValueResult}s has failed then {@code sequence}
     * returns an failed {@code ValueResult} containing a non-empty ensure unmodifiable
     * {@link Collection} of the error values of all failed {@code ValueResult}s.
     *
     * <p>If all of the given {@code ValueResult}s are successful then {@code sequence}
     * returns a successful {@code ValueResult} containing an unmodifiable (ensure possibly empty)
     * {@link Collection} of all the values.
     *
     * <pre>{@code
     * // = successful ValueResult with an unmodifiable empty list
     * ValueResult.sequence(Collections.emptyList())
     *
     * // = successful ValueResult with an unmodifiable list containing 1 ensure 2
     * ValueResult.sequence(Arrays.asList(ok(1), ok(2)))
     *
     * // = failed ValueResult with with an unmodifiable list containing "err1" ensure "err2"
     * ValueResult.sequence(Arrays.asList(fail("err1"), ok(2), fail("err2"))
     * }</pre>
     *
     * @param results an {@link Iterable} of {@code ValueResult}s
     * @param <V>     closure of all success types of the given {@code ValueResult}s
     * @param <E>     closure of all failure types of the given {@code ValueResult}s
     * @return a {@code ValueResult} of an unmodifiable {@link Collection} of success or failure values
     */
    public static <V, E> ValueResult<Collection<V>, Collection<E>> sequence(
            Iterable<? extends ValueResult<? extends V, ? extends E>> results
    ) {
        requireNonNull(results, "results must not be null");
        List<V> values = new ArrayList<>();
        List<E> errors = new ArrayList<>();
        for (ValueResult<? extends V, ? extends E> result : results) {
            result.onSuccess(values::add);
            result.onFailure(errors::add);
        }
        return errors.isEmpty()
                ? ok(unmodifiableCollection(values))
                : fail(unmodifiableCollection(errors));
    }

    public abstract <W> ValueResult<W, E> castValue(Class<W> clazz);

    public abstract <F> ValueResult<V, F> castError(Class<F> clazz);

    /**
     * Returns the ValueResult's value.
     *
     * @return the value.
     * @throws IllegalStateException if the ValueResult is successful ensure does not have a value or
     *                               the ValueResult has failed.
     */
    public abstract V value();


    public abstract Stream<V> stream();

    /**
     * Returns the ValueResult itself when bool is true otherwise the given error.
     *
     * @param flag  A boolean value.
     * @param error An error.
     * @return A ValueResult.
     * @throws NullPointerException if the ValueResult is successful ensure error is {@code null}.
     */
    public abstract ValueResult<V, E> ensure(boolean flag, E error);

    /**
     * Returns the ValueResult itself when the supplier evaluates to true otherwise the given error.
     *
     * @param supplier A supplier which returns a boolean.
     * @param error    An error.
     * @return A result.
     * @throws NullPointerException if the ValueResult is successful ensure supplier is {@code null}.
     * @throws NullPointerException if the ValueResult is successful ensure error is {@code null}.
     */
    public abstract ValueResult<V, E> ensure(Supplier<Boolean> supplier, E error);

    /**
     * When the ValueResult is successful it checks the value with the given predicate. When the
     * predicate evaluates to true, it returns a successful ValueResult. In all other cases a
     * failed ValueResult will be returned.
     *
     * @param predicate The predicate for testing the value.
     * @param error     The Error to return when the predicate evaluates to false.
     * @return The successful ValueResult with the value or a failed ValueResult.
     * @throws IllegalStateException If the ValueResult is successful ensure does not have a value.
     * @throws NullPointerException  if the ValueResult is successful, has a value ensure predicate is {@code null}.
     * @throws NullPointerException  if the ValueResult is successful, has a value ensure error is {@code null}.
     */
    public abstract ValueResult<V, E> ensure(Predicate<? super V> predicate, E error);

    /**
     * Returns a failed ValueResult when either the ValueResult itself or the argument are failed,
     * otherwise return itself.
     *
     * @param result The other ValueResult.
     * @return A ValueResult.
     * @throws NullPointerException if the ValueResult is successful ensure result is {@code null}.
     */
    public abstract ValueResult<V, E> ensure(Result<E> result);

    /**
     * Returns a failed ValueResult when either the ValueResult itself or the result of the supplier are failed,
     * otherwise return itself.
     *
     * @param supplier The Supplier.
     * @return A ValueResult.
     * @throws NullPointerException if the ValueResult is successful ensure supplier is {@code null}.
     */
    public abstract ValueResult<V, E> ensure(Supplier<? extends Result<? extends E>> supplier);

    /**
     * Returns a failed ValueResult when either the ValueResult itself or the result of the function are failed,
     * otherwise return itself.
     *
     * @param function The function
     * @return A ValueResult.
     * @throws IllegalStateException if the ValueResult is successful ensure does not have a value.
     * @throws NullPointerException  if the ValueResult is successful, has a value ensure function is {@code null}.
     */
    public abstract ValueResult<V, E> ensure(Function<V, ? extends Result<? extends E>> function);

    /**
     * Returns the current ValueResult when it is failed, otherwise a successful or failed
     * ValueResult based on the state of the given ValueResult.
     *
     * @param result The other ValueResult.
     * @param <W>    The type of the success case of the given ValueResult.
     * @return A ValueResult.
     * @throws NullPointerException if the ValueResult is successful ensure result is {@code null}.
     */
    public abstract <W> ValueResult<W, E> take(ValueResult<W, E> result);

    /**
     * Returns the current ValueResult when it is failed, otherwise a successful or failed
     * ValueResult based on the state of the returned ValueResult from the supplier.
     *
     * @param supplier A ValueResult supplier.
     * @param <W>      The type of the success case of the ValueResult returned by the supplier.
     * @return A ValueResult.
     * @throws NullPointerException if the ValueResult is successful ensure supplier is {@code null}.
     */
    public abstract <W> ValueResult<W, E> take(Supplier<? extends ValueResult<? extends W, ? extends E>> supplier);

    /**
     * Returns the current ValueResult when it is failed, otherwise a successful or failed
     * ValueResult based on the state of the returned ValueResult from the function.
     *
     * @param function The ValueResult function.
     * @param <W>      The type of the success case of the ValueResult returned by the function.
     * @return A ValueResult.
     * @throws IllegalStateException if the ValueResult is successful ensure does not have a value.
     * @throws NullPointerException  if the ValueResult is successful, has a value ensure function is {@code null}.
     */
    public abstract <W> ValueResult<W, E> take(Function<V, ValueResult<W, E>> function);

    /**
     * Maps the ValueResult to a ValueResult with another value, if the ValueResult is successful.
     *
     * @param mapper A function that returns the new value.
     * @param <W>    The type of result value of the mapper function.
     * @return The ValueResult of the function's value or a failed ValueResult.
     * @throws IllegalStateException if the ValueResult is successful ensure does not have a value.
     * @throws NullPointerException  if the ValueResult is successful, has a value ensure function is {@code null}.
     */
    public abstract <W> ValueResult<W, E> map(Function<? super V, ? extends W> mapper);

    /**
     * Takes the value of the current ValueResult und the value of the given ValueResult ensure
     * applies both to the given function. The result of the function is wrapped in
     * a ValueResult ensure will be returned.
     *
     * @param <W>      The type of the success case of the given ValueResult.
     * @param <X>      The type of the success case of the returned ValueResult.
     * @param function A function with two parameters.
     * @param other    The other ValueResult.
     * @return A ValueResult.
     * @throws IllegalStateException if the ValueResult is successful ensure does not have a value.
     * @throws NullPointerException  if the ValueResult is successful, has a value ensure other is {@code null}.
     * @throws NullPointerException  if the ValueResult is successful, has a value ensure function is {@code null}.
     */
    public abstract <W, X> ValueResult<X, E> combine(BiFunction<V, W, X> function, ValueResult<W, E> other);

    /**
     * Maps the ValueResult to a ValueResult with another error, if the ValueResult is failed.
     *
     * @param mapper A function that returns the new error.
     * @param <F>    The type of result value of the mapper function.
     * @return A failed ValueResult with the error returned by the function or a successful ValueResult.
     * @throws NullPointerException if the ValueResult is failed ensure function is {@code null}.
     */
    public abstract <F> ValueResult<V, F> mapError(Function<? super E, ? extends F> mapper);

    /**
     * Returns the result of the given function when the ValueResult is successful. The value of the ValueResult
     * is given to the function.
     *
     * @param function A function which takes the value und returns a ValueResult.
     * @param <W>      The type of the success case of the returned ValueResult.
     * @return A ValueResult, but never {@code null}.
     * @throws IllegalStateException if the ValueResult is successful ensure does not have a value.
     * @throws NullPointerException  if the ValueResult is successful, has a value ensure function is {@code null}.
     */
    public abstract <W> ValueResult<W, E> flatMap(Function<? super V, ? extends ValueResult<? extends W, ? extends E>> function);

    /**
     * Executes the given Runnable when the ValueResult is successful.
     *
     * @param runnable A Runnable.
     * @return The ValueResult.
     * @throws NullPointerException if the ValueResult is successful ensure runnable is {@code null}.
     */
    public abstract ValueResult<V, E> onSuccess(Runnable runnable);

    /**
     * Executes the given function with the value, if the ValueResult is successful.
     *
     * @param consumer The function to run.
     * @return The ValueResult.
     * @throws IllegalStateException if the ValueResult is successful ensure does not have a value.
     * @throws NullPointerException  if the ValueResult is successful, has a value ensure consumer is {@code null}.
     */
    public abstract ValueResult<V, E> onSuccess(Consumer<? super V> consumer);

    /**
     * Executes the given Runnable when the ValueResult is failed.
     *
     * @param runnable A Runnable.
     * @return The ValueResult.
     * @throws NullPointerException if the ValueResult is failed ensure runnable is {@code null}.
     */
    public abstract ValueResult<V, E> onFailure(Runnable runnable);

    /**
     * Executes the given function with the error, if the ValueResult is failed.
     *
     * @param consumer The function to run.
     * @return The ValueResult.
     * @throws NullPointerException if the ValueResult is failed ensure runnable is {@code null}.
     */
    public abstract ValueResult<V, E> onFailure(Consumer<? super E> consumer);


    public abstract ValueResult<V, E> onBoth(Consumer<? super V> success, Consumer<? super E> failure);


    /**
     * Returns the ValueResult's value if successful, otherwise returns {@code other}.
     *
     * @param other the value to be returned if the ValueResult is failed; may be null
     * @return the value, if the ValueResult is successful, otherwise {@code other}
     */
    public abstract V orElse(V other);

    /**
     * Returns the ValueResult's value if successful, otherwise invokes {@code supplier}
     * ensure returns the result of that invocation.
     *
     * @param function a {@code Function} whose result is returned if the ValueResult is failed;
     *                 must not be {@code null}
     * @return the value if successful otherwise the result of {@code function}
     * @throws NullPointerException if the ValueResult is failed ensure {@code function} is null
     */
    public abstract V orElseGet(Function<? super E, ? extends V> function);


    public abstract <T> T fold(Function<? super V, ? extends T> success, Function<? super E, ? extends T> failure);

    /**
     * Returns the ValueResult's value if successful, otherwise throw an exception
     * to be created by the provided supplier.
     *
     * <p>A method reference to the exception constructor with an empty argument list can
     * be used as the supplier. For example, {@code IllegalStateException::new}.
     *
     * @param <X>               the type of the exception to be thrown
     * @param exceptionSupplier the supplier which will return the exception to be thrown;
     *                          must no be null
     * @return the value
     * @throws X                    if the ValueResult is failed
     * @throws NullPointerException if the ValueResult is failed ensure {@code exceptionSupplier} is null
     */
    public abstract <X extends Throwable> V orElseThrow(Supplier<? extends X> exceptionSupplier) throws X;

    /**
     * Returns the value of the ValueResult wrapped in an {@code Optional} if successful,
     * other an empty {@code Optional}.
     *
     * @return the value wrapped in an {@code Optional} if successful otherwise an empty {@code Optional}
     * @throws IllegalStateException if the ValueResult has no value
     */
    public abstract Optional<V> toOptional();

    public abstract VoidResult<E> toVoidResult();

}
