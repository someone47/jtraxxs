package com.iremembr.jtraxxs;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.Objects;

class RailwayAssertions extends Assertions {

    static <V, E> ValueResultAssert<V, E> assertThat(ValueResult<V, E> actual) {
        return new ValueResultAssert<>(actual);
    }

    static <E> VoidResultAssert<E> assertThat(VoidResult<E> actual) {
        return new VoidResultAssert<>(actual);
    }

    static class VoidResultAssert<E> extends AbstractAssert<VoidResultAssert<E>, VoidResult<E>> {

        VoidResultAssert(VoidResult<E> actual) {
            super(actual, VoidResultAssert.class);
        }

        VoidResultAssert<E> isSuccessful() {
            isNotNull();
            if (!actual.isSuccessful()) {
                failWithMessage("Expected a successful ValueResult but it failed");
            }
            return this;
        }

        VoidResultAssert<E> hasFailed() {
            isNotNull();
            if (!actual.hasFailed()) {
                failWithMessage("Expected a failed ValueResult but it is successful");
            }
            return this;
        }

        VoidResultAssert<E> withError(E error) {
            isNotNull();
            hasFailed();
            if (!Objects.equals(actual.error(), error)) {
                failWithMessage("Expected ValueResult's error to be <%s> but was <%s>", error, actual.error());
            }
            return this;
        }
    }

    static class ValueResultAssert<V, E> extends AbstractAssert<ValueResultAssert<V, E>, ValueResult<V, E>> {

        ValueResultAssert(ValueResult<V, E> actual) {
            super(actual, ValueResultAssert.class);
        }

        ValueResultAssert<V, E> isSuccessful() {
            isNotNull();
            if (!actual.isSuccessful()) {
                failWithMessage("Expected a successful ValueResult but it failed");
            }
            return this;
        }

        ValueResultAssert<V, E> hasFailed() {
            isNotNull();
            if (!actual.hasFailed()) {
                failWithMessage("Expected a failed ValueResult but it is successful");
            }
            return this;
        }

        ValueResultAssert<V, E> withValue(V value) {
            isNotNull();
            isSuccessful();
            if (!Objects.equals(actual.value(), value)) {
                failWithMessage("Expected ValueResult's value to be <%s> but was <%s>", value, actual.value());
            }
            return this;
        }

        ValueResultAssert<V, E> hasNoValue() {
            isNotNull();
            isSuccessful();
            try {
                V value = actual.value();
                failWithMessage("Expected ValueResult to have no value but was <%s>", value);
            } catch (IllegalStateException e) {
                // EmptyResultHasNoValueException will be thrown
                // when ValueResult has no value
            }
            return this;
        }

        ValueResultAssert<V, E> withError(E error) {
            isNotNull();
            hasFailed();
            if (!Objects.equals(actual.error(), error)) {
                failWithMessage("Expected ValueResult's error to be <%s> but was <%s>", error, actual.error());
            }
            return this;
        }
    }
}
