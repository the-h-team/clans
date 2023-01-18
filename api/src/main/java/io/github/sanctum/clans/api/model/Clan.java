package io.github.sanctum.clans.api.model;

import com.github.sanctum.panther.util.HUID;
import io.github.sanctum.labyrinth.loci.location.WorldPerspective;
import org.intellij.lang.annotations.Pattern;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Documented;

/**
 * Represents the structures of a clan.
 *
 * @since 3.0.0
 * @author ms5984
 */
public interface Clan extends Nickname.Target, Bio.Target {
    /**
     * The required format of a clan tag.
     * <p>
     * Tags may only contain letters, numbers, underscores and hyphens; they
     * must not begin or end with a hyphen and must not be an empty string.
     */
    @RegExp String TAG_FORMAT = "^\\w(?:[\\w-]*\\w)?$";

    /**
     * The required format of a clan password (if defined).
     * <p>
     * In general, clan passwords may contain any non-whitespace character.
     * Additionally, they must not be an empty string.
     */
    @RegExp String PASSWORD_FORMAT = "^\\S+$";

    /**
     * Meta-annotation which marks a clan tag String representation.
     * <p>
     * Tags are used to identify clans in chat and other contexts.
     */
    @Documented
    @Pattern(TAG_FORMAT)
    @interface Tag {}

    /**
     * Meta-annotation which marks a clan password.
     * <p>
     * Passwords are used to restrict access to a clan.
     */
    @Documented
    @Pattern(PASSWORD_FORMAT)
    @interface Password {}

    /**
     * Represents boolean settings for a clan.
     *
     * @since 3.0.0
     */
    enum Flag {
        /**
         * True the clan is in war mode; false in peacetime.
         */
        WAR_MODE,
        /**
         * True if friendly fire is allowed; false if not.
         */
        FRIENDLY_FIRE,
    }

    /**
     * Represents a clan's relationship with another clan.
     *
     * @since 3.0.0
     */
    enum Stance {
        /**
         * The clan is neutral with another clan.
         * <p>
         * This is the default stance.
         */
        NEUTRAL,
        /**
         * The clan is allied with another clan.
         */
        ALLY,
        /**
         * The clan is enemies with another clan.
         */
        ENEMY,
    }

    /**
     * Represents the owner of a clan.
     *
     * @since 3.0.0
     */
    interface Owner {
        // TODO checks + casts
    }

    /**
     * Gets the HUID of this clan.
     *
     * @return the HUID of this clan
     * @see HUID
     */
    @NotNull HUID getId();

    /**
     * Gets the tag of this clan.
     * <p>
     * Tags are used to identify clans in chat and other contexts.
     *
     * @return the tag of this clan
     */
    @NotNull @Clan.Tag String getTag(); // moved from getName

    // see Nickname.Target for Nickname

    // TODO palette

    // see Bio.Target for getBio (moved from getDescription)

    /**
     * Gets the join password of this clan (if any).
     *
     * @return the join password of this clan or null
     */
    @Nullable @Clan.Password String getPassword();

    /**
     * Gets the owner of this clan.
     *
     * @return the owner of this clan
     */
    @NotNull Clan.Owner getOwner();

    /**
     * Gets the location of the clan base.
     *
     * @return the location or null if not set
     */
    @Nullable WorldPerspective getBase();

    // TODO bring clearancelog into api
    // getPermissions

    /**
     * Gets the amount of power held by this clan.
     *
     * @return this clan's power as a double
     */
    double getPower();

    // TODO move associate factories (at least off of instance)

    // TODO add cooldowns to api
    // getCooldown(String)

    // TODO move pdc, rename system
    // getValue(Class, String)
    // getValue(TypeAdapter, String)
    // getValue(String)
    // setValue(String, R, boolean)

    // TODO remove isValid, replace associated checks

    /**
     * Gets the state of a boolean setting flag for this clan.
     *
     * @param flag the flag type
     * @return true or false
     * @see Flag
     */
    boolean getFlag(@NotNull Clan.Flag flag); // replaces isPeaceful (WAR_MODE), isFriendlyFire (FRIENDLY_FIRE)

    // TODO move member management
    // kick(Associate)

    // TODO move chunk owner testing
    // isOwner(Chunk)

    // TODO move ownership transfer
    // transferOwnership(Associate); use Owner.Target instead

    // TODO move cooldown checks
    // isCooldown(String); isOnCooldown

    // TODO move pdc
    // removeValue(String)

    // TODO Move all setters to a mutable sub-interface
    // setTag(String) (moved from setName)
    // setNickname(String)
    // setBio(String) (moved from setDescription)
    // setPassword(String)
    // setColor(String) (remove?)
    // setFlag(Flag: Flag.WAR_MODE, !boolean) (moved from setPeaceful(boolean))
    // setFlag(Flag: Flag.FRIENDLY_FIRE, boolean) (moved from setFriendlyFire(boolean))
    // setBase(WorldPerspective) (from setBase(Location))

    // TODO pull broadcast methods (probably use Audience)
    // broadcast(String)
    // broadcast(BaseComponent...)
    // broadcast(Message...)
    // broadcast(Predicate<Associate>, String)

    // TODO move mut ops to mutable sub-interface; return resulting value
    // addPower(double) (moved from givePower(double)); add possible failure
    // subtractPower(double) (moved from takePower(double)); add possible failure
    // addClaims(int) (moved from giveChunks(int)) fix doc; add possible failure
    // subtractClaims(int) (moved from takeChunks(int)) fix doc; add possible failure
    // addWins(int) (moved from giveWins(int)); add possible failure; TODO is this ephemeral? move to dedicated api if so
    // subtractWins(int) (moved from takeWins(int)); add possible failure; TODO is this ephemeral? move to dedicated api if so

    // TODO move pdc
    // getKeys

    // TODO move clan stats
    // getClanInfo (is String[], TODO make dedicated api? map?)

    // getMembers TODO members as flyweights?

    /**
     * Gets the number of wars this clan has won.
     *
     * @return the number of wars this clan has won
     */
    int getWins(); // TODO move to war api

    /**
     * Gets the number of wars this clan has lost.
     *
     * @return the number of wars this clan has lost
     */
    int getLosses(); // TODO move to war api

    // TODO move cooldowns
    // getCooldowns

    // TODO move save op to mutable sub-interface
    // save

    // TODO remove remove()

    // TODO move size() to members api

    /**
     * Checks if this clan is owned by the server.
     *
     * @return true if this clan is owned by the server
     */
    boolean isConsole();

    // TODO add Rank api
}
