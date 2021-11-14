package com.github.sanctum.clans.bridge.internal.map.structure;

import com.github.sanctum.clans.construct.api.Clan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.library.Applicable;
import com.github.sanctum.labyrinth.library.HUID;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a clan map data point. Stores a nullable clanId String and a
 * ChunkPosition for further processing.
 */
public class MapPoint {
    public final String clanId;
    public Applicable appliance = null;
    public final ChunkPosition chunkPosition;
    protected String color = ChatColor.GRAY.toString();
    protected char representation = '-';
    protected String hover = null;

    /**
     * Main constructor for ordinary MapPoints.
     * <p>See {@link #center(String, ChunkPosition)} for simple factory-based
     * {@link #isCenter()} override.</p>
     *
     * @param clanId        clanId at this point (can be null)
     * @param chunkPosition chunk coordinate data at this point
     */
    public MapPoint(@Nullable String clanId, ChunkPosition chunkPosition) {
        this.clanId = clanId;
        this.chunkPosition = chunkPosition;
    }

    /**
     * Get the Clan object at this point, if applicable.
     *
     * @return Clan or null
     */
    public Clan getClan() {
        if (clanId == null) return null;
	    return ClansAPI.getInstance().getClanManager().getClan(HUID.fromString(clanId));
    }

    /**
     * Get the name of the Clan at this point, if applicable.
     *
     * @return name of the clan or null
     */
    public String getClanName() {
        if (clanId == null) return null;
	    return ClansAPI.getInstance().getClanManager().getClanName(HUID.fromString(clanId));
    }

    /**
     * Set the display color of this point on the map.
     *
     * @param color to display representation
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Set an operation to be ran when this map point is clicked on.
     *
     * @param appliance The information to run when the map point is clicked on.
     */
    public void setAppliance(Applicable appliance) {
        this.appliance = appliance;
    }

    /**
     * Get the display color set for this point on the map.
     *
     * @return color string (default gray)
     */
    public String getColor() {
        return color;
    }

    /**
     * Set the character to represent this point on the map.
     * Defaults to '-' for vacant points, 'A'... for claims
     *
     * @param character 1-width character
     */
    public void setRepresentation(char character) {
        this.representation = character;
    }

    /**
     * Gets the character which will be used to represent this point on the map.
     *
     * @return char ('-' default)
     */
    public char getRepresentation() {
        return representation;
    }

    /**
     * Set the hover message for this MapPoint.
     *
     * <p>A few placeholders are available:</p>
     * <p>%clanTag% = Clan name</p>
     * <p>%clanPower% = Clan power</p>
     *
     * @param textOnHover Formatted String with placeholders
     */
    public void setHover(String textOnHover) {
        this.hover = textOnHover;
    }

    /**
     * Get the direct hover text currently set without placeholder replacements.
     *
     * @return this.hover (can be null)
     */
    @Nullable
    public String getRawHover() {
        return hover;
    }

    /**
     * Get the fully-formatted hover text, if it has been set.
     *
     * @return text or null
     */
    @Nullable
    public String getHover() {
        if (hover == null) return null;
        if (!hover.contains("%")) return hover;
        String hover = this.hover;
        if (hover.contains("%clanTag%")) {
            hover = hover.replace("%clanTag%", getClanName());
        }
        if (hover.contains("%clanPower%")) {
            hover = hover.replace("%clanPower%", Clan.ACTION.format(getClan().getPower()));
        }
        if (hover.contains("%chunkX%")) {
            hover = hover.replace("%chunkX%", String.valueOf(chunkPosition.x));
        }
        if (hover.contains("%chunkZ%")) {
            hover = hover.replace("%chunkZ%", String.valueOf(chunkPosition.z));
        }
        /*if (hover.contains("%allyStatus%")) {
            // TODO: need reference to player?
        }*/
        return hover;
    }

    /**
     * Returns true if this point represents the player's chunk (the map center).
     *
     * @return false, if center player true
     */
    public boolean isCenter() {
        return false;
    }

    /**
     * Static factory to create object with isCenter() override instead of dedicating
     * another instance field and an alternate constructor.
     * <p>(There is, after all, only one time when we'll need {@link #isCenter()}
     * to return true)</p>
     *
     * @param clanId        clanId (can be null)
     * @param chunkPosition Data on chunk coordinates
     * @return new MapPoint with {@link #isCenter()} return true
     */
    public static MapPoint center(@Nullable String clanId, ChunkPosition chunkPosition) {
        return new MapPoint(clanId, chunkPosition) {
            @Override
            public boolean isCenter() {
                return true;
            }
        };
    }
}
