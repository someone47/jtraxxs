package jtraxxs;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static jtraxxs.RailwayAssertions.assertThat;
import static jtraxxs.RailwayAssertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("A successful VoidResult")
class SuccessfulVoidResultTest {

    @Nested
    @DisplayName("Properties")
    class Properties {
        @Test
        @DisplayName("isSuccessful() returns true")
        void isSuccess() {
            assertThat(VoidResult.ok().isSuccessful()).isTrue();
        }

        @Test
        @DisplayName("hasFailed() returns false")
        void isFailure() {
            assertThat(VoidResult.ok().hasFailed()).isFalse();
        }

        @Test
        @DisplayName("error() throws an IllegalStateException")
        void getError() {
            assertThatThrownBy(() -> VoidResult.ok().error())
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
            assertThat(VoidResult.ok().toString()).isEqualTo("SuccessfulVoidResult");
        }
    }

    @Nested
    @DisplayName("errorStream()")
    class errorStream {
        @Test
        @DisplayName("Returns an empty Stream")
        void errorStreamIsEmpty() {
            assertThat(VoidResult.ok().errorStream()).isEmpty();
        }
    }

    @Nested
    @DisplayName("onSuccess()")
    class onSuccess {
        @Test
        @DisplayName("WHEN given a Runnable THEN the Runnable will be invoked once AND the returned ValueResult is successful")
        void withRunnable(@Mock Runnable runnable) {
            assertThat(VoidResult.ok().onSuccess(runnable)).isSuccessful();
            verify(runnable, only()).run();
        }
    }

    @Nested
    @DisplayName("onFailure()")
    class onFailure {
        @Test
        @DisplayName("WHEN given a Runnable THEN the Runnable will not be invoked AND the base ValueResult will be passed through")
        void withRunnable(@Mock Runnable runnable) {
            assertThat(VoidResult.ok().onFailure(runnable)).isSuccessful();
            verify(runnable, never()).run();
        }

        @Test
        @DisplayName("WHEN given a Consumer THEN the Consumer will not be invoked AND the base ValueResult will be passed through")
        void withConsumer(@Mock Consumer<String> consumer) {
            assertThat(VoidResult.<String>ok().onFailure(consumer)).isSuccessful();
            verify(consumer, never()).accept(any());
        }
    }

    @Nested
    @DisplayName("onBoth()")
    class onBoth {
        @Test
        @DisplayName("Calls success Runnable")
        void returnsSuccess(@Mock Runnable success, @Mock Consumer<String> failure) {
            assertThat(VoidResult.<String>ok().onBoth(success, failure)).isSuccessful();
            verify(success, only()).run();
            verify(failure, never()).accept(anyString());
        }
    }

    @Nested
    @DisplayName("ensure()")
    class ensure {
        @Test
        @DisplayName("WHEN given true THEN the base ValueResult will be passed through")
        void withTrue() {
            assertThat(VoidResult.ok().ensure(true, "error")).isSuccessful();
        }

        @Test
        @DisplayName("WHEN given false THEN a failed ValueResult with the given error will be returned")
        void withFalse() {
            assertThat(VoidResult.ok().ensure(false, "error")).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a Supplier returning true THEN the base ValueResult will be passed through")
        void withSupplierReturningTrue(@Mock Supplier<Boolean> supplier) {
            when(supplier.get()).thenReturn(true);
            assertThat(VoidResult.ok().ensure(supplier, "error")).isSuccessful();
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a Supplier returning false THEN a failed ValueResult with the given error will be returned")
        void withSupplierReturningFalse(@Mock Supplier<Boolean> supplier) {
            when(supplier.get()).thenReturn(false);
            assertThat(VoidResult.ok().ensure(supplier, "error")).hasFailed().withError("error");
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a successful VoidResult THEN the returned VoidResult is successful")
        void successfulVoidResult() {
            assertThat(VoidResult.ok().ensure(VoidResult.ok())).isSuccessful();
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a successful VoidResult THEN the returned VoidResult is successful")
        void successfulVoidResultSupplier(@Mock Supplier<VoidResult<String>> supplier) {
            when(supplier.get()).thenReturn(VoidResult.ok());
            assertThat(VoidResult.ok().ensure(supplier)).isSuccessful();
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a successful ValueResult THEN the returned VoidResult is successful")
        void successfulValueResult() {
            assertThat(VoidResult.ok().ensure(ValueResult.ok("ok"))).isSuccessful();
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a successful ValueResult THEN the returned VoidResult is successful")
        void successfulValueResultSupplier(@Mock Supplier<ValueResult<String, String>> supplier) {
            when(supplier.get()).thenReturn(ValueResult.ok("ok"));
            assertThat(VoidResult.ok().ensure(supplier)).isSuccessful();
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a failed VoidResult THEN the returned VoidResult has failed with the error of the given VoidResult")
        void failedVoidResult() {
            assertThat(VoidResult.ok().ensure(VoidResult.fail("error"))).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a failed VoidResult THEN the returned VoidResult has failed with the error of the given VoidResult")
        void failedVoidResultSupplier(@Mock Supplier<VoidResult<String>> supplier) {
            when(supplier.get()).thenReturn(VoidResult.fail("error"));
            assertThat(VoidResult.ok().ensure(supplier)).hasFailed().withError("error");
            verify(supplier, only()).get();
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult THEN the returned VoidResult has failed with the error of the given ValueResult")
        void failedValueResult() {
            assertThat(VoidResult.ok().ensure(ValueResult.fail("error"))).hasFailed().withError("error");
        }

        @Test
        @DisplayName("WHEN given a Supplier returning a failed ValueResult THEN the returned VoidResult has failed with the error of the given ValueResult")
        void failedValueResultSupplier(@Mock Supplier<ValueResult<String, String>> supplier) {
            when(supplier.get()).thenReturn(ValueResult.fail("error"));
            assertThat(VoidResult.ok().ensure(supplier)).hasFailed().withError("error");
            verify(supplier, only()).get();
        }
    }

    @Nested
    @DisplayName("mapError")
    class mapError {
        @Test
        @DisplayName("WHEN given a Function THEN a returned ValueResult is successful")
        void mapErrorMethod(@Mock Function<String, String> function) {
            assertThat(VoidResult.<String>ok().mapError(function).isSuccessful()).isTrue();
            verify(function, never()).apply(any());
        }
    }

    @Nested
    @DisplayName("fold()")
    class fold {
        @Test
        @DisplayName("Returns the value of the success Supplier")
        void returnsSuccess(@Mock Supplier<String> success, @Mock Function<String, String> failure) {
            when(success.get()).thenReturn("success");
            assertThat(VoidResult.<String>ok().fold(success, failure)).isEqualTo("success");
            verify(success, only()).get();
            verify(failure, never()).apply(anyString());
        }
    }
}
