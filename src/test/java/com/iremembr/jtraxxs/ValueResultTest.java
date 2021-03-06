package com.iremembr.jtraxxs;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static com.iremembr.jtraxxs.RailwayAssertions.assertThat;
import static com.iremembr.jtraxxs.ValueResult.fail;
import static com.iremembr.jtraxxs.ValueResult.ok;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;


@DisplayName("A ValueResult")
class ValueResultTest {

    @Nested
    @DisplayName("ok()")
    class ok {
        @Test
        @DisplayName("WHEN given a object THEN a successful result with the given value will be returned")
        void withObject() {
            assertThat(ValueResult.ok("success").value()).isEqualTo("success");
        }

        @Test
        @DisplayName("WHEN given null THEN a successful result with the value null will be returned")
        void withNull() {
            assertThat(ValueResult.ok(null).value()).isNull();
        }
    }

    @Nested
    @DisplayName("fail()")
    class fail {
        @Test
        @DisplayName("WHEN given a object THEN a failed result with the given error will be returned")
        void withObject() {
            assertThat(ValueResult.fail("error").error()).isEqualTo("error");
        }

        @Test
        @DisplayName("WHEN given null THEN a failed result with the error null will be returned")
        void withNull() {
            assertThat(ValueResult.fail(null).error()).isNull();
        }
    }

    @Nested
    @DisplayName("fromOptional()")
    class fromOptional {
        @Test
        @DisplayName("WHEN given a nonempty Optional THEN fromOptional will return a successful result")
        void withNonEmptyOptional() {
            assertThat(ValueResult.fromOptional(Optional.of("success"), "error").value()).isEqualTo("success");
        }

        @Test
        @DisplayName("WHEN given an empty Optional THEN fromOptional will return a failed result")
        void withEmptyOptional() {
            assertThat(ValueResult.fromOptional(Optional.empty(), "error").error()).isEqualTo("error");
        }

        @Test
        @DisplayName("WHEN given null Optional THEN a NullPointerException will be thrown")
        void withNullError() {
            Assertions.assertThatThrownBy(() -> ValueResult.fromOptional(null, "error"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("optional must not be null");
        }
    }

    @Nested
    @DisplayName("fromNullable()")
    class fromNullable {
        @Test
        @DisplayName("WHEN given a non-null value THEN fromNullable will return a successful result")
        void withValue() {
            assertThat(ValueResult.fromNullable("success", "error").value()).isEqualTo("success");
        }

        @Test
        @DisplayName("WHEN given null THEN fromNullable will return a failed result")
        void withNull() {
            assertThat(ValueResult.fromNullable(null, "error").error()).isEqualTo("error");
        }
    }

    @Nested
    @DisplayName("upCast()")
    class upCast {
        @Test
        @DisplayName("WHEN given a successful ValueResult THEN upCast will return the result with adjusted types")
        void withSuccessfulValueResult() {
            ValueResult<String, SubMessage> success = ok("success");
            ValueResult<CharSequence, Message> result = ValueResult.upCast(success);
            assertThat(result).isSuccessful().withValue("success");
            assertThat(result.value()).isExactlyInstanceOf(String.class);
        }

        @Test
        @DisplayName("WHEN given a failed ValueResult THEN upCast will return the result with adjusted types")
        void withFailedValueResult() {
            ValueResult<String, SubMessage> failed = fail(new SubMessage());
            ValueResult<CharSequence, Message> result = ValueResult.upCast(failed);
            assertThat(result).hasFailed();
            assertThat(result.error()).isExactlyInstanceOf(SubMessage.class);
        }
    }

    @Nested
    @DisplayName("sequence()")
    class sequence {
        @Test
        @DisplayName("WHEN given an empty list of Results THEN sequence will return a successful ValueResult with an empty list")
        void withEmptyList() {
            ValueResult<Collection<String>, Collection<String>> result = ValueResult.sequence(emptyList());
            assertThat(result).isSuccessful();
            assertThat(result.value().isEmpty()).isTrue();
        }

        @Test
        @DisplayName("WHEN given two successful Results THEN sequence will return a successful ValueResult with a list with two values")
        void withTwoSuccessfulResults() {
            ValueResult<Collection<Integer>, Collection<String>> result = ValueResult.sequence(asList(ok(1), ok(2)));
            assertThat(result).isSuccessful();
            assertThat(result.value()).containsExactly(1, 2);
        }

        @Test
        @DisplayName("WHEN given one successful and two failed Results THEN sequence will return a failed ValueResult with a list containing two errors")
        void withOneSuccessfulAndTwoFailedResults() {
            ValueResult<Collection<Integer>, Collection<String>> result = ValueResult.sequence(asList(fail("err1"), ok(2), fail("err2")));
            assertThat(result).hasFailed();
            assertThat(result.error()).containsExactly("err1", "err2");
        }
    }
}