package io.github.sanctum.clans.api.action.actions.attributes.bio;

import io.github.sanctum.clans.api.action.ApiAction;
import io.github.sanctum.clans.api.action.prototypes.HasAssociateContext;
import io.github.sanctum.clans.api.action.prototypes.HasClanContext;
import io.github.sanctum.clans.api.model.Bio;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * APIs for getting a bio.
 *
 * @since 3.0.0
 * @author ms5984
 */
@ApiStatus.NonExtendable
public interface GetBio extends ApiAction {
    /**
     * The result of {@link GetBio}.
     *
     * @since 3.0.0
     */
    @ApiStatus.NonExtendable
    interface Result<A extends GetBio> extends ApiAction.Result<A> {
        /**
         * Get the bio.
         *
         * @return the bio or null if not set
         */
        default @Nullable @Bio.Format String getBio() {
            //noinspection PatternValidation
            return (String) getResults().get("bio");
        }
    }

    /**
     * API for getting a clan's bio.
     *
     * @since 3.0.0
     */
    @ApiStatus.NonExtendable
    interface OfClan extends GetBio, HasClanContext {
        /**
         * The result of {@link OfClan}.
         *
         * @since 3.0.0
         */
        @ApiStatus.NonExtendable
        interface Result extends GetBio.Result<OfClan>, HasClanContext {}
    }

    /**
     * API for getting an associate's bio.
     *
     * @since 3.0.0
     */
    @ApiStatus.NonExtendable
    interface OfAssociate extends GetBio, HasAssociateContext {
        /**
         * The result of {@link OfAssociate}.
         *
         * @since 3.0.0
         */
        @ApiStatus.NonExtendable
        interface Result extends GetBio.Result<OfAssociate>, HasAssociateContext {}
    }
}
