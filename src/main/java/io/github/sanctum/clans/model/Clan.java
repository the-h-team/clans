package io.github.sanctum.clans.model;

import com.github.sanctum.panther.util.HUID;
import io.github.sanctum.clans.interfacing.*;
import io.github.sanctum.clans.model.association.*;
import io.github.sanctum.clans.model.combat.FriendlyFireScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Access clan data (a DAO, save for {@linkplain #getId()}).
 *
 * @since 1.6.1
 * @author ms5984
 */
public interface Clan extends MutableEntity, CanUseAlias, CanDescribe, Grouping, FriendlyFireScope {
    /**
     * Gets the id of the clan.
     * <p>
     * Clan ids are stable, constant values.
     *
     * @return the id of this clan
     */
    @NotNull HUID getId();

    /**
     * Gets clan tag, or the name of the clan.
     *
     * @return the name of this clan
     */
    @NotNull String getTag();

    /**
     * Gets the display name of the clan.
     *
     * @return the display name of this clan or null
     */
    @Override
    @Nullable String getAlias();

    // Move Clan.Color for getPalette

    /**
     * Gets the description of the clan.
     *
     * @return the description of this clan or null
     */
    @Override
    @Nullable String getDescription();

    /**
     * Gets the join password of the clan.
     *
     * @return the password of this clan or null
     */
    @Nullable String getJoinPassword();

    // getOwner - Associate
    // getBase - Location util
    // getPermissiveHandle - obj (calculated, relational permission data)
    // resetPermissions - clears permission data

    /**
     * Gets the power amount of the clan.
     *
     * @return the power of this clan
     */
    double getPower();

    // newAssociate factories (UUID, InvasiveEntity, LabyrinthUser)
    // getCooldown(String) -> getCooldowns (map view)

    // FIXME pull to new interface
    // R? getValue(Class<R>,String)
    // R? getValue(TypeAdapter<R>,String)
    // R? getValue(String)
    // R setValue(String,R,boolean)

    // isValid

    /**
     * Gets the pvp mode of the clan.
     * <p>
     * Clans can be peaceful or warring.
     * <p>
     * This flag controls whether a clan may participate in clan wars //FIXME add linkplain
     * . A state of true prevents participation; false allows participation.
     *
     * @return true if this clan is peaceful, false if warring
     */
    boolean isPeaceful();

    /**
     * Gets the friendly fire state of the clan.
     * <p>
     * This flag controls whether associates in the clan are able to damage
     * each other by default. A state of true allows damage; false disallows.
     *
     * @return true if this clan allows friendly fire
     */
    @Override
    boolean isFriendlyFire();

    /**
     * A clan editing utility.
     *
     * @since 1.6.1
     */
    interface Edits extends CanUseAlias.Edits, CanDescribe.Edits, FriendlyFireScope.Edits {
        @Override
        @NotNull Clan getMutating();

        /**
         * Sets the new join password for the clan.
         * <p>
         * Use {@code null} to clear.
         *
         * @param newPassword a new password or null
         * @return this edit util
         * @see #getJoinPassword()
         */
        @NotNull Clan.Edits setJoinPassword(@Nullable String newPassword);
        // TODO more set methods

        /**
         * Transfers ownership of the clan to a specified member.
         * <p>
         * If also a player, the provided entity must be a member of the clan.
         *
         * @param target an entity
         * @return this edit util
         */
        @NotNull Clan.Edits transferOwnership(@NotNull Entity target);

        /**
         * Changes the clan tag (the name of the clan).
         * <p>
         * Generally, clan tags must not have spaces or special characters.
         * However, this method does not perform such validation. It is up
         * to the underlying implementation when the edit is applied.
         *
         * @param tag a new tag
         * @return this edit util
         * @see #getTag()
         */
        @NotNull Clan.Edits setTag(@NotNull String tag);

        /**
         * Changes the pvp mode of the clan.
         *
         * @param peaceful a peaceful state
         * @return this edit util
         * @see #isPeaceful()
         */
        @NotNull Clan.Edits setPeaceful(boolean peaceful);
    }

    /**
     * A staged clan info update.
     *
     * @since 1.6.1
     */
    interface Update extends StagedUpdate, CanUseAlias.Update, CanDescribe.Update, FriendlyFireScope.Update {
        @Override
        @NotNull Clan getReferenceObject();

        // TODO more getProposed methods
    }
}
