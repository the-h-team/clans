package io.github.sanctum.clans.api.action.actions.clan.attributes;

import io.github.sanctum.clans.api.action.ApiAction;
import io.github.sanctum.clans.api.action.prototypes.HasClanContext;
import io.github.sanctum.clans.api.model.Clan;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Actions for join password.
 *
 * @since 3.0.0
 * @see Clan.Password
 * @author ms5984
 */
@ApiStatus.NonExtendable
public interface JoinPassword {
    /**
     * API for getting join password.
     *
     * @since 3.0.0
     */
    @ApiStatus.NonExtendable
    interface Get extends ApiAction.Result<Get.Args> {
        /**
         * Gets the join password, if defined.
         *
         * @return the password or null if not set
         */
        @Nullable @Clan.Password String getJoinPassword();

        /**
         * Arguments for {@link Get}.
         *
         * @implSpec {@link #setClan(String)} must be supported
         * @since 3.0.0
         */
        @ApiStatus.NonExtendable
        interface Args extends HasClanContext {}
    }

    /**
     * API for updating join password.
     *
     * @since 3.0.0
     */
    @ApiStatus.NonExtendable
    interface Update extends ApiAction.Result<Update.Args> {
        /**
         * Gets the old join password, if it was defined.
         *
         * @return the old password or null if it was not set
         */
        default @Nullable @Clan.Password String getOldJoinPassword() {
            //noinspection PatternValidation
            return (String) getResults().get("old-password");
        }

        /**
         * Gets the new join password.
         *
         * @return the new password or null if it was unset
         */
        default @Nullable @Clan.Password String getNewJoinPassword() {
            //noinspection PatternValidation
            return (String) getResults().get("new-password");
        }

        /**
         * Arguments for {@link Update}.
         *
         * @implSpec {@link #setClan(String)} must be supported
         * @since 3.0.0
         */
        @ApiStatus.NonExtendable
        interface Args extends HasClanContext {
            /**
             * Gets the new join password.
             *
             * @return the new password or null if it was unset
             */
            default @Nullable @Clan.Password String getNewJoinPassword() {
                //noinspection PatternValidation
                return (String) getArgs().get("new-password");
            }

            /**
             * Set the new join password.
             *
             * @param password the new password or null to unset
             */
            void setNewJoinPassword(@Nullable @Clan.Password String password);
        }
    }
}
