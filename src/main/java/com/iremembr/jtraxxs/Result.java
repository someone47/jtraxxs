package com.iremembr.jtraxxs;

import java.util.stream.Stream;

/**
 * Result of a computation that can be successful or it may have failed.
 *
 * <p>
 *
 * @param <E> the type of the error object in case of a failure
 */
public abstract class Result<E> {

    /**
     * Returns a string which represents the Result.
     *
     * @return the string representation of the Result.
     */
    @Override
    public abstract String toString();

    /**
     * Returns {@code true} if the Result is successful.
     *
     * @return {@code true} if the Result is successful.
     */
    public abstract boolean isSuccessful();

    /**
     * Returns {@code true} if the Result has failed.
     *
     * @return {@code true} if the Result has failed.
     */
    public boolean hasFailed() {
        return !isSuccessful();
    }

    /**
     * Returns the error of the Result.
     *
     * @return The error.
     * @throws IllegalStateException if Result is successful.
     */
    public abstract E error();

    /**
     * Returns an empty {@code Stream} when the Result is successful,
     * otherwise a sequential {@link Stream} containing only one error
     * object.
     *
     * <p>This method can be used to transform a {@code Stream} of Result
     * objects to a {@code Stream} of value elements:
     * <pre>{@code
     *     Stream<Result<E>> someResults = ...
     *     Stream<E> s = someResults.flatMap(Result::stream)
     * }</pre>
     *
     * @return the error as a {@code Stream}
     */
    public abstract Stream<E> errorStream();

}
