package io.github.sanctum.clans.api.action.actions.clan.attributes.flags;

import io.github.sanctum.clans.api.action.ApiAction;
import io.github.sanctum.clans.api.action.prototypes.HasClanContext;
import io.github.sanctum.clans.api.model.Clan;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * API for getting flag states.
 *
 * @since 3.0.0
 * @author ms5984
 */
@ApiStatus.NonExtendable
public interface GetFlagState extends HasClanContext {
    /**
     * Get the flag type to be queried.
     *
     * @return the flag type
     */
    default @NotNull Clan.Flag getFlag() {
        return (Clan.Flag) getArgs().get("flag");
    }

    /**
     * Set the flag type to be queried.
     *
     * @param flag a flag type
     */
    void setFlag(@NotNull Clan.Flag flag);

    /**
     * The result of {@link GetFlagState}.
     *
     * @since 3.0.0
     */
    @ApiStatus.NonExtendable
    interface Result extends ApiAction.Result<GetFlagState> {
        /**
         * Get the flag type.
         *
         * @return the flag type
         */
        default @NotNull Clan.Flag getFlag() {
            return (Clan.Flag) getResults().get("flag");
        }

        /**
         * Get the flag state.
         * <p>
         * See {@link Clan.Flag} for flag types.
         *
         * @return true or false
         */
        default boolean getState() {
            return (Boolean) getResults().get("state");
        }
    }
}
