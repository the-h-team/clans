package io.github.sanctum.clans.api.action.actions.associate;

import io.github.sanctum.clans.api.action.ApiAction;
import io.github.sanctum.clans.api.action.prototypes.HasAssociateContext;
import io.github.sanctum.clans.api.model.Clan;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * API for associate clan data.
 *
 * @since 3.0.0
 * @author ms5984
 */
public interface AssociateClan {
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
     * API for getting an associate's clan.
     *
     * @since 3.0.0
     */
    @ApiStatus.NonExtendable
    interface Get extends ApiAction.Result<Get.Args>, HasAssociateContext {
        /**
         * Get the associate's clan, if any.
         *
         * @return a clan tag or null if associate not in a clan
         */
        default @Nullable @Clan.Tag String getClan() {
            //noinspection PatternValidation
            return (String) getResults().get("clan");
        }

        /**
         * Arguments for {@link Get}.
         *
         * @implSpec {@link #setAssociate(String)} must be supported
         * @since 3.0.0
         */
        @ApiStatus.NonExtendable
        interface Args extends HasAssociateContext {}
    }

    /**
     * API for updating an associate's clan.
     *
     * @since 3.0.0
     */
    @ApiStatus.NonExtendable
    interface Update extends ApiAction.Result<Update>, HasAssociateContext {
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

        /**
         * Arguments for {@link Update}.
         *
         * @implSpec {@link #setAssociate(String)} must be supported
         * @since 3.0.0
         */
        @ApiStatus.NonExtendable
        interface Args extends HasAssociateContext {
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
        }
    }
}
