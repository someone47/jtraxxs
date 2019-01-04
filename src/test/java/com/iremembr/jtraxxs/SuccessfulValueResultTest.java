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
@DisplayName("A successful ValueResult")
class SuccessfulValueResultTest {

    private ValueResult<Value, Message> base;
    private ValueResult<SubValue, SubMessage> success;
    private ValueResult<SubValue, SubMessage> failed;
    private VoidResult<SubMessage> ok;
    private VoidResult<SubMessage> bad;

    @BeforeEach
    void setUp() {
        base = ValueResult.ok(Value.INSTANCE);
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
            ValueResult<ParentValue, Message> result = base.castValue(ParentValue.class);
            assertThat(result).isSuccessful().withValue(Value.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a class which is not a superclass of the value type THEN castValue throw an IllegalArgumentException")
        void invalidCast() {
            Throwable thrown = catchThrowable(() -> base.castValue(BigDecimal.class));
            assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Can not cast the value to the given type");
        }
    }

    @Nested
    @DisplayName("castError()")
    class castError {
        @Test
        @DisplayName("WHEN given a superclass of the error type THEN castError will return the ValueResult with the adjusted type")
        void validCast() {
            ValueResult<Value, Message> result = base.castError(Message.class);
            assertThat(result).isSuccessful().withValue(Value.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a class which is not a superclass of the error type THEN castError  will return the ValueResult with the adjusted type")
        void invalidCast() {
            ValueResult<Value, BigDecimal> result = base.castError(BigDecimal.class);
            assertThat(result).isSuccessful().withValue(Value.INSTANCE);
        }
    }

    @Nested
    @DisplayName("Properties")
    class Properties {
        @Test
        @DisplayName("isSuccessful() returns true")
        void isSuccess() {
            assertThat(base.isSuccessful()).isTrue();
        }

        @Test
        @DisplayName("hasFailed() returns false")
        void hasFailed() {
            assertThat(base.hasFailed()).isFalse();
        }

        @Test
        @DisplayName("value() returns the value")
        void value() {
            assertThat(base.value()).isEqualTo(Value.INSTANCE);
        }

        @Test
        @DisplayName("error() throws an IllegalStateException")
        void getError() {
            // When
            Throwable thrown = catchThrowable(() -> base.error());

            // Then
            assertThat(thrown)
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
            assertThat(base.toString()).isEqualTo("SuccessfulValueResult{value=Value}");
        }
    }

    @Nested
    @DisplayName("errorStream()")
    class errorStream {
        @Test
        @DisplayName("Returns an empty Stream")
        void errorStreamIsEmpty() {
            assertThat(base.errorStream()).isEmpty();
        }
    }

    @Nested
    @DisplayName("stream()")
    class stream {
        @Test
        @DisplayName("Returns an empty Stream")
        void streamHasOneValue() {
            assertThat(base.stream()).containsExactly(Value.INSTANCE);
        }
    }

    @Nested
    @DisplayName("onSuccess()")
    class onSuccess {
        @Test
        @DisplayName("WHEN given a Runnable THEN the returned ValueResult is successful with the value of the base Result")
        void withRunnable(@Mock Runnable runnable) {
            assertThat(base.onSuccess(runnable)).isSuccessful().withValue(Value.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Runnable THEN the Runnable will be invoked once")
        void withRunnableInvoke(@Mock Runnable runnable) {
            base.onSuccess(runnable);
            verify(runnable, only()).run();
        }

        @Test
        @DisplayName("WHEN given a Consumer THEN the returned ValueResult is successful with the value of the base Result")
        void withConsumer(@Mock Consumer<ParentValue> consumer) {
            assertThat(base.onSuccess(consumer)).isSuccessful().withValue(Value.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Consumer THEN the Consumer will be invoked once")
        void withConsumerInvoked(@Mock Consumer<ParentValue> consumer) {
            base.onSuccess(consumer);
            verify(consumer, only()).accept(Value.INSTANCE);
        }
    }

    @Nested
    @DisplayName("onFailure()")
    class onFailure {
        @Test
        @DisplayName("WHEN given a Runnable THEN the returned ValueResult is successful with the value of the base Result")
        void withRunnable(@Mock Runnable runnable) {
            assertThat(base.onFailure(runnable)).isSuccessful().withValue(Value.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Runnable THEN the Runnable will not be invoked ensure the base ValueResult will be passed through")
        void withRunnableInvoked(@Mock Runnable runnable) {
            base.onFailure(runnable);
            verify(runnable, never()).run();
        }

        @Test
        @DisplayName("WHEN given a Consumer THEN the returned ValueResult is successful with the value of the base Result")
        void withConsumer(@Mock Consumer<ParentMessage> consumer) {
            assertThat(base.onFailure(consumer)).isSuccessful().withValue(Value.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Consumer THEN the Consumer will not be invoked")
        void withConsumerInvoked(@Mock Consumer<ParentMessage> consumer) {
            base.onFailure(consumer);
            verify(consumer, never()).accept(any());
        }
    }

    @Nested
    @DisplayName("onBoth()")
    class ifSuccessOrFailure {
        @Test
        @DisplayName("Calls success Consumer")
        void returnsSuccess(@Mock Consumer<ParentValue> success, @Mock Consumer<ParentMessage> failure) {
            base.onBoth(success, failure);
            verify(success, only()).accept(Value.INSTANCE);
            verify(failure, never()).accept(any(Message.class));
        }
    }

    @Nested
    @DisplayName("ensure()")
    class ensure {
        @Test
        @DisplayName("WHEN given true THEN the returned ValueResult is successful with the value of the base Result")
        void withTrue() {
            ValueResult<Value, Message> result = base.ensure(true, SubMessage.INSTANCE);
            assertThat(result).isSuccessful().withValue(Value.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given false THEN a failed ValueResult with the given error will be returned")
        void withFalse() {
            ValueResult<Value, Message> result = base.ensure(false, SubMessage.INSTANCE);
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Supplier returning true THEN the returned ValueResult is successful with the value of the base Result")
        void withSupplierReturningTrue(@Mock Supplier<Boolean> supplier) {
            // Given
            when(supplier.get()).thenReturn(true);

            // When
            ValueResult<Value, Message> result = base.ensure(supplier, SubMessage.INSTANCE);

            // Then
            assertThat(result).isSuccessful().withValue(Value.INSTANCE);
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Supplier returning false THEN the returned ValueResult has failed with the given error")
        void withSupplierReturningFalse(@Mock Supplier<Boolean> supplier) {
            // Given
            when(supplier.get()).thenReturn(false);

            // When
            ValueResult<Value, Message> result = base.ensure(supplier, SubMessage.INSTANCE);

            // Then
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Predicate returning true THEN the returned ValueResult is successful with the value of the base Result")
        void withPredicateReturningTrue(@Mock Predicate<ParentValue> predicate) {
            // Given
            when(predicate.test(Value.INSTANCE)).thenReturn(true);

            // When
            ValueResult<Value, Message> result = base.ensure(predicate, SubMessage.INSTANCE);

            // Then
            assertThat(result).isSuccessful().withValue(Value.INSTANCE);
            verify(predicate, only()).test(Value.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Predicate returning false THEN the returned ValueResult has failed with the given error")
        void withPredicateReturningFalse(@Mock Predicate<Value> predicate) {
            // Given
            when(predicate.test(Value.INSTANCE)).thenReturn(false);

            // When
            ValueResult<Value, Message> result = base.ensure(predicate, SubMessage.INSTANCE);

            // Then
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
            verify(predicate, only()).test(Value.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Function returning a successful VoidResult THEN the returned ValueResult is successful")
        void withSuccessfulVoidResultFunction(@Mock Function<ParentValue, VoidResult<SubMessage>> function) {
            // Given
            when(function.apply(Value.INSTANCE)).thenReturn(ok);

            // When
            ValueResult<Value, Message> result = base.ensure(function);

            // Then
            assertThat(result).isSuccessful().withValue(Value.INSTANCE);
            verify(function, only()).apply(any());
        }

        @Test
        @DisplayName("WHEN given a successful ValueResult THEN the returned ValueResult is successful")
        void withSuccessfulValueResult() {
            ValueResult<Value, Message> result = base.ensure(success);
            assertThat(result).isSuccessful().withValue(Value.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a failed VoidResult THEN the returned ValueResult has failed with the error of the given VoidResult")
        void withFailedVoidResult() {
            ValueResult<Value, Message> result = base.ensure(failed);
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a successful ValueResult THEN the returned ValueResult is successful")
        void withSuccessfulValueResultSupplier(@Mock Supplier<ValueResult<SubValue, SubMessage>> supplier) {
            // Given
            when(supplier.get()).thenReturn(success);

            // When
            ValueResult<Value, Message> result = base.ensure(supplier);

            // Then
            assertThat(result).isSuccessful().withValue(Value.INSTANCE);
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Function returning a successful ValueResult THEN the returned ValueResult is successful")
        void withSuccessfulValueResultFunction(@Mock Function<ParentValue, ValueResult<SubValue, SubMessage>> function) {
            // Given
            when(function.apply(Value.INSTANCE)).thenReturn(success);

            // When
            ValueResult<Value, Message> result = base.ensure(function);

            // Then
            assertThat(result).isSuccessful().withValue(Value.INSTANCE);
            verify(function, only()).apply(any());
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a failed VoidResult THEN the returned ValueResult has failed with the error of the given VoidResult")
        void withFailedVoidResultSupplier(@Mock Supplier<VoidResult<SubMessage>> supplier) {
            // Given
            when(supplier.get()).thenReturn(bad);

            // When
            ValueResult<Value, Message> result = base.ensure(supplier);

            // Then
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Function returning a failed VoidResult THEN the returned ValueResult has failed with the error of the given VoidResult")
        void withFailedVoidResultFunction(@Mock Function<ParentValue, VoidResult<SubMessage>> function) {
            // Given
            when(function.apply(Value.INSTANCE)).thenReturn(bad);

            // When
            ValueResult<Value, Message> result = base.ensure(function);

            // Then
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
            verify(function, only()).apply(any());
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult THEN the returned ValueResult has failed with the error of the given ValueResult")
        void withFailedValueResult() {
            ValueResult<Value, Message> result = base.ensure(failed);
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a failed ValueResult THEN the returned ValueResult has failed with the error of the given ValueResult")
        void withFailedValueResultSupplier(@Mock Supplier<ValueResult<SubValue, SubMessage>> supplier) {
            // Given
            when(supplier.get()).thenReturn(failed);

            // When
            ValueResult<Value, Message> result = base.ensure(supplier);

            // Then
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Function returning a failed ValueResult THEN the returned ValueResult has failed with the error of the given ValueResult")
        void withFailedValueResultFunction(@Mock Function<ParentValue, ValueResult<SubValue, SubMessage>> function) {
            // Given
            when(function.apply(Value.INSTANCE)).thenReturn(failed);

            // When
            ValueResult<Value, Message> result = base.ensure(function);

            // Then
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
            verify(function, only()).apply(any());
        }
    }

    @Nested
    @DisplayName("take()")
    class take {
        @Test
        @DisplayName("WHEN given a successful ValueResult THEN the returned ValueResult is successful with the value of the given Result")
        void withSuccessfulResult() {
            ValueResult<Value, Message> result = base.take(success);
            assertThat(result).isSuccessful().withValue(SubValue.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult THEN the returned ValueResult has failed with the error of the given Result")
        void withFailedResult() {
            ValueResult<Value, Message> result = base.take(failed);
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a successful ValueResult THEN the returned ValueResult is successful with the value of the given Result")
        void withSupplierReturningSuccessfulResult(@Mock Supplier<ValueResult<SubValue, SubMessage>> supplier) {
            // Given
            when(supplier.get()).thenReturn(success);

            // When
            ValueResult<Value, Message> result = base.take(supplier);

            // Then
            assertThat(result).isSuccessful().withValue(SubValue.INSTANCE);
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a failed ValueResult THEN the returned ValueResult has failed with the error of the given Result")
        void withSupplierReturningFailedResult(@Mock Supplier<ValueResult<SubValue, SubMessage>> supplier) {
            // Given
            when(supplier.get()).thenReturn(failed);

            // When
            ValueResult<Value, Message> result = base.take(supplier);

            // Then
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Function returning a successful ValueResult THEN the returned ValueResult is successful with the value of the given Result")
        void withFunctionReturningSuccessfulResult(@Mock Function<ParentValue, ValueResult<SubValue, SubMessage>> function) {
            // Given
            when(function.apply(Value.INSTANCE)).thenReturn(success);

            // When
            ValueResult<Value, Message> result = base.take(function);

            // Then
            assertThat(result).isSuccessful().withValue(SubValue.INSTANCE);
            verify(function, only()).apply(Value.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Function returning a failed ValueResult THEN the returned ValueResult has failed with the error of the given Result")
        void withFunctionReturningFailedResult(@Mock Function<ParentValue, ValueResult<SubValue, SubMessage>> function) {
            // Given
            when(function.apply(Value.INSTANCE)).thenReturn(failed);

            // When
            ValueResult<Value, Message> result = base.take(function);

            // Then
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
            verify(function, only()).apply(Value.INSTANCE);
        }
    }

    @Nested
    @DisplayName("map()")
    class map {
        @Test
        @DisplayName("WHEN given a mapper Function THEN the returned ValueResult is successful with the value returned by the mapper Function")
        void mapValue(@Mock Function<ParentValue, SubValue> mapper) {
            when(mapper.apply(Value.INSTANCE)).thenReturn(SubValue.INSTANCE);
            assertThat(base.map(mapper)).isSuccessful().withValue(SubValue.INSTANCE);
            verify(mapper, only()).apply(Value.INSTANCE);
        }
    }

    @Nested
    @DisplayName("mapError()")
    class mapError {
        @Test
        @DisplayName("WHEN given a mapper Function THEN the returned ValueResult is successful with the value of the base Result")
        void mapErrorReturnsSuccessfulValueResult(@Mock Function<ParentMessage, Message> mapper) {
            assertThat(base.mapError(mapper)).isSuccessful().withValue(Value.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a mapper Function THEN the mapper function is not invoked")
        void mapFunctionNotInvoked(@Mock Function<Message, Message> mapper) {
            base.mapError(mapper);
            verify(mapper, never()).apply(any(Message.class));
        }
    }

    @Nested
    @DisplayName("flatMap()")
    class flatMap {
        @Test
        @DisplayName("WHEN given a mapper Function returning a successful ValueResult THEN this ValueResult will be returned")
        void flatMapReturningSuccessfulValueResult(@Mock Function<ParentValue, ValueResult<SubValue, SubMessage>> mapper) {
            when(mapper.apply(Value.INSTANCE)).thenReturn(success);
            assertThat(base.flatMap(mapper)).isSuccessful().withValue(SubValue.INSTANCE);
            verify(mapper, only()).apply(Value.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a mapper Function returning a failed ValueResult THEN this ValueResult will be returned")
        void flatMapReturningFailedValueResult(@Mock Function<ParentValue, ValueResult<SubValue, SubMessage>> mapper) {
            when(mapper.apply(Value.INSTANCE)).thenReturn(failed);
            assertThat(base.flatMap(mapper)).hasFailed().withError(SubMessage.INSTANCE);
            verify(mapper, only()).apply(Value.INSTANCE);
        }
    }

    @Nested
    @DisplayName("combine()")
    class combine {
        @Test
        @DisplayName("WHEN given a successful ValueResult ensure a Function THEN the returned ValueResult is successful with a value returned by the Function")
        void withSuccessfulResult(@Mock BiFunction<ParentValue, ParentValue, String> function) {
            when(function.apply(Value.INSTANCE, SubValue.INSTANCE)).thenReturn("combined");
            assertThat(base.combine(function, success)).isSuccessful().withValue("combined");
            verify(function, only()).apply(Value.INSTANCE, SubValue.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult ensure a Function THEN the returned ValueResult has failed with the error of the base Result")
        void withFailedResult(@Mock BiFunction<ParentValue, ParentValue, String> function) {
            assertThat(base.combine(function, failed)).hasFailed().withError(SubMessage.INSTANCE);
            verify(function, never()).apply(any(), any());
        }
    }

    @Nested
    @DisplayName("orElse()")
    class orElse {
        @Test
        @DisplayName("Returns the value")
        void returnsValue() {
            assertThat(base.orElse(SubValue.INSTANCE)).isEqualTo(Value.INSTANCE);
        }
    }

    @Nested
    @DisplayName("orElseGet()")
    class orElseGet {
        @Test
        @DisplayName("Returns the value")
        void returnsValue(@Mock Function<ParentMessage, SubValue> function) {
            assertThat(base.orElseGet(function)).isEqualTo(Value.INSTANCE);
            verify(function, never()).apply(any());
        }
    }

    @Nested
    @DisplayName("orElseThrow()")
    class orElseThrow {
        @Test
        @DisplayName("Throws exception produced by the the given supplier")
        void throwsExceptionFromSupplier(@Mock Supplier<RuntimeException> supplier) {
            assertThat(base.orElseThrow(supplier)).isEqualTo(Value.INSTANCE);
            verify(supplier, never()).get();
        }
    }

    @Nested
    @DisplayName("fold()")
    class fold {
        @Test
        @DisplayName("Returns value mapped by the success Function")
        void returnsSuccess(@Mock Function<ParentValue, SubValue> success, @Mock Function<ParentMessage, SubMessage> failure) {
            // Given
            when(success.apply(Value.INSTANCE)).thenReturn(SubValue.INSTANCE);

            // When
            assertThat(base.fold(success, failure)).isEqualTo(SubValue.INSTANCE);

            // Then
            verify(success, only()).apply(Value.INSTANCE);
            verify(failure, never()).apply(any());
        }
    }

    @Nested
    @DisplayName("toOptional()")
    class toOptional {
        @Test
        @DisplayName("Returns value wrapped in an Optional")
        void returnsOptionalWithValue() {
            //noinspection OptionalGetWithoutIsPresent
            assertThat(base.toOptional().get()).isEqualTo(Value.INSTANCE);
        }
    }

    @Nested
    @DisplayName("toVoidResult()")
    class toVoidResult {
        @Test
        @DisplayName("Returns a successful VoidResult")
        void returnsSuccessfulVoidResult() {
            VoidResult<Message> result = base.toVoidResult();
            assertThat(result).isSuccessful();
        }
    }
}
