package io.github.sanctum.clans.api.action.actions.clan.attributes.flags;

import io.github.sanctum.clans.api.action.ApiAction;
import io.github.sanctum.clans.api.action.prototypes.HasClanContext;
import io.github.sanctum.clans.api.model.Clan;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * API for updating flag states.
 *
 * @since 3.0.0
 * @author ms5984
 */
@ApiStatus.NonExtendable
public interface UpdateFlagState extends HasClanContext {
    /**
     * Get the flag type to be modified.
     *
     * @return the flag type
     */
    default @NotNull Clan.Flag getFlag() {
        return (Clan.Flag) getArgs().get("flag");
    }

    /**
     * Set the flag type to be modified.
     *
     * @param flag a flag type
     */
    void setFlag(@NotNull Clan.Flag flag);

    /**
     * Get the new flag state.
     *
     * @return the new flag state or null if no change should be made
     */
    default @Nullable Boolean getNewState() {
        return (Boolean) getArgs().get("new-state");
    }

    /**
     * Set the new flag state.
     *
     * @param state new flag state or null if no change should be made
     */
    void setNewState(@Nullable Boolean state);

    /**
     * The result of {@link UpdateFlagState}.
     *
     * @since 3.0.0
     */
    @ApiStatus.NonExtendable
    interface Result extends ApiAction.Result<UpdateFlagState>, HasClanContext {
        /**
         * Get the flag type.
         *
         * @return the flag type
         */
        default @NotNull Clan.Flag getFlag() {
            return (Clan.Flag) getResults().get("flag");
        }

        /**
         * Get the old flag state.
         *
         * @return true or false
         */
        default boolean getOldState() {
            return (Boolean) getResults().get("old-state");
        }

        /**
         * Get the new flag state.
         *
         * @return true or false
         */
        default boolean getNewState() {
            return (Boolean) getResults().get("new-state");
        }

        /**
         * Evaluate if the flag state has changed.
         *
         * @return true if the flag state changed
         */
        default boolean hasChanged() {
            return getOldState() != getNewState();
        }
    }
}
