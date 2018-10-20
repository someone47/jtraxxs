package com.iremembr.jtraxxs;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("A successful ValueResult")
class SuccessfulValueResultTest {

    @Nested
    @DisplayName("Properties")
    class Properties {
        @Test
        @DisplayName("isSuccessful() returns true")
        void isSuccess() {
            Assertions.assertThat(ValueResult.ok("success").isSuccessful()).isTrue();
        }

        @Test
        @DisplayName("hasFailed() returns false")
        void hasFailed() {
            Assertions.assertThat(ValueResult.ok("success").hasFailed()).isFalse();
        }

        @Test
        @DisplayName("value() returns the value")
        void value() {
            Assertions.assertThat(ValueResult.ok("success").value()).isEqualTo("success");
        }

        @Test
        @DisplayName("error() throws an IllegalStateException")
        void getError() {
            Assertions.assertThatThrownBy(() -> ValueResult.ok("success").error())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Successful ValueResult has no error");
        }
    }

    @Nested
    @DisplayName("equalsContract")
    class equalsContract {
        @Test
        @DisplayName("equals() ensure hashCode() adhere to the equals contract")
        void equalsAndHashCode() {
            EqualsVerifier.forClass(SuccessfulValueResult.class).verify();
        }
    }

    @Nested
    @DisplayName("toString()")
    class toString {
        @Test
        @DisplayName("Returns a proper representation")
        void toStringMethod() {
            Assertions.assertThat(ValueResult.ok("success").toString()).isEqualTo("SuccessfulValueResult{value=success}");
        }
    }

    @Nested
    @DisplayName("errorStream()")
    class errorStream {
        @Test
        @DisplayName("Returns an empty Stream")
        void errorStreamIsEmpty() {
            Assertions.assertThat(ValueResult.ok("success").errorStream()).isEmpty();
        }
    }

    @Nested
    @DisplayName("stream()")
    class stream {
        @Test
        @DisplayName("Returns an empty Stream")
        void streamHasOneValue() {
            Assertions.assertThat(ValueResult.ok("success").stream()).containsExactly("success");
        }
    }

    @Nested
    @DisplayName("onSuccess()")
    class onSuccess {
        @Test
        @DisplayName("WHEN given a Runnable THEN the returned ValueResult is successful with the value of the base Result")
        void withRunnable(@Mock Runnable runnable) {
            RailwayAssertions.assertThat(ValueResult.ok("success").onSuccess(runnable)).isSuccessful().withValue("success");
        }

        @Test
        @DisplayName("WHEN given a Runnable THEN the Runnable will be invoked once")
        void withRunnableInvoke(@Mock Runnable runnable) {
            ValueResult.ok("success").onSuccess(runnable);
            verify(runnable, only()).run();
        }

        @Test
        @DisplayName("WHEN given a Consumer THEN the returned ValueResult is successful with the value of the base Result")
        void withConsumer(@Mock Consumer<String> consumer) {
            RailwayAssertions.assertThat(ValueResult.ok("success").onSuccess(consumer)).isSuccessful().withValue("success");
        }

        @Test
        @DisplayName("WHEN given a Consumer THEN the Consumer will be invoked once")
        void withConsumerInvoked(@Mock Consumer<String> consumer) {
            ValueResult.ok("success").onSuccess(consumer);
            verify(consumer, only()).accept("success");
        }
    }

    @Nested
    @DisplayName("onFailure()")
    class onFailure {
        @Test
        @DisplayName("WHEN given a Runnable THEN the returned ValueResult is successful with the value of the base Result")
        void withRunnable(@Mock Runnable runnable) {
            RailwayAssertions.assertThat(ValueResult.ok("success").onFailure(runnable)).isSuccessful().withValue("success");
        }

        @Test
        @DisplayName("WHEN given a Runnable THEN the Runnable will not be invoked ensure the base ValueResult will be passed through")
        void withRunnableInvoked(@Mock Runnable runnable) {
            ValueResult.ok("success").onFailure(runnable);
            verify(runnable, never()).run();
        }

        @Test
        @DisplayName("WHEN given a Consumer THEN the returned ValueResult is successful with the value of the base Result")
        void withConsumer(@Mock Consumer<String> consumer) {
            RailwayAssertions.assertThat(ValueResult.<String, String>ok("success").onFailure(consumer)).isSuccessful().withValue("success");
        }

