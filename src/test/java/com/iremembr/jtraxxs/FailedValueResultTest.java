package com.iremembr.jtraxxs;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.function.*;

import static com.iremembr.jtraxxs.RailwayAssertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("A failed ValueResult")
class FailedValueResultTest {

    @Nested
    @DisplayName("castValue()")
    class castValue {
        @Test
        @DisplayName("WHEN given a superclass of the value type THEN castValue will return the ValueResult with the adjusted type")
        void validCast() {
            ValueResult<String, Message> success = ValueResult.fail(Message.INSTANCE);
            ValueResult<CharSequence, Message> result = success.castValue(CharSequence.class);
            assertThat(result).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a class which is not a superclass of the value type THEN castValue will return the ValueResult with the adjusted type")
        void invalidCast() {
            ValueResult<String, Message> success = ValueResult.fail(Message.INSTANCE);
            ValueResult<BigDecimal, Message> result = success.castValue(BigDecimal.class);
            assertThat(result).hasFailed().withError(Message.INSTANCE);
        }
    }

    @Nested
    @DisplayName("castError()")
    class castError {
        @Test
        @DisplayName("WHEN given a superclass of the error type THEN castError will return the ValueResult with the adjusted type")
        void validCast() {
            ValueResult<String, SubMessage> success = ValueResult.fail(SubMessage.INSTANCE);
            ValueResult<String, Message> result = success.castError(Message.class);
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a class which is not a superclass of the error type THEN castError will throw an IllegalArgumentException")
        void invalidCast() {
            ValueResult<String, Message> success = ValueResult.fail(Message.INSTANCE);
            Throwable thrown = catchThrowable(() -> {
                success.castError(BigDecimal.class);
            });
            assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Can not cast the error to the given type");
        }
    }

    @Nested
    @DisplayName("Properties")
    class properties {
        @Test
        @DisplayName("isSuccessful() returns false")
        void isSuccess() {
            Assertions.assertThat(ValueResult.fail("error").isSuccessful()).isFalse();
        }

        @Test
        @DisplayName("hasFailed() returns true")
        void isFailure() {
            Assertions.assertThat(ValueResult.fail("error").hasFailed()).isTrue();
        }

        @Test
        @DisplayName("value() throws an IllegalStateException")
        void getValue() {
            Assertions.assertThatThrownBy(() -> ValueResult.fail("error").value())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Failed ValueResult has no value");
        }

        @Test
        @DisplayName("error() returns the error")
        void getError() {
            Assertions.assertThat(ValueResult.fail("error").error()).isEqualTo("error");
        }
    }

    @Nested
    @DisplayName("equalsContract")
    class equalsContract {
        @Test
        @DisplayName("equals() ensure hashCode() adhere to the equals contract")
        void equalsAndHashCode() {
            EqualsVerifier.forClass(FailedValueResult.class).verify();
        }
    }

    @Nested
    @DisplayName("toString()")
    class object {
        @Test
        @DisplayName("Returns a proper representation")
        void toStringMethod() {
            Assertions.assertThat(ValueResult.fail("error").toString()).isEqualTo("FailedValueResult{error=error}");
        }
    }

    @Nested
    @DisplayName("errorStream()")
    class errorStream {
        @Test
        @DisplayName("Returns an Stream with the error")
        void errorStreamHasOneError() {
            Assertions.assertThat(ValueResult.fail("error").errorStream()).containsExactly("error");
        }
    }

    @Nested
    @DisplayName("stream()")
    class stream {
        @Test
        @DisplayName("Returns an empty Stream")
        void streamIsEmpty() {
            Assertions.assertThat(ValueResult.fail("error").stream()).isEmpty();
        }
    }

    @Nested
    @DisplayName("onSuccess()")
    class onSuccess {
        @Test
        @DisplayName("WHEN given a Runnable THEN the Runnable will not be invoked AND the base ValueResult will be passed through")
        void withRunnable(@Mock Runnable runnable) {
            RailwayAssertions.assertThat(ValueResult.fail("error").onSuccess(runnable)).hasFailed().withError("error");
            verify(runnable, never()).run();
        }

        @Test
        @DisplayName("WHEN given a Consumer THEN the Consumer will not be invoked AND the base ValueResult will be passed through")
        void withConsumer(@Mock Consumer<String> consumer) {
            RailwayAssertions.assertThat(ValueResult.<String, String>fail("error").onSuccess(consumer)).hasFailed().withError("error");
            verify(consumer, never()).accept(any());
        }
    }

    @Nested
    @DisplayName("onFailure()")
    class onFailure {
        @Test
        @DisplayName("WHEN given a Runnable THEN the Runnable will be invoked once AND the base ValueResult will be passed through")
        void withRunnable(@Mock Runnable runnable) {
            RailwayAssertions.assertThat(ValueResult.fail("error").onFailure(runnable)).hasFailed().withError("error");
            verify(runnable, only()).run();
        }

