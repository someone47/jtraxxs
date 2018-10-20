package jtraxxs;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("A failed VoidResult")
class FailedVoidResultTest {

    @Nested
    @DisplayName("Properties")
    class properties {
        @Test
        @DisplayName("isSuccessful() returns false")
        void isSuccess() {
            Assertions.assertThat(VoidResult.fail("error").isSuccessful()).isFalse();
        }

        @Test
        @DisplayName("hasFailed() returns true")
        void isFailure() {
            Assertions.assertThat(VoidResult.fail("error").hasFailed()).isTrue();
        }

        @Test
        @DisplayName("error() returns the error")
        void getError() {
            Assertions.assertThat(VoidResult.fail("error").error()).isEqualTo("error");
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
            Assertions.assertThat(VoidResult.fail("error").toString()).isEqualTo("FailedVoidResult{error=error}");
        }
    }

    @Nested
    @DisplayName("errorStream()")
    class errorStream {
        @Test
        @DisplayName("Returns an Stream with the error")
        void successfulVoidResult() {
            Assertions.assertThat(VoidResult.fail("error").errorStream()).containsExactly("error");
        }
    }

    @Nested
    @DisplayName("onSuccess()")
    class onSuccess {
        @Test
        @DisplayName("WHEN given a Runnable THEN the Runnable will not be invoked AND the base ValueResult will be passed through")
        void withRunnable(@Mock Runnable runnable) {
            RailwayAssertions.assertThat(VoidResult.fail("error").onSuccess(runnable)).hasFailed().withError("error");
            verify(runnable, never()).run();
        }
    }

    @Nested
    @DisplayName("onFailure()")
    class onFailure {
        @Test
        @DisplayName("WHEN given a Runnable THEN the Runnable will be invoked once AND the base ValueResult will be passed through")
        void withRunnable(@Mock Runnable runnable) {
            RailwayAssertions.assertThat(VoidResult.fail("error").onFailure(runnable)).hasFailed().withError("error");
            verify(runnable, only()).run();
        }

        @Test
        @DisplayName("WHEN given a Consumer THEN the Consumer will be invoked once AND the base ValueResult will be passed through")
        void withConsumer(@Mock Consumer<String> consumer) {
            RailwayAssertions.assertThat(VoidResult.fail("error").onFailure(consumer)).hasFailed().withError("error");
            verify(consumer, only()).accept("error");
        }
    }

    @Nested
    @DisplayName("onBoth()")
    class onBoth {
        @Test
        @DisplayName("Calls failure Consumer")
        void callsFailureConsumer(@Mock Runnable success, @Mock Consumer<String> failure) {
            RailwayAssertions.assertThat(VoidResult.fail("error").onBoth(success, failure)).hasFailed().withError("error");
            verify(success, never()).run();
            verify(failure, only()).accept("error");
        }
    }

    @Nested
    @DisplayName("ensure()")
    class ensure {
        @Test
        @DisplayName("WHEN given true THEN the VoidResult will be passed through")
        void withTrue() {
            RailwayAssertions.assertThat(VoidResult.fail("error").ensure(true, "error")).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given false THEN the VoidResult will be passed through")
        void withFalse() {
            RailwayAssertions.assertThat(VoidResult.fail("error").ensure(false, "error")).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a Supplier THEN the Supplier will not be invoked AND the VoidResult will be passed through")
        void withSupplier(@Mock Supplier<Boolean> supplier) {
            RailwayAssertions.assertThat(VoidResult.fail("error").ensure(supplier, "error")).hasFailed().withError("error");
            verify(supplier, never()).get();
        }

        @Test
        @DisplayName("WHEN given a successful VoidResult THEN the returned VoidResult has failed with the error of base Result")
        void successfulVoidResult() {
            RailwayAssertions.assertThat(VoidResult.fail("error").ensure(VoidResult.ok())).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a successful ValueResult THEN the returned VoidResult has failed with the error of base Result")
        void successfulValueResult() {
            RailwayAssertions.assertThat(VoidResult.fail("error").ensure(ValueResult.ok("ok"))).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a failed VoidResult THEN the returned VoidResult has failed with the error of the base Result")
        void failedVoidResult() {
            RailwayAssertions.assertThat(VoidResult.fail("error").ensure(VoidResult.fail("bad"))).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult THEN the returned VoidResult has failed with the error of the base Result")
        void failedValueResult() {
            RailwayAssertions.assertThat(VoidResult.fail("error").ensure(ValueResult.fail("bad"))).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a supplier THEN the returned VoidResult has failed with the error of base Result")
        void voidResultSupplier(@Mock Supplier<VoidResult<String>> supplier) {
            RailwayAssertions.assertThat(VoidResult.fail("error").ensure(supplier)).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a supplier THEN the supplier is not invoked")
        void voidResultSupplierNeverCalled(@Mock Supplier<VoidResult<String>> supplier) {
            VoidResult.fail("error").ensure(supplier);
            verify(supplier, never()).get();
        }
    }

    @Nested
    @DisplayName("mapError()")
    class mapError {
        @Test
        @DisplayName("WHEN given a Function THEN the Function will be invoked AND its return value will be the error of the returned failed VoidResult")
        void withFunction(@Mock Function<String, String> function) {
            when(function.apply("error")).thenReturn("failedResult");
            RailwayAssertions.assertThat(VoidResult.fail("error").mapError(function)).hasFailed().withError("failedResult");
            verify(function, only()).apply("error");
        }
    }

    @Nested
    @DisplayName("fold()")
    class fold {
        @Test
        @DisplayName("Returns the value of the failure Function")
        void returnsFailureFunction(@Mock Supplier<String> success, @Mock Function<String, String> failure) {
            when(failure.apply("error")).thenReturn("mapped");
            Assertions.assertThat(VoidResult.fail("error").fold(success, failure)).isEqualTo("mapped");
            verify(success, never()).get();
            verify(failure, only()).apply("error");

        }
    }
}
