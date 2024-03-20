package io.github.sanctum.clans.model;

import com.github.sanctum.panther.util.HUID;
import io.github.sanctum.clans.interfacing.MutableEntity;
import io.github.sanctum.clans.interfacing.StagedUpdate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Access clan data (a DAO, save for {@linkplain #getId()}).
 *
 * @since 1.6.1
 * @author ms5984
 */
public interface Clan extends MutableEntity, CanUseAlias, CanDescribe {
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
     * @return the display name of this clan
     */
    @Override
    @Nullable String getAlias();

    // Move Clan.Color for getPalette

    /**
     * Gets the description of the clan.
     *
     * @return the description of this clan
     */
    @Override
    @Nullable String getDescription();

    /**
     * A clan editing utility.
     *
     * @since 1.6.1
     */
    interface Edits extends CanUseAlias.Edits, CanDescribe.Edits {
        @Override
        @NotNull Clan getMutating();

        // TODO more set methods
    }

    /**
     * A staged clan info update.
     */
    interface Update extends StagedUpdate, CanUseAlias.Update, CanDescribe.Update {
        @Override
        @NotNull Clan getReferenceObject();

        // TODO more getProposed methods
    }
}
