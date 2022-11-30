package io.github.sanctum.clans.api.action.actions.clan.attributes.stance;

import io.github.sanctum.clans.api.action.ApiAction;
import io.github.sanctum.clans.api.action.prototypes.HasAnotherClanContext;
import io.github.sanctum.clans.api.model.Clan;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * API for modifying stance in relation to another clan.
 * <p>
 * This is from the perspective of the context clan ({@link #getClan()})!
 *
 * @since 3.0.0
 * @author ms5984
 */
@ApiStatus.NonExtendable
public interface UpdateStance extends HasAnotherClanContext {
    /**
     * Get the new stance of the context clan in relation to the other clan.
     *
     * @return the new stance or null if no change should be made
     */
    default @Nullable Clan.Stance getNewStance() {
        return (Clan.Stance) getArgs().get("new-stance");
    }

    /**
     * Set the new stance of the context clan in relation to the other clan.
     *
     * @param newStance the desired stance or null if no change should be made
     */
    void setNewStance(@Nullable Clan.Stance newStance);

    /**
     * The result of {@link UpdateStance}.
     *
     * @since 3.0.0
     */
    @ApiStatus.NonExtendable
    interface Result extends ApiAction.Result<UpdateStance>, HasAnotherClanContext {
        /**
         * Get the previous stance of the context clan in relation to the other clan.
         *
         * @return the previous stance
         */
        default @NotNull Clan.Stance getOldStance() {
            return Objects.requireNonNull((Clan.Stance) getArgs().get("old-stance"));
        }

        /**
         * Get the new stance of the context clan in relation to the other clan.
         *
         * @return the new stance
         */
        default @NotNull Clan.Stance getNewStance() {
            return (Clan.Stance) getResults().get("new-stance");
        }

        /**
         * Evaluate if the stance has changed.
         *
         * @return true if the stance has changed
         */
        default boolean isChanged() {
            return getOldStance() != getNewStance();
        }
    }
}
