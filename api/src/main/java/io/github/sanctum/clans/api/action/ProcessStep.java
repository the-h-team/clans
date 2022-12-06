package io.github.sanctum.clans.api.action;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents intermediate operations on an action.
 *
 * @since 3.0.0
 * @param <T> the action type
 */
@ApiStatus.NonExtendable
@FunctionalInterface
interface ProcessStep<T extends ApiAction> {
    /**
     * Processes an API action.
     *
     * @param action an action for intermediate processing
     */
    void process(@NotNull T action);

    /**
     * Chains a process step to execute after this one.
     *
     * @param another another process step
     * @return a process step chain
     */
    default ProcessStep<T> then(@NotNull ProcessStep<T> another) {
        return action -> {
            process(action);
            another.process(action);
        };
    }

    /**
     * Creates a process step that does nothing.
     *
     * @return a no-op process step
     */
    static <T extends ApiAction> ProcessStep<T> doNothing() {
        return t -> {};
    }
}