        @Test
        @DisplayName("WHEN given a Consumer THEN the Consumer will not be invoked")
        void withConsumerInvoked(@Mock Consumer<String> consumer) {
            ValueResult.<String, String>ok("success").onFailure(consumer);
            verify(consumer, never()).accept(any());
        }
    }

    @Nested
    @DisplayName("onBoth()")
    class ifSuccessOrFailure {
        @Test
        @DisplayName("Calls success Consumer")
        void returnsSuccess(@Mock Consumer<String> success, @Mock Consumer<String> failure) {
            ValueResult.<String, String>ok("success").onBoth(success, failure);
            verify(success, only()).accept("success");
            verify(failure, never()).accept(anyString());
        }
    }

    @Nested
    @DisplayName("ensure()")
    class ensure {
        @Test
        @DisplayName("WHEN given true THEN the returned ValueResult is successful with the value of the base Result")
        void withTrue() {
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(true, "error")).isSuccessful().withValue("success");
        }

        @Test
        @DisplayName("WHEN given false THEN a failed ValueResult with the given error will be returned")
        void withFalse() {
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(false, "error")).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a Supplier returning true THEN the returned ValueResult is successful with the value of the base Result")
        void withSupplierReturningTrue(@Mock Supplier<Boolean> supplier) {
            when(supplier.get()).thenReturn(true);
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(supplier, "error")).isSuccessful().withValue("success");
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Supplier returning false THEN the returned ValueResult has failed with the given error")
        void withSupplierReturningFalse(@Mock Supplier<Boolean> supplier) {
            when(supplier.get()).thenReturn(false);
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(supplier, "error")).hasFailed().withError("error");
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Predicate returning true THEN the returned ValueResult is successful with the value of the base Result")
        void withPredicateReturningTrue(@Mock Predicate<String> predicate) {
            when(predicate.test("success")).thenReturn(true);
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(predicate, "error")).isSuccessful().withValue("success");
            verify(predicate, only()).test("success");
        }

        @Test
        @DisplayName("WHEN given a Predicate returning false THEN the returned ValueResult has failed with the given error")
        void withPredicateReturningFalse(@Mock Predicate<String> predicate) {
            when(predicate.test("success")).thenReturn(false);
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(predicate, "error")).hasFailed().withError("error");
            verify(predicate, only()).test("success");
        }

        @Test
        @DisplayName("WHEN given a successful VoidResult THEN the returned ValueResult is successful")
        void withSuccessfulVoidResult() {
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(VoidResult.ok())).isSuccessful().withValue("success");
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a successful VoidResult THEN the returned ValueResult is successful")
        void withSuccessfulVoidResultSupplier(@Mock Supplier<VoidResult<String>> supplier) {
            when(supplier.get()).thenReturn(VoidResult.ok());
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(supplier)).isSuccessful().withValue("success");
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Function returning a successful VoidResult THEN the returned ValueResult is successful")
        void withSuccessfulVoidResultFunction(@Mock Function<String, VoidResult<String>> function) {
            when(function.apply("success")).thenReturn(VoidResult.ok());
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(function)).isSuccessful().withValue("success");
            verify(function, only()).apply(any());
        }

        @Test
        @DisplayName("WHEN given a successful ValueResult THEN the returned ValueResult is successful")
        void withSuccessfulValueResult() {
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(ValueResult.ok("ok"))).isSuccessful().withValue("success");
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a successful ValueResult THEN the returned ValueResult is successful")
        void withSuccessfulValueResultSupplier(@Mock Supplier<ValueResult<String, String>> supplier) {
            when(supplier.get()).thenReturn(ValueResult.ok("ok"));
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(supplier)).isSuccessful().withValue("success");
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Function returning a successful ValueResult THEN the returned ValueResult is successful")
        void withSuccessfulValueResultFunction(@Mock Function<String, ValueResult<String, String>> function) {
            when(function.apply("success")).thenReturn(ValueResult.ok("ok"));
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(function)).isSuccessful().withValue("success");
            verify(function, only()).apply(any());
        }

        @Test
        @DisplayName("WHEN given a failed VoidResult THEN the returned ValueResult has failed with the error of the given VoidResult")
        void withFailedVoidResult() {
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(VoidResult.fail("error"))).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a failed VoidResult THEN the returned ValueResult has failed with the error of the given VoidResult")
        void withFailedVoidResultSupplier(@Mock Supplier<VoidResult<String>> supplier) {
            when(supplier.get()).thenReturn(VoidResult.fail("error"));
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(supplier)).hasFailed().withError("error");
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Function returning a failed VoidResult THEN the returned ValueResult has failed with the error of the given VoidResult")
        void withFailedVoidResultFunction(@Mock Function<String, VoidResult<String>> function) {
            when(function.apply("success")).thenReturn(VoidResult.fail("error"));
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(function)).hasFailed().withError("error");
            verify(function, only()).apply(any());
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult THEN the returned ValueResult has failed with the error of the given ValueResult")
        void withFailedValueResult() {
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(ValueResult.fail("error"))).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a failed ValueResult THEN the returned ValueResult has failed with the error of the given ValueResult")
        void withFailedValueResultSupplier(@Mock Supplier<ValueResult<String, String>> supplier) {
            when(supplier.get()).thenReturn(ValueResult.fail("error"));
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(supplier)).hasFailed().withError("error");
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Function returning a failed ValueResult THEN the returned ValueResult has failed with the error of the given ValueResult")
        void withFailedValueResultFunction(@Mock Function<String, ValueResult<String, String>> function) {
            when(function.apply("success")).thenReturn(ValueResult.fail("error"));
            RailwayAssertions.assertThat(ValueResult.ok("success").ensure(function)).hasFailed().withError("error");
            verify(function, only()).apply(any());
        }
    }

    @Nested
    @DisplayName("take()")
    class take {
        @Test
        @DisplayName("WHEN given a successful ValueResult THEN the returned ValueResult is successful with the value of the given Result")
        void withSuccessfulResult() {
            RailwayAssertions.assertThat(ValueResult.ok("success").take(ValueResult.ok("ok"))).isSuccessful().withValue("ok");
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult THEN the returned ValueResult has failed with the error of the given Result")
        void withFailedResult() {
            RailwayAssertions.assertThat(ValueResult.ok("success").take(ValueResult.fail("error"))).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a successful ValueResult THEN the returned ValueResult is successful with the value of the given Result")
        void withSupplierReturningSuccessfulResult(@Mock Supplier<ValueResult<String, String>> supplier) {
            when(supplier.get()).thenReturn(ValueResult.ok("ok"));
            RailwayAssertions.assertThat(ValueResult.<String, String>ok("success").take(supplier)).isSuccessful().withValue("ok");
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a failed ValueResult THEN the returned ValueResult has failed with the error of the given Result")
        void withSupplierReturningFailedResult(@Mock Supplier<ValueResult<String, String>> supplier) {
            when(supplier.get()).thenReturn(ValueResult.fail("error"));
            RailwayAssertions.assertThat(ValueResult.<String, String>ok("success").take(supplier)).hasFailed().withError("error");
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Function returning a successful ValueResult THEN the returned ValueResult is successful with the value of the given Result")
        void withFunctionReturningSuccessfulResult(@Mock Function<String, ValueResult<String, String>> function) {
            when(function.apply("success")).thenReturn(ValueResult.ok("ok"));
            RailwayAssertions.assertThat(ValueResult.<String, String>ok("success").take(function)).isSuccessful().withValue("ok");
            verify(function, only()).apply("success");
        }
    }

    @Nested
    @DisplayName("map()")
    class map {
        @Test
        @DisplayName("WHEN given a mapper Function THEN the returned ValueResult is successful with the value returned by the mapper Function")
        void mapValue(@Mock Function<String, String> mapper) {
            when(mapper.apply("success")).thenReturn("mapped");
            RailwayAssertions.assertThat(ValueResult.<String, String>ok("success").map(mapper)).isSuccessful().withValue("mapped");
            verify(mapper, only()).apply("success");
        }
    }

    @Nested
    @DisplayName("mapError()")
    class mapError {
        @Test
        @DisplayName("WHEN given a mapper Function THEN the returned ValueResult is successful with the value of the base Result")
        void mapErrorReturnsSuccessfulValueResult(@Mock Function<String, String> mapper) {
            RailwayAssertions.assertThat(ValueResult.<String, String>ok("success").mapError(mapper)).isSuccessful().withValue("success");
        }

        @Test
        @DisplayName("WHEN given a mapper Function THEN the mapper function is not invoked")
        void mapFunctionNotInvoked(@Mock Function<String, String> mapper) {
            ValueResult.<String, String>ok("success").mapError(mapper);
            verify(mapper, never()).apply(any());
        }
    }

    @Nested
    @DisplayName("flatMap()")
    class flatMap {
        @Test
        @DisplayName("WHEN given a mapper Function returning a successful ValueResult THEN this ValueResult will be returned")
        void flatMapReturningSuccessfulValueResult(@Mock Function<String, ValueResult<String, String>> mapper) {
            when(mapper.apply("success")).thenReturn(ValueResult.ok("flat mapped"));
            RailwayAssertions.assertThat(ValueResult.<String, String>ok("success").flatMap(mapper)).isSuccessful().withValue("flat mapped");
            verify(mapper, only()).apply("success");
        }

        @Test
        @DisplayName("WHEN given a mapper Function returning a failed ValueResult THEN this ValueResult will be returned")
        void flatMapReturningFailedValueResult(@Mock Function<String, ValueResult<String, String>> mapper) {
            when(mapper.apply("success")).thenReturn(ValueResult.fail("flat mapped"));
            RailwayAssertions.assertThat(ValueResult.<String, String>ok("success").flatMap(mapper)).hasFailed().withError("flat mapped");
            verify(mapper, only()).apply("success");
        }
    }

    @Nested
    @DisplayName("combine()")
    class combine {
        @Test
        @DisplayName("WHEN given a successful ValueResult ensure a Function THEN the returned ValueResult is successful with a value returned by the Function")
        void withSuccessfulResult(@Mock BiFunction<String, String, String> function) {
            when(function.apply("success", "ok")).thenReturn("combined");
            RailwayAssertions.assertThat(ValueResult.ok("success").combine(function, ValueResult.ok("ok"))).isSuccessful().withValue("combined");
            verify(function, only()).apply("success", "ok");
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult ensure a Function THEN the returned ValueResult has failed with the error of the base Result")
        void withFailedResult(@Mock BiFunction<String, String, String> function) {
            RailwayAssertions.assertThat(ValueResult.ok("success").combine(function, ValueResult.fail("error"))).hasFailed().withError("error");
            verify(function, never()).apply(any(), any());
        }
    }

    @Nested
    @DisplayName("orElse()")
    class orElse {
        @Test
        @DisplayName("Returns the value")
        void returnsValue() {
            Assertions.assertThat(ValueResult.ok("success").orElse("fail")).isEqualTo("success");
        }
    }

    @Nested
    @DisplayName("orElseGet()")
    class orElseGet {
        @Test
        @DisplayName("Returns the value")
        void returnsValue(@Mock Function<String, String> function) {
            Assertions.assertThat(ValueResult.<String, String>ok("success").orElseGet(function)).isEqualTo("success");
            verify(function, never()).apply(any());
        }
    }

    @Nested
    @DisplayName("orElseThrow()")
    class orElseThrow {
        @Test
        @DisplayName("Throws exception produced by the the given supplier")
        void throwsExceptionFromSupplier(@Mock Supplier<RuntimeException> supplier) {
            Assertions.assertThat(ValueResult.ok("success").orElseThrow(supplier)).isEqualTo("success");
            verify(supplier, never()).get();
        }
    }

    @Nested
    @DisplayName("toOptional()")
    class toOptional {
        @Test
        @DisplayName("Returns value wrapped in an Optional")
        void returnsOptionalWithValue() {
            //noinspection OptionalGetWithoutIsPresent
            Assertions.assertThat(ValueResult.ok("success").toOptional().get()).isEqualTo("success");
        }
    }

    @Nested
    @DisplayName("fold()")
    class fold {
        @Test
        @DisplayName("Returns value mapped by the success Function")
        void returnsSuccess(@Mock Function<String, String> success, @Mock Function<String, String> failure) {
            when(success.apply("success")).thenReturn("mapped");
            Assertions.assertThat(ValueResult.<String, String>ok("success").fold(success, failure)).isEqualTo("mapped");
            verify(success, only()).apply("success");
            verify(failure, never()).apply(anyString());
        }
    }
}
