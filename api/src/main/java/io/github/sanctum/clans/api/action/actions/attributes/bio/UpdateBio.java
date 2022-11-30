package io.github.sanctum.clans.api.action.actions.attributes.bio;

import io.github.sanctum.clans.api.action.ApiAction;
import io.github.sanctum.clans.api.action.prototypes.HasAssociateContext;
import io.github.sanctum.clans.api.action.prototypes.HasClanContext;
import io.github.sanctum.clans.api.model.Bio;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * API for updating a bio.
 *
 * @since 3.0.0
 * @author ms5984
 */
@ApiStatus.NonExtendable
public interface UpdateBio extends ApiAction {
    /**
     * Get the new bio of this object.
     *
     * @return the new bio or null if none
     */
    default @Nullable @Bio.Format String getNewBio() {
        //noinspection PatternValidation
        return (String) getArgs().get("new-bio");
    }

    /**
     * Set the new bio of this object.
     *
     * @param newBio new bio or null for none
     */
    void setNewBio(@Nullable @Bio.Format String newBio);

    // TODO add result api

    /**
     * API for updating a clan's bio.
     *
     * @since 3.0.0
     */
    @ApiStatus.NonExtendable
    interface OfClan extends UpdateBio, HasClanContext {}

    /**
     * API for updating an associate's bio.
     *
     * @since 3.0.0
     */
    @ApiStatus.NonExtendable
    interface OfAssociate extends UpdateBio, HasAssociateContext {}
}
