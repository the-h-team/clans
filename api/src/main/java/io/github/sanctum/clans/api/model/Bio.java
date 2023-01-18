package io.github.sanctum.clans.api.model;

import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Documented;

/**
 * The format of a bio.
 *
 * @since 3.0.0
 * @author ms5984
 */
public interface Bio {
    /**
     * The required format of a bio (if defined).
     * <p>
     * In general, bios may contain any character.
     * Additionally, they must not be an empty string.
     */
    @RegExp String BIO_FORMAT = ".+";

    /**
     * Meta-annotation which marks a bio.
     */
    @Documented
    @Pattern(BIO_FORMAT)
    @interface Format {}

    /**
     * An object that can have a bio.
     *
     * @since 3.0.0
     */
    interface Target {
        /**
         * Gets the bio of this object if one is set.
         *
         * @return a bio or null
         */
        @Nullable @Bio.Format String getBio();

        /**
         * An object that can update its bio.
         *
         * @since 3.0.0
         */
        interface Mutable extends Target {
            /**
             * Sets the bio of this object.
             * <p>
             * If the {@code bio} is null the bio is removed.
             *
             * @param bio a new bio or null
             */
            void setBio(@Nullable @Bio.Format String bio);
        }
    }
}
