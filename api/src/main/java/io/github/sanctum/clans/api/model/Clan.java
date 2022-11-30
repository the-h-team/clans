package io.github.sanctum.clans.api.model;

import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.RegExp;

/**
 * Represents the structures of a clan.
 *
 * @since 3.0.0
 * @author ms5984
 */
public interface Clan {
    /**
     * The required format of a clan tag.
     * <p>
     * Tags may only contain letters, numbers, underscores and hyphens; they
     * must not begin or end with a hyphen and must not be an empty string.
     */
    @RegExp String TAG_FORMAT = "^\\w(?:[\\w-]*\\w)?$";

    /**
     * The required format of a clan password.
     * <p>
     * In general, clan passwords may contain any non-whitespace character.
     * Additionally, they must not be an empty string.
     */
    @RegExp String PASSWORD_FORMAT = "^\\S+$";

    /**
     * Meta-annotation which marks a clan tag String representation.
     * <p>
     * Tags are used to identify clans in chat and other contexts.
     */
    @Pattern(TAG_FORMAT)
    @interface Tag {}

    /**
     * Meta-annotation which marks a clan password.
     * <p>
     * Passwords are used to restrict access to a clan.
     */
    @Pattern(PASSWORD_FORMAT)
    @interface Password {}

    /**
     * Represents boolean settings for a clan.
     *
     * @since 3.0.0
     */
    enum Flag {
        /**
         * True the clan is in war mode; false in peacetime.
         */
        WAR_MODE,
        /**
         * True if friendly fire is allowed; false if not.
         */
        FRIENDLY_FIRE,
    }

    /**
     * Represents a clan's relationship with another clan.
     *
     * @since 3.0.0
     */
    enum Stance {
        /**
         * The clan is neutral with another clan.
         * <p>
         * This is the default stance.
         */
        NEUTRAL,
        /**
         * The clan is allied with another clan.
         */
        ALLY,
        /**
         * The clan is enemies with another clan.
         */
        ENEMY,
    }
}
