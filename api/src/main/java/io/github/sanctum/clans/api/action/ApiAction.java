package io.github.sanctum.clans.api.action;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents an action exposed by public API.
 *
 * @since 3.0.0
 * @author ms5984
 */
@ApiStatus.NonExtendable
public interface ApiAction {
    /**
     * Get the raw arguments map of this action.
     *
     * @return a map of arguments
     */
    @NotNull Map<String, ?> getArgs();

    // Intermediate operations
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
         * Chain a process step to execute after this one.
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
         * Get a process step that does nothing.
         *
         * @return a no-op process step
         */
        static <T extends ApiAction> ProcessStep<T> doNothing() {
            return t -> {};
        }
    }

    // Results
    /**
     * Represents the results of an action exposed by public API.
     * <p>
     * All action results also implement {@link ApiAction} to help encourage
     * chained calls.
     *
     * @since 3.0.0
     * @param <T> the action type
     */
    @ApiStatus.NonExtendable
    interface Result<T extends ApiAction> extends ApiAction {
        /**
         * Get the action which produced this result.
         *
         * @return the action
         */
        @NotNull T getAction();

        /**
         * Get the results of this action.
         *
         * @return a map of results
         */
        @NotNull Map<String, ?> getResults();
    }
}
