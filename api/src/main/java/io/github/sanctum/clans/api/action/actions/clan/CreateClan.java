package io.github.sanctum.clans.api.action.actions.clan;

import io.github.sanctum.clans.api.action.ApiAction;
import io.github.sanctum.clans.api.action.prototypes.HasClanContext;
import io.github.sanctum.clans.api.model.Clan;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * API for creating a clan.
 *
 * @since 3.0.0
 * @author ms5984
 */
@ApiStatus.NonExtendable
public interface CreateClan extends ApiAction {
    /**
     * Get the tag of the clan to be created.
     *
     * @return a clan tag
     */
    default @NotNull @Clan.Tag String getTag() {
        //noinspection PatternValidation
        return (String) getArgs().get("tag");
    }

    /**
     * Set the tag of the clan to be created.
     *
     * @param tag a clan tag
     * @throws IllegalArgumentException if {@code tag} format invalid
     * @throws IllegalStateException if the clan tag cannot be updated
     * @implSpec It is not required for implementations to support this
     * method. See throws declaration for suitable response.
     */
    default void setTag(@NotNull @Clan.Tag String tag) throws IllegalArgumentException, IllegalStateException {
        throw new IllegalStateException("Cannot update tag", new UnsupportedOperationException());
    }

    /**
     * Get the password that will be required for those joining the clan.
     * <p>
     * It is not required to set a password. The default response is null.
     *
     * @return the password or null if no password is required
     */
    default @Nullable @Clan.Password String getPassword() {
        //noinspection PatternValidation
        return (String) getArgs().get("password");
    }

    /**
     * Set the password that will be required for those joining the clan.
     *
     * @param password the password or null if no password is desired
     */
    void setPassword(@Nullable @Clan.Password String password);

    /**
     * The result of {@link CreateClan}.
     *
     * @since 3.0.0
     */
    interface Result extends ApiAction.Result<CreateClan>, HasClanContext {
        /**
         * Get the password (if any) required by those joining the clan.
         *
         * @return the password or null if no password is required
         */
        default @Nullable @Clan.Password String getPassword() {
            //noinspection PatternValidation
            return (String) getResults().get("password");
        }
    }
}
