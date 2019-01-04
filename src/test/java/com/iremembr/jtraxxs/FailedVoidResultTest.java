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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("A failed VoidResult")
class FailedVoidResultTest {

    private VoidResult<Message> base;
    private ValueResult<SubValue, SubMessage> success;
    private ValueResult<SubValue, SubMessage> failed;
    private VoidResult<SubMessage> ok;
    private VoidResult<SubMessage> bad;

    @BeforeEach
    void setUp() {
        base = VoidResult.fail(Message.INSTANCE);
        success = ValueResult.ok(SubValue.INSTANCE);
        failed = ValueResult.fail(SubMessage.INSTANCE);
        ok = VoidResult.ok();
        bad = VoidResult.fail(SubMessage.INSTANCE);
    }

    @Nested
    @DisplayName("castError()")
    class castError {
        @Test
        @DisplayName("WHEN given a superclass of the error type THEN castError will return the ValueResult with the adjusted type")
        void validCast() {
            VoidResult<SubMessage> success = VoidResult.fail(SubMessage.INSTANCE);
            VoidResult<Message> result = success.castError(Message.class);
            assertThat(result).hasFailed().withError(SubMessage.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a class which is not a superclass of the error type THEN castError will throw an IllegalArgumentException")
        void invalidCast() {
            Throwable thrown = catchThrowable(() -> base.castError(BigDecimal.class));
            assertThat(thrown)
                    .isInstanceOf(IllegalArgumentException.class)
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
        @DisplayName("error() returns the error")
        void getError() {
            assertThat(base.error()).isEqualTo(Message.INSTANCE);
        }
    }

    @Nested
    @DisplayName("equalsContract")
    class equalsContract {
        @Test
        @DisplayName("equals() ensure hashCode() adhere to the equals contract")
        void equalsAndHashCode() {
            EqualsVerifier.forClass(FailedVoidResult.class).verify();
        }
    }

    @Nested
    @DisplayName("toString()")
    class object {
        @Test
        @DisplayName("Returns a proper representation")
        void toStringMethod() {
            assertThat(base.toString()).isEqualTo("FailedVoidResult{error=Message}");
        }
    }

    @Nested
    @DisplayName("errorStream()")
    class errorStream {
        @Test
        @DisplayName("Returns an Stream with the error")
        void successfulVoidResult() {
            assertThat(base.errorStream()).containsExactly(Message.INSTANCE);
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
        void callsFailureConsumer(@Mock Runnable success, @Mock Consumer<ParentMessage> failure) {
            assertThat(base.onBoth(success, failure)).hasFailed().withError(Message.INSTANCE);
            verify(success, never()).run();
            verify(failure, only()).accept(Message.INSTANCE);
        }
    }

    @Nested
    @DisplayName("ensure()")
    class ensure {
        @Test
        @DisplayName("WHEN given true THEN the VoidResult will be passed through")
        void withTrue() {
            assertThat(base.ensure(true, SubMessage.INSTANCE)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given false THEN the VoidResult will be passed through")
        void withFalse() {
            assertThat(base.ensure(false, SubMessage.INSTANCE)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a Supplier THEN the Supplier will not be invoked AND the VoidResult will be passed through")
        void withSupplier(@Mock Supplier<Boolean> supplier) {
            assertThat(base.ensure(supplier, SubMessage.INSTANCE)).hasFailed().withError(Message.INSTANCE);
            verify(supplier, never()).get();
        }

        @Test
        @DisplayName("WHEN given a successful VoidResult THEN the returned VoidResult has failed with the error of base Result")
        void successfulVoidResult() {
            assertThat(base.ensure(ok)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a successful ValueResult THEN the returned VoidResult has failed with the error of base Result")
        void successfulValueResult() {
            assertThat(base.ensure(success)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a failed VoidResult THEN the returned VoidResult has failed with the error of the base Result")
        void failedVoidResult() {
            assertThat(base.ensure(bad)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult THEN the returned VoidResult has failed with the error of the base Result")
        void failedValueResult() {
            assertThat(base.ensure(failed)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a supplier THEN the returned VoidResult has failed with the error of base Result")
        void voidResultSupplier(@Mock Supplier<VoidResult<SubMessage>> supplier) {
            assertThat(base.ensure(supplier)).hasFailed().withError(Message.INSTANCE);
        }

        @Test
        @DisplayName("WHEN given a supplier THEN the supplier is not invoked")
        void voidResultSupplierNeverCalled(@Mock Supplier<VoidResult<SubMessage>> supplier) {
            base.ensure(supplier);
            verify(supplier, never()).get();
        }
    }

    @Nested
    @DisplayName("mapError()")
    class mapError {
        @Test
        @DisplayName("WHEN given a Function THEN the Function will be invoked AND its return value will be the error of the returned failed VoidResult")
        void withFunction(@Mock Function<ParentMessage, SubMessage> function) {
            when(function.apply(Message.INSTANCE)).thenReturn(SubMessage.INSTANCE);
            assertThat(base.mapError(function)).hasFailed().withError(SubMessage.INSTANCE);
            verify(function, only()).apply(Message.INSTANCE);
        }
    }

    @Nested
    @DisplayName("fold()")
    class fold {
        @Test
        @DisplayName("Returns the value of the failure Function")
        void returnsFailureFunction(@Mock Supplier<String> success, @Mock Function<ParentMessage, SubMessage> failure) {
            when(failure.apply(Message.INSTANCE)).thenReturn(SubMessage.INSTANCE);
            assertThat(base.fold(success, failure)).isEqualTo(SubMessage.INSTANCE);
            verify(success, never()).get();
            verify(failure, only()).apply(Message.INSTANCE);
        }
    }
}
