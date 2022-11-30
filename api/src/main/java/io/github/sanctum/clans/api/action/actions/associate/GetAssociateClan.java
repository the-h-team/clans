package io.github.sanctum.clans.api.action.actions.associate;

import io.github.sanctum.clans.api.action.ApiAction;
import io.github.sanctum.clans.api.action.prototypes.HasAssociateContext;
import io.github.sanctum.clans.api.model.Clan;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * API for getting an associate's clan.
 *
 * @since 3.0.0
 * @author ms5984
 */
@ApiStatus.NonExtendable
public interface GetAssociateClan extends HasAssociateContext {
    /**
     * The result of {@link GetAssociateClan}.
     *
     * @since 3.0.0
     */
    @ApiStatus.NonExtendable
    interface Result extends ApiAction.Result<GetAssociateClan>, HasAssociateContext {
        /**
         * Get the associate's clan, if any.
         *
         * @return a clan tag or null if associate not in a clan
         */
        default @Nullable @Clan.Tag String getClan() {
            //noinspection PatternValidation
            return (String) getResults().get("clan");
        }
    }
}
