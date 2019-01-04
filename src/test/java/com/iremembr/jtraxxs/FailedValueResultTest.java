package com.iremembr.jtraxxs;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("A failed ValueResult")
class FailedValueResultTest {

    private ValueResult<Value, Message> base;
    private ValueResult<SubValue, SubMessage> success;
    private ValueResult<SubValue, SubMessage> failed;
    private VoidResult<SubMessage> ok;
    private VoidResult<SubMessage> bad;

    @BeforeEach
    void setUp() {
        base = ValueResult.fail(Message.INSTANCE);
        success = ValueResult.ok(SubValue.INSTANCE);
        failed = ValueResult.fail(SubMessage.INSTANCE);
        ok = VoidResult.ok();
        bad = VoidResult.fail(SubMessage.INSTANCE);
    }

    @Nested
    @DisplayName("castValue()")
    class castValue {
        @Test
        @DisplayName("WHEN given a superclass of the value type THEN castValue will return the ValueResult with the adjusted type")
        void validCast() {
            ValueResult<CharSequence, Message> result = base.castValue(CharSequence.class);
            assertThat(result).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a class which is not a superclass of the value type THEN castValue will return the ValueResult with the adjusted type")
        void invalidCast() {
            ValueResult<BigDecimal, Message> result = base.castValue(BigDecimal.class);
            assertThat(result).hasFailed().withError(Message.INSTANCE);
        }
    }

    @Nested
    @DisplayName("castError()")
    class castError {
        @Test
        @DisplayName("WHEN given a superclass of the error type THEN castError will return the ValueResult with the adjusted type")
        void validCast() {
            ValueResult<Value, Message> result = base.castError(Message.class);
            assertThat(result).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a class which is not a superclass of the error type THEN castError will throw an IllegalArgumentException")
        void invalidCast() {
            Throwable thrown = catchThrowable(() -> base.castError(BigDecimal.class));
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
            assertThat(base.isSuccessful()).isFalse();
        }

        @Test
        @DisplayName("hasFailed() returns true")
        void isFailure() {
            assertThat(base.hasFailed()).isTrue();
        }

        @Test
        @DisplayName("value() throws an IllegalStateException")
        void getValue() {
            // When
            Throwable thrown = catchThrowable(() -> ValueResult.fail("error").value());

            // Then
            assertThat(thrown)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Failed ValueResult has no value");
        }

        @Test
        @DisplayName("error() returns the error")
        void getError() {
            assertThat(base.error()).isSameAs(Message.INSTANCE);
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
            assertThat(base.toString()).isEqualTo("FailedValueResult{error=Message}");
        }
    }

    @Nested
    @DisplayName("errorStream()")
    class errorStream {
        @Test
        @DisplayName("Returns an Stream with the error")
        void errorStreamHasOneError() {
            assertThat(base.errorStream()).containsExactly(Message.INSTANCE);
        }
    }

    @Nested
    @DisplayName("stream()")
    class stream {
        @Test
        @DisplayName("Returns an empty Stream")
        void streamIsEmpty() {
            assertThat(base.stream()).isEmpty();
        }
    }

    @Nested
    @DisplayName("onSuccess()")
    class onSuccess {
        @Test
        @DisplayName("WHEN given a Runnable THEN the Runnable will not be invoked AND the base ValueResult will be passed through")
        void withRunnable(@Mock Runnable runnable) {
            assertThat(base.onSuccess(runnable)).hasFailed().withError(Message.INSTANCE);
            verify(runnable, never()).run();
        }

        @Test
        @DisplayName("WHEN given a Consumer THEN the Consumer will not be invoked AND the base ValueResult will be passed through")
        void withConsumer(@Mock Consumer<ParentValue> consumer) {
            assertThat(base.onSuccess(consumer)).hasFailed().withError(Message.INSTANCE);
            verify(consumer, never()).accept(any());
        }
    }

    @Nested
    @DisplayName("onFailure()")
    class onFailure {
        @Test
        @DisplayName("WHEN given a Runnable THEN the Runnable will be invoked once AND the base ValueResult will be passed through")
        void withRunnable(@Mock Runnable runnable) {
            assertThat(base.onFailure(runnable)).hasFailed().withError(Message.INSTANCE);
            verify(runnable, only()).run();
        }

