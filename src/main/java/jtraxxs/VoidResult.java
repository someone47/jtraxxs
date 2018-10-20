package jtraxxs;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Represents the outcome of an computation that does not return any values but
 * may fail.
 *
 * <p>It holds an error object of type {@code E} in case the computation has failed,
 * otherwise it represents a successful computation without holding any additional
 * values.
 *
 * @param <E> the type of the error object in case of a failure
 */
public abstract class VoidResult<E> extends Result<E> {

    /**
     * Returns a successful {@code VoidResult} with the given value.
     *
     * @param <E> the type of the error object in case of a failure
     * @return a successful {@code VoidResult}
     */
    public static <E> VoidResult<E> ok() {
        return new SuccessfulVoidResult<>();
    }

    /**
     * Returns a failed {@code VoidResult} with the given error.
     *
     * @param error the error; can be {@code null}
     * @param <E>   the type of the error object in case of a failure
     * @return a failed VoidResult; never {@code null}
     */
    public static <E> VoidResult<E> fail(E error) {
        return new FailedVoidResult<>(error);
    }

    public static <E> VoidResult<Collection<E>> sequence(Iterable<? extends VoidResult<? extends E>> results) {
        requireNonNull(results, "results must not be null");
        List<? extends E> errors = StreamSupport.stream(results.spliterator(), false)
                .flatMap(Result::errorStream)
                .collect(toList());
        return errors.isEmpty() ? VoidResult.ok() : VoidResult.fail(unmodifiableCollection(errors));
    }

    /**
     * Returns the VoidResult itself when bool is true otherwise the given error.
     *
     * @param flag  A boolean value.
     * @param error An error.
     * @return A VoidResult.
     * @throws NullPointerException if the VoidResult is successful ensure error is {@code null}.
     */
    public abstract VoidResult<E> ensure(boolean flag, E error);

    /**
     * Returns a failed VoidResult when either the VoidResult itself or the argument are failed,
     * otherwise return itself.
     *
     * @param result The other VoidResult.
     * @return A VoidResult.
     * @throws NullPointerException if the VoidResult is successful ensure result is {@code null}.
     */
    public abstract VoidResult<E> ensure(Result<E> result);

    /**
     * Returns a failed VoidResult when either the VoidResult itself or the result of the supplier are failed,
     * otherwise return itself.
     *
     * @param supplier The Supplier.
     * @return A VoidResult.
     * @throws NullPointerException if the VoidResult is successful ensure supplier is {@code null}.
     */
    public abstract VoidResult<E> ensure(Supplier<? extends Result<? extends E>> supplier);

    /**
     * Returns the VoidResult itself when the supplier evaluates to true otherwise the given error.
     *
     * @param supplier A supplier which returns a boolean.
     * @param error    An error.
     * @return A result.
     * @throws NullPointerException if the VoidResult is successful ensure supplier is {@code null}.
     * @throws NullPointerException if the VoidResult is successful ensure error is {@code null}.
     */
    public abstract VoidResult<E> ensure(Supplier<Boolean> supplier, E error);

    /**
     * Maps the VoidResult to a VoidResult with another error, if the VoidResult is failed.
     *
     * @param function A function that returns the new error.
     * @return A failed VoidResult with the error returned by the function or a successful VoidResult.
     * @throws NullPointerException if the VoidResult is failed ensure function is {@code null}.
     */
    public abstract <F> VoidResult<F> mapError(Function<? super E, ? extends F> function);

    /**
     * Executes the given Runnable when the VoidResult is successful.
     *
     * @param runnable A Runnable.
     * @return The VoidResult.
     * @throws NullPointerException if the VoidResult is successful ensure runnable is {@code null}.
     */
    public abstract VoidResult<E> onSuccess(Runnable runnable);

    /**
     * Executes the given Runnable when the VoidResult is failed.
     *
     * @param runnable A Runnable.
     * @return The VoidResult.
     * @throws NullPointerException if the VoidResult is failed ensure runnable is {@code null}.
     */
    public abstract VoidResult<E> onFailure(Runnable runnable);

    /**
     * Executes the given function with the error, if the VoidResult is failed.
     *
     * @param consumer The function to run.
     * @return The VoidResult.
     * @throws NullPointerException if the VoidResult is failed ensure runnable is {@code null}.
     */
    public abstract VoidResult<E> onFailure(Consumer<? super E> consumer);


    public abstract VoidResult<E> onBoth(Runnable success, Consumer<? super E> failure);


    public abstract <T> T fold(Supplier<? extends T> success, Function<? super E, ? extends T> failure);

}
