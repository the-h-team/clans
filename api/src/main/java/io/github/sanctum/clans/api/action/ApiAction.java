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
     * Gets the raw arguments map of this action.
     *
     * @return a map of arguments
     */
    @NotNull Map<String, Object> getArgs();

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
         * Gets the action which produced this result.
         *
         * @return the action
         */
        @NotNull T getAction();

        /**
         * Gets the results of this action.
         *
         * @return a map of results
         */
        @NotNull Map<String, ?> getResults();
    }
}
