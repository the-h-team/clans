package io.github.sanctum.clans.api.action.actions.clan.attributes;

import io.github.sanctum.clans.api.action.ApiAction;
import io.github.sanctum.clans.api.action.prototypes.HasClanContext;
import io.github.sanctum.clans.api.model.Clan;
import org.jetbrains.annotations.NotNull;

/**
 * API for updating a clan's tag.
 * <p>
 * The output of {@link #getClan()} is the new tag.
 *
 * @since 3.0.0
 * @author ms5984
 */
public interface UpdateTag extends ApiAction.Result<UpdateTag.Args>, HasClanContext {
    /**
     * Get the old clan tag.
     *
     * @return the old clan tag
     */
    default @NotNull @Clan.Tag String getOldTag() {
        //noinspection PatternValidation
        return (String) getResults().get("old-tag");
    }

    /**
     * Evaluate whether the tag was updated.
     *
     * @return true if the tag was updated
     */
    default boolean isChanged() {
        return !getClan().equals(getOldTag());
    }

    /**
     * Arguments for {@link UpdateTag}.
     *
     * @implSpec {@link #setClan(String)} must be supported
     * @since 3.0.0
     */
    interface Args extends HasClanContext {
        /**
         * Get the new tag of the clan.
         *
         * @return a new clan tag
         */
        default @NotNull @Clan.Tag String getNewTag() {
            //noinspection PatternValidation
            return (String) getArgs().get("new-tag");
        }

        /**
         * Set the new tag of the clan.
         *
         * @param tag a new clan tag
         */
        void setNewTag(@NotNull @Clan.Tag String tag);
    }
}