        @Test
        @DisplayName("WHEN given a Consumer THEN the Consumer will be invoked once AND the base ValueResult will be passed through")
        void withConsumer(@Mock Consumer<String> consumer) {
            RailwayAssertions.assertThat(ValueResult.fail("error").onFailure(consumer)).hasFailed().withError("error");
            verify(consumer, only()).accept("error");
        }
    }

    @Nested
    @DisplayName("onBoth()")
    class onBoth {
        @Test
        @DisplayName("Calls failure Consumer")
        void returnsSuccess(@Mock Consumer<String> success, @Mock Consumer<String> failure) {
            RailwayAssertions.assertThat(ValueResult.<String, String>fail("error").onBoth(success, failure)).hasFailed().withError("error");
            verify(success, never()).accept(anyString());
            verify(failure, only()).accept("error");
        }
    }

    @Nested
    @DisplayName("ensure()")
    class ensure {
        @Test
        @DisplayName("WHEN given true THEN the ValueResult will be passed through")
        void withTrue() {
            RailwayAssertions.assertThat(ValueResult.fail("error").ensure(true, "error")).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given false THEN the ValueResult will be passed through")
        void withFalse() {
            RailwayAssertions.assertThat(ValueResult.fail("error").ensure(false, "error")).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a Supplier THEN the Supplier will not be invoked AND the ValueResult will be passed through")
        void withSupplier(@Mock Supplier<Boolean> supplier) {
            RailwayAssertions.assertThat(ValueResult.fail("error").ensure(supplier, "error")).hasFailed().withError("error");
            verify(supplier, never()).get();
        }

        @Test
        @DisplayName("WHEN given a predicate THEN the Supplier will not be invoked AND the ValueResult will be passed through")
        void withPredicate(@Mock Predicate<String> predicate) {
            RailwayAssertions.assertThat(ValueResult.<String, String>fail("error").ensure(predicate, "error")).hasFailed().withError("error");
            verify(predicate, never()).test(any());
        }

        @Test
        @DisplayName("WHEN given a successful VoidResult THEN the returned ValueResult has failed with the error of base Result")
        void successfulVoidResult() {
            RailwayAssertions.assertThat(ValueResult.fail("error").ensure(VoidResult.ok())).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a successful ValueResult THEN the returned ValueResult has failed with the error of base Result")
        void successfulValueResult() {
            RailwayAssertions.assertThat(ValueResult.fail("error").ensure(ValueResult.ok("ok"))).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a failed VoidResult THEN the returned ValueResult has failed with the error of the base Result")
        void failedVoidResult() {
            RailwayAssertions.assertThat(ValueResult.fail("error").ensure(VoidResult.fail("bad"))).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult THEN the returned ValueResult has failed with the error of the base Result")
        void failedValueResult() {
            RailwayAssertions.assertThat(ValueResult.fail("error").ensure(ValueResult.fail("bad"))).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a supplier THEN the returned ValueResult has failed with the error of base Result")
        void voidResultSupplier(@Mock Supplier<VoidResult<String>> supplier) {
            RailwayAssertions.assertThat(ValueResult.fail("error").ensure(supplier)).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a supplier THEN the supplier is not invoked")
        void voidResultSupplierNeverCalled(@Mock Supplier<VoidResult<String>> supplier) {
            ValueResult.fail("error").ensure(supplier);
            verify(supplier, never()).get();
        }

        @Test
        @DisplayName("WHEN given a Function THEN the Function is not invoked")
        void voidResultSupplierNeverCalled(@Mock Function<String, VoidResult<String>> function) {
            ValueResult.<String, String>fail("error").ensure(function);
            verify(function, never()).apply(anyString());
        }
    }

    @Nested
    @DisplayName("take()")
    class take {
        @Test
        @DisplayName("WHEN given a successful ValueResult THEN a failed ValueResult will be returned with the error of the base result")
        void withSuccessfulResult() {
            RailwayAssertions.assertThat(ValueResult.fail("error").take(ValueResult.ok("ok"))).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult THEN a failed ValueResult will be returned with the error of the base result")
        void withFailedResult() {
            RailwayAssertions.assertThat(ValueResult.fail("error").take(ValueResult.fail("bad"))).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a Supplier THEN the Supplier will not be invoked AND a failed ValueResult will be returned with the error of the base result")
        void withSupplier(@Mock Supplier<ValueResult<String, String>> supplier) {
            RailwayAssertions.assertThat(ValueResult.fail("error").take(supplier)).hasFailed().withError("error");
            verify(supplier, never()).get();
        }

        @Test
        @DisplayName("WHEN given a Function THEN the Function will not be invoked AND a failed ValueResult will be returned with the error of the base result")
        void withFunction(@Mock Function<String, ValueResult<String, String>> function) {
            RailwayAssertions.assertThat(ValueResult.<String, String>fail("error").take(function)).hasFailed().withError("error");
            verify(function, never()).apply(any());
        }
    }

    @Nested
    @DisplayName("map()")
    class map {
        @Test
        @DisplayName("WHEN given a Function THEN the Function will not be invoked AND a failed ValueResult will be returned with the error of the base result")
        void withFunction(@Mock Function<String, String> function) {
            RailwayAssertions.assertThat(ValueResult.<String, String>fail("error").map(function)).hasFailed().withError("error");
            verify(function, never()).apply(any());
        }
    }

    @Nested
    @DisplayName("mapError()")
    class mapError {
        @Test
        @DisplayName("WHEN given a Function THEN the Function will be invoked AND its return value will be the error of the returned failed ValueResult")
        void withFunction(@Mock Function<String, String> function) {
            when(function.apply("error")).thenReturn("failedResult");
            RailwayAssertions.assertThat(ValueResult.fail("error").mapError(function)).hasFailed().withError("failedResult");
            verify(function, only()).apply("error");
        }
    }

    @Nested
    @DisplayName("flatMap()")
    class flatMap {
        @Test
        @DisplayName("WHEN given a Function THEN the Function will not be invoked AND a failed ValueResult will be returned with the error of the base result")
        void withFunction(@Mock Function<String, ValueResult<String, String>> function) {
            RailwayAssertions.assertThat(ValueResult.<String, String>fail("error").flatMap(function)).hasFailed().withError("error");
            verify(function, never()).apply(any());
        }
    }

    @Nested
    @DisplayName("combine()")
    class combine {
        @Test
        @DisplayName("WHEN given a successful ValueResult AND a Function THEN the Function will not be invoked AND a failed ValueResult will be returned with the error of the base result")
        void withSuccessfulResult(@Mock BiFunction<String, String, String> function) {
            RailwayAssertions.assertThat(ValueResult.<String, String>fail("error").combine(function, ValueResult.ok("ok"))).hasFailed().withError("error");
            verify(function, never()).apply(any(), any());
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult AND a Function THEN the Function will not be invoked AND a failed ValueResult will be returned with the error of the base result")
        void withFailedResult(@Mock BiFunction<String, String, String> function) {
            RailwayAssertions.assertThat(ValueResult.<String, String>fail("error").combine(function, ValueResult.fail("bad"))).hasFailed().withError("error");
            verify(function, never()).apply(any(), any());
        }
    }

    @Nested
    @DisplayName("orElse()")
    class orElse {
        @Test
        @DisplayName("Returns the argument")
        void returnsArgument() {
            Assertions.assertThat(ValueResult.fail("error").orElse("fail")).isEqualTo("fail");
        }
    }

    @Nested
    @DisplayName("orElseGet()")
    class orElseGet {
        @Test
        @DisplayName("Returns the result of the given function")
        void returnsFunctionResult(@Mock Function<String, String> function) {
            when(function.apply("error")).thenReturn("from function");
            Assertions.assertThat(ValueResult.fail("error").orElseGet(function)).isEqualTo("from function");
        }
    }

    @Nested
    @DisplayName("orElseThrow()")
    class orElseThrow {
        @Test
        @DisplayName("Throws exception produced by the the given supplier")
        void throwsExceptionFromSupplier(@Mock Supplier<RuntimeException> supplier) {
            when(supplier.get()).thenReturn(new RuntimeException("from supplier"));
            Assertions.assertThatThrownBy(() -> ValueResult.fail("error").orElseThrow(supplier))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("from supplier");
        }
    }

    @Nested
    @DisplayName("fold()")
    class fold {
        @Test
        @DisplayName("Returns value mapped by the error Function")
        void returnsSuccess(@Mock Function<String, String> success, @Mock Function<String, String> failure) {
            when(failure.apply("error")).thenReturn("mapped");
            Assertions.assertThat(ValueResult.<String, String>fail("error").fold(success, failure)).isEqualTo("mapped");
            verify(success, never()).apply(anyString());
            verify(failure, only()).apply("error");
        }
    }

    @Nested
    @DisplayName("toOptional()")
    class toOptional {
        @Test
        @DisplayName("Returns None")
        void returnsNone() {
            Assertions.assertThat(ValueResult.fail("error").toOptional().isPresent()).isFalse();
        }
    }

    @Nested
    @DisplayName("toVoidResult()")
    class toVoidResult {
        @Test
        @DisplayName("Returns a failed VoidResult")
        void returnsFailedVoidResult() {
            ValueResult<String, Message> error = ValueResult.fail(Message.INSTANCE);
            VoidResult<Message> result = error.toVoidResult();
            RailwayAssertions.assertThat(result).hasFailed().withError(Message.INSTANCE);
        }
    }
}
