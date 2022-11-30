package io.github.sanctum.clans.api.model;

import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.RegExp;

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
}
