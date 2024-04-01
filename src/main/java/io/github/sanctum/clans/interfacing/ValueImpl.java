package io.github.sanctum.clans.interfacing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @since 1.6.1
 * @author ms5984
 * @param <T> the value type
 */
abstract class ValueImpl<T> implements Value<T> {
    T value;

    ValueImpl(T value) {
        this.value = value;
    }

    static final class RequiredImpl<T> extends ValueImpl<T> implements Required<T> {
        RequiredImpl(@NotNull T value) {
            super(value);
        }

        @Override
        public @NotNull T get() {
            return value;
        }

        @Override
        public @NotNull OrNull<T> toNullable() {
            return Value.optional(value);
        }

        @Override
        public String toString() {
            return "ValueImpl.RequiredImpl{" +
                    "value=" + value +
                    '}';
        }
    }

    static final class OrNullImpl<T> extends ValueImpl<T> implements OrNull<T> {
        OrNullImpl(@Nullable T value) {
            super(value);
        }

        @Override
        public @Nullable T get() {
            return value;
        }

        @Override
        public @Nullable Required<T> toNotNull() {
            return (value != null) ? Value.of(value) : null;
        }

        @Override
        public String toString() {
            return "ValueImpl.OrNullImpl{" +
                    "value=" + value +
                    '}';
        }
    }

    @Override
    public boolean isSimilar(@NotNull Value<?> wrapper) {
        if (wrapper instanceof ValueImpl) {
            return Objects.equals(value, ((ValueImpl<?>) wrapper).value);
        }
        return Objects.equals(value, wrapper.get());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueImpl<?> value1 = (ValueImpl<?>) o;
        return Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
        return (value != null) ? value.hashCode() : 0;
    }
}
