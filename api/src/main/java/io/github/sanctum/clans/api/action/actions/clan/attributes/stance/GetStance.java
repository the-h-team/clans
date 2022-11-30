package io.github.sanctum.clans.api.action.actions.clan.attributes.stance;

import io.github.sanctum.clans.api.action.ApiAction;
import io.github.sanctum.clans.api.action.prototypes.HasAnotherClanContext;
import io.github.sanctum.clans.api.model.Clan;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * API for getting stance in relation to another clan.
 *
 * @since 3.0.0
 * @author ms5984
 */
@ApiStatus.NonExtendable
public interface GetStance extends HasAnotherClanContext {
    /**
     * The result of {@link GetStance}.
     *
     * @since 3.0.0
     */
    @ApiStatus.NonExtendable
    interface Result extends ApiAction.Result<GetStance>, HasAnotherClanContext {
        /**
         * Get the stance of the context clan in relation to the other clan.
         * <p>
         * This is from the perspective of the context clan ({@link #getClan()})!
         *
         * @return a stance
         */
        default @NotNull Clan.Stance getStance() {
            return Objects.requireNonNull((Clan.Stance) getResults().get("stance"));
        }
    }
}
