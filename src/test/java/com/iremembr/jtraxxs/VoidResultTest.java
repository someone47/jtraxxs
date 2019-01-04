package com.iremembr.jtraxxs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static com.iremembr.jtraxxs.RailwayAssertions.assertThat;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

@DisplayName("A VoidResult")
class VoidResultTest {

    @Nested
    @DisplayName("fail()")
    class fail {
        @Test
        @DisplayName("WHEN given a object THEN a failed result with the given error will be returned")
        void withObject() {
            assertThat(VoidResult.fail("error").error()).isEqualTo("error");
        }

        @Test
        @DisplayName("WHEN given null THEN a failed result with the error null will be returned")
        void withNull() {
            assertThat(VoidResult.fail(null).error()).isNull();
        }
    }

    @Nested
    @DisplayName("upCast()")
    class upCast {
        @Test
        @DisplayName("WHEN given a successful VoidResult THEN upCast will return the result with adjusted type")
        void withSuccessfulVoidResult() {
            VoidResult<SubMessage> success = VoidResult.ok();
            VoidResult<Message> result = VoidResult.upCast(success);
            assertThat(result).isSuccessful();
        }

        @Test
        @DisplayName("WHEN given a failed VoidResult THEN upCast will return the result with adjusted types")
        void withFailedVoidResult() {
            VoidResult<SubMessage> failed = VoidResult.fail(new SubMessage());
            VoidResult<Message> result = VoidResult.upCast(failed);
            assertThat(result).hasFailed();
            assertThat(result.error()).isExactlyInstanceOf(SubMessage.class);
        }
    }

    @Nested
    @DisplayName("sequence()")
    class sequence {
        @Test
        @DisplayName("WHEN given an empty list of Results THEN sequence will return a successful VoidResult with an empty error list")
        void withEmptyList() {
            VoidResult<Collection<String>> result = VoidResult.sequence(emptyList());
            assertThat(result).isSuccessful();
        }

        @Test
        @DisplayName("WHEN given two successful Results THEN sequence will return a successful VoidResult with an empty error list")
        void withTwoSuccessfulResults() {
            VoidResult<Collection<String>> result = VoidResult.sequence(asList(VoidResult.ok(), VoidResult.ok()));
            assertThat(result).isSuccessful();
        }

        @Test
        @DisplayName("WHEN given one successful and two failed Results THEN sequence will return a failed VoidResult with a list containing two errors")
        void withOneSuccessfulAndTwoFailedResults() {
            VoidResult<Collection<String>> result = VoidResult.sequence(asList(
                    VoidResult.fail("err1"), VoidResult.ok(), VoidResult.fail("err2")
            ));
            assertThat(result).hasFailed();
            assertThat(result.error()).containsExactly("err1", "err2");
        }
    }
}
