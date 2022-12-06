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
     * Runs an API action.
     *
     * @param result the results class
     * @param input steps to prepare the action
     * @return an action result future
     */
    @NotNull <R extends ApiAction.Result<A>, A extends ApiAction> CompletableFuture<R> run(@NotNull Class<R> result, @NotNull ProcessStep<A> input);
}
