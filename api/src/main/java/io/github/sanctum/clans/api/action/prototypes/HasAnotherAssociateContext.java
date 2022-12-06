package io.github.sanctum.clans.api.action.prototypes;

import io.github.sanctum.clans.api.model.Associate;
import org.jetbrains.annotations.NotNull;

/**
 * An action that requires the context of two associates.
 *
 * @since 3.0.0
 * @author ms5984
 */
public interface HasAnotherAssociateContext extends HasAssociateContext {
    /**
     * Gets the other context associate of this action.
     *
     * @return the other associate's name
     */
    default @NotNull @Associate.Name String getOtherAssociate() {
        //noinspection PatternValidation
        return (String) getArgs().get("other-associate");
    }

    /**
     * Sets the other context associate of this action.
     *
     * @param otherName the other associate's name
     * @throws IllegalArgumentException if {@code otherName} format invalid
     * @throws IllegalStateException if the context cannot be updated
     * @implSpec It is not required for implementations to support this
     * method. See throws declaration for suitable response.
     */
    default void setOtherAssociate(@NotNull @Associate.Name String otherName) throws IllegalArgumentException, IllegalStateException {
        throw new IllegalStateException("Cannot update other-associate context", new UnsupportedOperationException());
    }
}
