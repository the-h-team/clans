package io.github.sanctum.clans.api.action;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Evaluates API actions.
 *
 * @since 3.0.0
 * @author ms5984
 */
public interface ActionRunner {
    /**
     * Run an API action.
     *
     * @param action the action type to evaluate
     * @return an action result future
     */
    @NotNull <T extends ApiAction> CompletableFuture<ApiAction.Result<T>> getActionResult(@NotNull Class<T> action);

    /**
     * Run an API action.
     *
     * @param action the action type to evaluate
     * @param processing a process step to perform on the action
     * @return an action result future
     */
    @NotNull <T extends ApiAction> CompletableFuture<ApiAction.Result<T>> getActionResult(@NotNull Class<T> action, @NotNull ApiAction.ProcessStep<T> processing);
}