        @Test
        @DisplayName("WHEN given a Consumer THEN the Consumer will be invoked once AND the base ValueResult will be passed through")
        void withConsumer(@Mock Consumer<ParentMessage> consumer) {
            assertThat(base.onFailure(consumer)).hasFailed().withError(Message.INSTANCE);
            verify(consumer, only()).accept(Message.INSTANCE);
        }
    }

    @Nested
    @DisplayName("onBoth()")
    class onBoth {
        @Test
        @DisplayName("Calls failure Consumer")
        void returnsSuccess(@Mock Consumer<ParentValue> success, @Mock Consumer<ParentMessage> failure) {
            assertThat(base.onBoth(success, failure)).hasFailed().withError(Message.INSTANCE);
            verify(success, never()).accept(any());
            verify(failure, only()).accept(Message.INSTANCE);
        }
    }

    @Nested
    @DisplayName("ensure()")
    class ensure {
        @Test
        @DisplayName("WHEN given true THEN the ValueResult will be passed through")
        void withTrue() {
            assertThat(base.ensure(true, SubMessage.INSTANCE)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given false THEN the ValueResult will be passed through")
        void withFalse() {
            assertThat(base.ensure(false, SubMessage.INSTANCE)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Supplier THEN the Supplier will not be invoked AND the ValueResult will be passed through")
        void withSupplier(@Mock Supplier<Boolean> supplier) {
            assertThat(base.ensure(supplier, SubMessage.INSTANCE)).hasFailed().withError(Message.INSTANCE);
            verify(supplier, never()).get();
        }

        @Test
        @DisplayName("WHEN given a predicate THEN the Supplier will not be invoked AND the ValueResult will be passed through")
        void withPredicate(@Mock Predicate<ParentValue> predicate) {
            assertThat(base.ensure(predicate, SubMessage.INSTANCE)).hasFailed().withError(Message.INSTANCE);
            verify(predicate, never()).test(any());
        }

        @Test
        @DisplayName("WHEN given a successful VoidResult THEN the returned ValueResult has failed with the error of base Result")
        void successfulVoidResult() {
            assertThat(base.ensure(ok)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a successful ValueResult THEN the returned ValueResult has failed with the error of base Result")
        void successfulValueResult() {
            assertThat(base.ensure(success)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a failed VoidResult THEN the returned ValueResult has failed with the error of the base Result")
        void failedVoidResult() {
            assertThat(base.ensure(bad)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult THEN the returned ValueResult has failed with the error of the base Result")
        void failedValueResult() {
            assertThat(base.ensure(failed)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a supplier THEN the returned ValueResult has failed with the error of base Result")
        void voidResultSupplier(@Mock Supplier<VoidResult<SubMessage>> supplier) {
            assertThat(base.ensure(supplier)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a supplier THEN the supplier is not invoked")
        void voidResultSupplierNeverCalled(@Mock Supplier<VoidResult<SubMessage>> supplier) {
            base.ensure(supplier);
            verify(supplier, never()).get();
        }

        @Test
        @DisplayName("WHEN given a Function THEN the Function is not invoked")
        void voidResultSupplierNeverCalled(@Mock Function<ParentValue, VoidResult<SubMessage>> function) {
            base.ensure(function);
            verify(function, never()).apply(any());
        }
    }

    @Nested
    @DisplayName("take()")
    class take {
        @Test
        @DisplayName("WHEN given a successful ValueResult THEN a failed ValueResult will be returned with the error of the base result")
        void withSuccessfulResult() {
            assertThat(base.take(success)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult THEN a failed ValueResult will be returned with the error of the base result")
        void withFailedResult() {
            assertThat(base.take(failed)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Supplier THEN the Supplier will not be invoked AND a failed ValueResult will be returned with the error of the base result")
        void withSupplier(@Mock Supplier<ValueResult<String, SubMessage>> supplier) {
            assertThat(base.take(supplier)).hasFailed().withError(Message.INSTANCE);
            verify(supplier, never()).get();
        }

        @Test
        @DisplayName("WHEN given a Function THEN the Function will not be invoked AND a failed ValueResult will be returned with the error of the base result")
        void withFunction(@Mock Function<ParentValue, ValueResult<SubValue, SubMessage>> function) {
            assertThat(base.take(function)).hasFailed().withError(Message.INSTANCE);
            verify(function, never()).apply(any());
        }
    }

    @Nested
    @DisplayName("map()")
    class map {
        @Test
        @DisplayName("WHEN given a Function THEN the Function will not be invoked AND a failed ValueResult will be returned with the error of the base result")
        void withFunction(@Mock Function<ParentValue, SubValue> function) {
            assertThat(base.map(function)).hasFailed().withError(Message.INSTANCE);
            verify(function, never()).apply(any());
        }
    }

    @Nested
    @DisplayName("mapError()")
    class mapError {
        @Test
        @DisplayName("WHEN given a Function THEN the Function will be invoked AND its return value will be the error of the returned failed ValueResult")
        void withFunction(@Mock Function<ParentMessage, SubMessage> function) {
            when(function.apply(Message.INSTANCE)).thenReturn(SubMessage.INSTANCE);
            assertThat(base.mapError(function)).hasFailed().withError(SubMessage.INSTANCE);
            verify(function, only()).apply(Message.INSTANCE);
        }
    }

    @Nested
    @DisplayName("flatMap()")
    class flatMap {
        @Test
        @DisplayName("WHEN given a Function THEN the Function will not be invoked AND a failed ValueResult will be returned with the error of the base result")
        void withFunction(@Mock Function<ParentValue, ValueResult<String, SubMessage>> function) {
            assertThat(base.flatMap(function)).hasFailed().withError(Message.INSTANCE);
            verify(function, never()).apply(any());
        }
    }

    @Nested
    @DisplayName("combine()")
    class combine {
        @Test
        @DisplayName("WHEN given a successful ValueResult AND a Function THEN the Function will not be invoked AND a failed ValueResult will be returned with the error of the base result")
        void withSuccessfulResult(@Mock BiFunction<ParentValue, ParentValue, SubValue> function) {
            assertThat(base.combine(function, success)).hasFailed().withError(Message.INSTANCE);
            verify(function, never()).apply(any(), any());
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult AND a Function THEN the Function will not be invoked AND a failed ValueResult will be returned with the error of the base result")
        void withFailedResult(@Mock BiFunction<ParentValue, ParentValue, String> function) {
            assertThat(base.combine(function, failed)).hasFailed().withError(Message.INSTANCE);
            verify(function, never()).apply(any(), any());
        }
    }

    @Nested
    @DisplayName("orElse()")
    class orElse {
        @Test
        @DisplayName("Returns the argument")
        void returnsArgument() {
            assertThat(base.orElse(SubValue.INSTANCE)).isEqualTo(SubValue.INSTANCE);
        }
    }

    @Nested
    @DisplayName("orElseGet()")
    class orElseGet {
        @Test
        @DisplayName("Returns the result of the given function")
        void returnsFunctionResult(@Mock Function<ParentMessage, SubValue> function) {
            when(function.apply(Message.INSTANCE)).thenReturn(SubValue.INSTANCE);
            assertThat(base.orElseGet(function)).isEqualTo(SubValue.INSTANCE);
        }
    }

    @Nested
    @DisplayName("orElseThrow()")
    class orElseThrow {
        @Test
        @DisplayName("Throws exception produced by the the given supplier")
        void throwsExceptionFromSupplier(@Mock Supplier<RuntimeException> supplier) {
            // Given
            when(supplier.get()).thenReturn(new RuntimeException("from supplier"));

            // When
            Throwable thrown = catchThrowable(() -> ValueResult.fail("error").orElseThrow(supplier));

            // Then
            assertThat(thrown)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("from supplier");
        }
    }

    @Nested
    @DisplayName("fold()")
    class fold {
        @Test
        @DisplayName("Returns value mapped by the error Function")
        void returnsSuccess(@Mock Function<ParentValue, String> success, @Mock Function<ParentMessage, String> failure) {
            // Given
            when(failure.apply(Message.INSTANCE)).thenReturn("mapped");

            // When
            assertThat(base.fold(success, failure)).isEqualTo("mapped");

            // Then
            verify(success, never()).apply(any());
            verify(failure, only()).apply(Message.INSTANCE);
        }
    }

    @Nested
    @DisplayName("toOptional()")
    class toOptional {
        @Test
        @DisplayName("Returns None")
        void returnsNone() {
            assertThat(base.toOptional().isPresent()).isFalse();
        }
    }

    @Nested
    @DisplayName("toVoidResult()")
    class toVoidResult {
        @Test
        @DisplayName("Returns a failed VoidResult")
        void returnsFailedVoidResult() {
            assertThat(base.toVoidResult()).hasFailed().withError(Message.INSTANCE);
        }
    }
}
