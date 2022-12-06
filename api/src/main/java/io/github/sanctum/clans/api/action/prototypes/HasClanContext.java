package io.github.sanctum.clans.api.action.prototypes;

import io.github.sanctum.clans.api.action.ApiAction;
import io.github.sanctum.clans.api.model.Clan;
import org.jetbrains.annotations.NotNull;

/**
 * An action that requires a clan context.
 *
 * @since 3.0.0
 * @author ms5984
 */
public interface HasClanContext extends ApiAction {
    /**
     * Gets the context clan of this action.
     *
     * @return the context clan tag
     */
    default @NotNull @Clan.Tag String getClan() {
        //noinspection PatternValidation
        return (String) getArgs().get("clan");
    }

    /**
     * Sets the context clan of this action.
     *
     * @param tag the context clan tag
     * @throws IllegalArgumentException if {@code tag} format invalid
     * @throws IllegalStateException if the clan context cannot be updated
     * @implSpec It is not required for implementations to support this
     * method. See throws declaration for suitable response.
     */
    default void setClan(@NotNull @Clan.Tag String tag) throws IllegalArgumentException, IllegalStateException {
        throw new IllegalStateException("Cannot update clan context", new UnsupportedOperationException());
    }
}
