package io.github.sanctum.clans.api.action.prototypes;

import io.github.sanctum.clans.api.action.ApiAction;
import io.github.sanctum.clans.api.model.Associate;
import org.jetbrains.annotations.NotNull;

/**
 * An action that requires an associate context.
 *
 * @since 3.0.0
 * @author ms5984
 */
public interface HasAssociateContext extends ApiAction {
    /**
     * Gets the context associate of this action.
     *
     * @return the context associate's name
     */
    default @NotNull @Associate.Name String getAssociate() {
        //noinspection PatternValidation
        return (String) getArgs().get("associate");
    }

    /**
     * Sets the context associate of this action.
     *
     * @param name the context associate's name
     * @throws IllegalArgumentException if {@code associate} format invalid
     * @throws IllegalStateException if the context cannot be updated
     * @implSpec It is not required for implementations to support this
     * method. See throws declaration for suitable response.
     */
    default void setAssociate(@NotNull @Associate.Name String name) throws IllegalArgumentException, IllegalStateException {
        throw new IllegalStateException("Cannot update associate context", new UnsupportedOperationException());
    }
}
