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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.iremembr.jtraxxs.RailwayAssertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("A successful VoidResult")
class SuccessfulVoidResultTest {

    private VoidResult<Message> base;
    private ValueResult<SubValue, SubMessage> success;
    private ValueResult<SubValue, SubMessage> failed;
    private VoidResult<SubMessage> ok;
    private VoidResult<SubMessage> bad;

    @BeforeEach
    void setUp() {
        base = VoidResult.ok();
        success = ValueResult.ok(SubValue.INSTANCE);
        failed = ValueResult.fail(SubMessage.INSTANCE);
        ok = VoidResult.ok();
        bad = VoidResult.fail(SubMessage.INSTANCE);
    }

    @Nested
    @DisplayName("castError()")
    class castError {
        @Test
        @DisplayName("WHEN given a superclass of the error type THEN castError will return the VoidResult with the adjusted type")
        void validCast() {
            VoidResult<SubMessage> success = VoidResult.ok();
            VoidResult<Message> result = success.castError(Message.class);
            assertThat(result).isSuccessful();
        }

        @Test
        @DisplayName("WHEN given a class which is not a superclass of the error type THEN castError  will return the VoidResult with the adjusted type")
        void invalidCast() {
            VoidResult<Message> success = VoidResult.ok();
            VoidResult<BigDecimal> result = success.castError(BigDecimal.class);
            assertThat(result).isSuccessful();
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
        void isFailure() {
            assertThat(base.hasFailed()).isFalse();
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
            EqualsVerifier.forClass(SuccessfulVoidResult.class).verify();
        }
    }

    @Nested
    @DisplayName("toString()")
    class toString {
        @Test
        @DisplayName("Returns a proper representation")
        void toStringMethod() {
            assertThat(base.toString()).isEqualTo("SuccessfulVoidResult");
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
    @DisplayName("onSuccess()")
    class onSuccess {
        @Test
        @DisplayName("WHEN given a Runnable THEN the Runnable will be invoked once AND the returned ValueResult is successful")
        void withRunnable(@Mock Runnable runnable) {
            assertThat(base.onSuccess(runnable)).isSuccessful();
            verify(runnable, only()).run();
        }
    }

    @Nested
    @DisplayName("onFailure()")
    class onFailure {
        @Test
        @DisplayName("WHEN given a Runnable THEN the Runnable will not be invoked AND the base ValueResult will be passed through")
        void withRunnable(@Mock Runnable runnable) {
            assertThat(base.onFailure(runnable)).isSuccessful();
            verify(runnable, never()).run();
        }

        @Test
        @DisplayName("WHEN given a Consumer THEN the Consumer will not be invoked AND the base ValueResult will be passed through")
        void withConsumer(@Mock Consumer<ParentMessage> consumer) {
            assertThat(base.onFailure(consumer)).isSuccessful();
            verify(consumer, never()).accept(any());
        }
    }

    @Nested
    @DisplayName("onBoth()")
    class onBoth {
        @Test
        @DisplayName("Calls success Runnable")
        void returnsSuccess(@Mock Runnable success, @Mock Consumer<ParentMessage> failure) {
            assertThat(base.onBoth(success, failure)).isSuccessful();
            verify(success, only()).run();
            verify(failure, never()).accept(any(ParentMessage.class));
        }
    }

    @Nested
    @DisplayName("ensure()")
    class ensure {
        @Test
        @DisplayName("WHEN given true THEN the base ValueResult will be passed through")
        void withTrue() {
            VoidResult<Message> result = base.ensure(true, SubMessage.INSTANCE);
            assertThat(result).isSuccessful();
        }

        @Test
        @DisplayName("WHEN given false THEN a failed ValueResult with the given error will be returned")
        void withFalse() {
            VoidResult<Message> result = base.ensure(false, SubMessage.INSTANCE);
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Supplier returning true THEN the base ValueResult will be passed through")
        void withSupplierReturningTrue(@Mock Supplier<Boolean> supplier) {
            // Given
            when(supplier.get()).thenReturn(true);

            // When
            VoidResult<Message> result = base.ensure(supplier, SubMessage.INSTANCE);

            // Then
            assertThat(result).isSuccessful();
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Supplier returning false THEN a failed ValueResult with the given error will be returned")
        void withSupplierReturningFalse(@Mock Supplier<Boolean> supplier) {
            // Given
            when(supplier.get()).thenReturn(false);

            // When
            VoidResult<Message> result = base.ensure(supplier, SubMessage.INSTANCE);

            // Then
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a successful VoidResult THEN the returned VoidResult is successful")
        void successfulVoidResult() {
            VoidResult<Message> result = base.ensure(ok);
            assertThat(result).isSuccessful();
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a successful VoidResult THEN the returned VoidResult is successful")
        void successfulVoidResultSupplier(@Mock Supplier<VoidResult<SubMessage>> supplier) {
            // Given
            when(supplier.get()).thenReturn(ok);

            // When
            VoidResult<Message> result = base.ensure(supplier);

            // Then
            assertThat(result).isSuccessful();
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a successful ValueResult THEN the returned VoidResult is successful")
        void successfulValueResult() {
            VoidResult<Message> result = base.ensure(success);
            assertThat(result).isSuccessful();
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a successful ValueResult THEN the returned VoidResult is successful")
        void successfulValueResultSupplier(@Mock Supplier<ValueResult<SubValue, SubMessage>> supplier) {
            // Given
            when(supplier.get()).thenReturn(success);

            // When
            VoidResult<Message> result = base.ensure(supplier);

            // Then
            assertThat(result).isSuccessful();
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a failed VoidResult THEN the returned VoidResult has failed with the error of the given VoidResult")
        void failedVoidResult() {
            VoidResult<Message> result = base.ensure(bad);
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a failed VoidResult THEN the returned VoidResult has failed with the error of the given VoidResult")
        void failedVoidResultSupplier(@Mock Supplier<VoidResult<SubMessage>> supplier) {
            // Given
            when(supplier.get()).thenReturn(bad);

            // When
            VoidResult<Message> result = base.ensure(supplier);

            // Then
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult THEN the returned VoidResult has failed with the error of the given ValueResult")
        void failedValueResult() {
            VoidResult<Message> result = base.ensure(failed);
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a failed ValueResult THEN the returned VoidResult has failed with the error of the given ValueResult")
        void failedValueResultSupplier(@Mock Supplier<ValueResult<SubValue, SubMessage>> supplier) {
            // Given
            when(supplier.get()).thenReturn(failed);

            // When
            VoidResult<Message> result = base.ensure(supplier);

            // Then
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
            verify(supplier, only()).get();
        }
    }

    @Nested
    @DisplayName("mapError")
    class mapError {
        @Test
        @DisplayName("WHEN given a Function THEN a returned ValueResult is successful")
        void mapErrorMethod(@Mock Function<ParentMessage, SubMessage> function) {
            assertThat(base.mapError(function).isSuccessful()).isTrue();
            verify(function, never()).apply(any());
        }
    }

    @Nested
    @DisplayName("fold()")
    class fold {
        @Test
        @DisplayName("Returns the value of the success Supplier")
        void returnsSuccess(@Mock Supplier<ParentValue> success, @Mock Function<ParentMessage, SubMessage> failure) {
            // Given
            when(success.get()).thenReturn(SubValue.INSTANCE);

            // When
            assertThat(base.fold(success, failure)).isEqualTo(SubValue.INSTANCE);

            // Then
            verify(success, only()).get();
            verify(failure, never()).apply(any());
        }
    }
}
