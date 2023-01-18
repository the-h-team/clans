package io.github.sanctum.clans.api.model;

import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Documented;

/**
 * The format of a nickname.
 *
 * @since 3.0.0
 * @author ms5984
 */
public interface Nickname {
    /**
     * The required format of a nickname (if defined).
     * <p>
     * In general, nicknames may contain any character.
     * Additionally, they must not be an empty string.
     */
    @RegExp String NICKNAME_FORMAT = ".+";

    /**
     * Meta-annotation which marks a nickname.
     */
    @Documented
    @Pattern(NICKNAME_FORMAT)
    @interface Format {}

    /**
     * An object that can have a nickname.
     *
     * @since 3.0.0
     */
    interface Target {
        /**
         * Gets the nickname of this object if one is set.
         *
         * @return a nickname or null
         */
        @Nullable @Nickname.Format String getNickname();

        /**
         * An object that can update its nickname.
         *
         * @since 3.0.0
         */
        interface Mutable extends Target {
            /**
             * Sets the nickname of this object.
             * <p>
             * If the {@code nickname} is null the nickname is removed.
             *
             * @param nickname a new nickname or null
             */
            void setNickname(@Nullable @Nickname.Format String nickname);
        }
    }
}
