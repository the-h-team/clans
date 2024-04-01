package io.github.sanctum.clans.interfacing;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Represents a property value.
 *
 * @since 1.6.1
 * @author ms5984
 * @param <T> the property value type
 */
@ApiStatus.NonExtendable
public interface Value<T> extends Supplier<T> {
    /**
     * Represents a value for a property which requires a value.
     *
     * @param <T> the property value type
     * @see #of(Object)
     */
    @ApiStatus.NonExtendable
    interface Required<T> extends Value<T> {
        @Override
        @NotNull T get();

        @Override
        default @NotNull Required<T> toNotNull() {
            return this;
        }
    }

    /**
     * Represents a value for a property that accepts nulls.
     *
     * @param <T> the property value type
     * @see #optional(Object)
     * @see #empty()
     */
    @ApiStatus.NonExtendable
    interface OrNull<T> extends Value<T> {
        @Override
        @Nullable T get();

        @Override
        default @NotNull OrNull<T> toNullable() {
            return this;
        }
    }

    /**
     * Coaxes this value wrapper to one that does not accept nulls.
     * <p>
     * If this wrapper does not have a value, this method returns {@code null}.
     *
     * @return a null-resistant wrapper or null
     */
    @Nullable Value.Required<T> toNotNull();

    /**
     * Gets a property value wrapper that accepts nulls.
     * <p>
     * This method may return this object if it already accepts null.
     *
     * @return a null-tolerant wrapper
     */
    @NotNull Value.OrNull<T> toNullable();

    /**
     * Checks if the value of this wrapper equals that of another wrapper.
     * <p>
     * This method ignores differences in wrapper type ({@linkplain Required}
     * and {@linkplain OrNull}).
     *
     * @param wrapper another wrapper
     * @return true if the values are the same
     */
    boolean isSimilar(@NotNull Value<?> wrapper);

    /**
     * Wraps a nonnull property value.
     *
     * @param value a value
     * @return a property value wrapper
     */
    static <T> Value.Required<T> of(@NotNull T value) {
        return new ValueImpl.RequiredImpl<>(value);
    }

    /**
     * Wraps a value for a property that supports nulls.
     *
     * @param value a value or null
     * @return a property value wrapper
     */
    static <T> Value.OrNull<T> optional(@Nullable T value) {
        return new ValueImpl.OrNullImpl<>(value);
    }

    /**
     * Represents an empty value.
     *
     * @return a property value wrapper
     */
    static <R> Value.OrNull<R> empty() {
        return optional(null);
    }
}
