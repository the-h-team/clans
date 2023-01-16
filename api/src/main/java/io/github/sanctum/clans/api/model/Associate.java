package io.github.sanctum.clans.api.model;

import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a member of a clan.
 *
 * @since 3.0.0
 * @author ms5984
 */
public interface Associate {
    /**
     * The required format of an associate's name.
     * <p>
     * Names may only contain letters, numbers, underscores and hyphens; they
     * must not begin or end with a hyphen and must not be an empty string.
     */
    @RegExp String NAME_FORMAT = "^\\w(?:[\\w-]*\\w)?$";

    /**
     * Meta-annotation which marks an associate's name.
     * <p>
     * Names are used to identify associates in chat and other contexts.
     */
    @Pattern(NAME_FORMAT)
    @interface Name {}

    /**
     * Represents different types of membership updating.
     *
     * @since 3.0.0
     */
    enum MemberUpdateType {
        /**
         * The associate is joining a clan.
         */
        JOIN,
        /**
         * The associate is leaving a clan.
         */
        LEAVE,
        /**
         * The associate is being kicked from a clan.
         */
        KICK,
        /**
         * The associate is being removed from a clan administratively.
         */
        REMOVE,
        /**
         * The associate is being added to a clan administratively.
         */
        SET,
    }

    /**
     * Gets the name of this associate.
     *
     * @return the name of this associate
     */
    @NotNull @Associate.Name String getName();

    /**
     * Gets the clan of which this associate is a member, if any.
     *
     * @return the clan this associate is in or null
     */
    @Nullable Clan getClan();
}
