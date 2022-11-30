package io.github.sanctum.clans.api.action.actions.associate;

import io.github.sanctum.clans.api.action.ApiAction;
import io.github.sanctum.clans.api.action.prototypes.HasAssociateContext;
import io.github.sanctum.clans.api.model.Clan;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * API for updating an associate's clan.
 *
 * @since 3.0.0
 * @author ms5984
 */
public interface UpdateAssociateClan extends HasAssociateContext {
    /**
     * Represents an update type.
     *
     * @since 3.0.0
     */
    enum UpdateType {
        /**
         * The associate is joining a clan.
         * <p>
         * Current clan should be null; new clan should be non-null.
         */
        JOIN,
        /**
         * The associate is leaving a clan.
         * <p>
         * Current clan should be non-null.
         */
        LEAVE,
        /**
         * The associate is being kicked from a clan.
         * <p>
         * Current clan should be non-null.
         */
        KICK,
        /**
         * The associate is being removed from a clan administratively.
         * <p>
         * New clan should be null.
         */
        REMOVE,
        /**
         * The associate is being added to a clan administratively.
         * <p>
         * New clan should be non-null.
         */
        SET,
    }

    /**
     * Get the update type.
     *
     * @return the update type
     */
    default @NotNull UpdateType getType() {
        return (UpdateType) getArgs().get("type");
    }

    /**
     * Set the update type.
     *
     * @param type the update type
     */
    void setType(@NotNull UpdateType type);

    /**
     * Get the new clan of the associate, if any.
     *
     * @return a new clan or null if none
     */
    default @Nullable @Clan.Tag String getNewClan() {
        //noinspection PatternValidation
        return (String) getArgs().get("new-clan");
    }

    /**
     * Set the new clan of the associate.
     *
     * @param clan a new clan or null if none
     */
    void setNewClan(@Nullable @Clan.Tag String clan);

    /**
     * The result of {@link UpdateAssociateClan}.
     *
     * @since 3.0.0
     */
    interface Result extends ApiAction.Result<UpdateAssociateClan>, HasAssociateContext {
        /**
         * Get the update type.
         *
         * @return the update type
         */
        default @NotNull UpdateType getType() {
            return (UpdateType) getResults().get("type");
        }

        /**
         * Get the previous clan of the associate, if any.
         *
         * @return the old clan or null if none
         */
        default @Nullable @Clan.Tag String getOldClan() {
            //noinspection PatternValidation
            return (String) getResults().get("old-clan");
        }

        /**
         * Get the new clan of the associate, if any.
         *
         * @return the new clan of the associate or null if none
         */
        default @Nullable @Clan.Tag String getNewClan() {
            //noinspection PatternValidation
            return (String) getResults().get("new-clan");
        }
    }
}
