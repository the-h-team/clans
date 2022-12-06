package io.github.sanctum.clans.api.action.prototypes;

import io.github.sanctum.clans.api.model.Clan;
import org.jetbrains.annotations.NotNull;

/**
 * An action that requires the context of two clans.
 *
 * @since 3.0.0
 * @author ms5984
 */
public interface HasAnotherClanContext extends HasClanContext {
    /**
     * Gets the other context clan of this action.
     *
     * @return the tag of the other clan
     */
    default @NotNull @Clan.Tag String getOtherClan() {
        //noinspection PatternValidation
        return (String) getArgs().get("other-clan");
    }

    /**
     * Sets the other context clan of this action.
     *
     * @param otherTag the tag of the other clan
     * @throws IllegalArgumentException if {@code otherTag} format invalid
     * @throws IllegalStateException if the context cannot be updated
     * @implSpec It is not required for implementations to support this
     * method. See throws declaration for suitable response.
     */
    default void setOtherClan(@NotNull @Clan.Tag String otherTag) throws IllegalArgumentException, IllegalStateException {
        throw new IllegalStateException("Cannot update other-clan context", new UnsupportedOperationException());
    }
}
