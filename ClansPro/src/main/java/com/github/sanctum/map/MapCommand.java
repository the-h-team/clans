package com.github.sanctum.map;

import com.github.sanctum.clans.ClansPro;
import com.github.sanctum.clans.construct.Claim;
import com.github.sanctum.clans.construct.DefaultClan;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.clans.util.events.command.CommandHelpInsertEvent;
import com.github.sanctum.clans.util.events.command.CommandInsertEvent;
import com.github.sanctum.clans.util.events.command.TabInsertEvent;
import com.github.sanctum.labyrinth.event.custom.Vent;
import com.github.sanctum.labyrinth.formatting.string.ColoredString;
import com.github.sanctum.labyrinth.library.DirectivePoint;
import com.github.sanctum.labyrinth.library.TextLib;
import com.github.sanctum.link.ClanVentBus;
import com.github.sanctum.map.structure.ChunkPosition;
import com.github.sanctum.map.structure.MapPoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MapCommand implements Listener {

    public MapCommand() {

        ClanVentBus.subscribe(CommandHelpInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> e.insert("&7|&e) &6/clan &fmap"));

        ClanVentBus.subscribe(TabInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

            if (!e.getArgs(1).contains("map")) {
                e.add(1, "map");
            }
            final String[] commandArgs = e.getCommandArgs();
            if (commandArgs.length > 0 && commandArgs[0].equalsIgnoreCase("map")) {
                if (!e.getArgs(2).contains("on")) {
                    e.add(2, "on");
                }
                if (!e.getArgs(2).contains("off")) {
                    e.add(2, "off");
                }
            }

        });

        ClanVentBus.subscribe(CommandInsertEvent.class, Vent.Priority.HIGH, (e, subscription) -> {

            final Player p = e.getSender();
            final String[] args = e.getArgs();
            final int length = args.length;
            if (length > 0 && args[0].equalsIgnoreCase("map")) {
                if (length == 1) {
                    e.setReturn(true);
                    sendMapCurrentLoc(p);
                } else {
                    if (args[1].equalsIgnoreCase("on")) {
                        // on logic
                        sendMapCurrentLoc(p);
                        players.add(p);
                    } else if (args[1].equalsIgnoreCase("off")) {
                        // off logic
                        players.remove(p);
                    } else {
                        // send usage
                        return;
                    }
                    e.setReturn(true);
                }
            }

        });

    }

    static class Rose {
        private static final String[][] NORTH = new String[][]{{"N"}, {"W", "E"}, {"S"}};
        private static final String[][] EAST = new String[][]{{"E"}, {"N", "S"}, {"W"}};
        private static final String[][] SOUTH = new String[][]{{"S"}, {"E", "W"}, {"N"}};
        private static final String[][] WEST = new String[][]{{"W"}, {"S", "N"}, {"E"}};

        static String[][] getForFacing(BlockFace facing) {
            switch (facing) {
                case NORTH:
                    return NORTH;
                case EAST:
                    return EAST;
                case SOUTH:
                    return SOUTH;
                case WEST:
                    return WEST;
                default:
                    return null;
            }
        }
    }

    protected static final int CHUNK_RADIUS = 16;
    protected static final int UI_HORIZONTAL = 14;
    protected static final int UI_VERTICAL = 21;
    private final Plugin PLUGIN = JavaPlugin.getProvidingPlugin(MapCommand.class);
    protected final boolean on1_16 = PLUGIN.getServer().getVersion().contains("1.16");
    private final TextLib textLib = TextLib.getInstance();
    private final Set<Player> players = new HashSet<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerChangeChunk(PlayerMoveEvent e) {
        final Location to = e.getTo();
        if (to == null) return;
        final Location from = e.getFrom();
        final World fromWorld = from.getWorld();
        if (fromWorld == null) return;
        final World toWorld = to.getWorld();
        if (toWorld == null) return;
        if (fromWorld != toWorld) return;
        final Chunk chunkFrom = fromWorld.getChunkAt(from);
        final Chunk chunkTo = toWorld.getChunkAt(to);
        if (chunkFrom.equals(chunkTo)) return;
        // more code
        PLUGIN.getServer().getPluginManager()
                .callEvent(new ChangeChunkInWorldEvent(e.getPlayer(), chunkFrom, chunkTo, fromWorld));
    }

    @EventHandler
    public void onChangeChunkInWorld(ChangeChunkInWorldEvent e) {
        if (players.contains(e.player)) {
            final float yaw = e.player.getLocation().getYaw();
            final Optional<BlockFace> optional = CompletableFuture.supplyAsync(() -> chooseDirection(yaw)).join();
            if (optional.isPresent()) sendMapCurrentLoc(e.player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMapDrawEvent(AsyncMapDrawEvent e) {
        final ChunkPosition playerChunkPosition = e.getPlayerChunkPosition();
        final Map<ChunkPosition, String> clanClaims = e.getClanChunks();
        new BukkitRunnable() {
            final MapPoint[][] mapPoints = new MapPoint[UI_HORIZONTAL][UI_VERTICAL];
            final Set<String> clanIds = e.getClanIdStrings();
            final BlockFace blockFace = e.getCompassDirection();
            final int playerX = playerChunkPosition.x;
            final int playerZ = playerChunkPosition.z;
            final int x_offset = UI_VERTICAL / 2;
            final int z_offset = UI_HORIZONTAL / 2;

            private int[] calculate_rot(int init_X, int init_Z) {
                final int x, z;
                if (blockFace == BlockFace.WEST) {
                    x = z_offset - init_Z;
                    z = -x_offset + init_X;
                } else if (blockFace == BlockFace.NORTH) {
                    x = -init_X + x_offset;
                    z = -init_Z + z_offset;
                } else if (blockFace == BlockFace.EAST) {
                    x = -z_offset + init_Z;
                    z = x_offset - init_X;
                } else { // SOUTH
                    x = init_X - x_offset;
                    z = init_Z - z_offset;
                }
                return new int[]{playerX - x, playerZ - z};
            }

            @Override
            public void run() {
                for (int z = 0; z < UI_HORIZONTAL; ++z) {
                    for (int x = 0; x < UI_VERTICAL; ++x) {
                        final ChunkPosition test = new ChunkPosition(calculate_rot(x, z));
                        String clanId = clanClaims.get(test);
                        if (test.equals(playerChunkPosition)) {
                            mapPoints[z][x] = MapPoint.center(clanId, test);
                            continue;
                        }
                        mapPoints[z][x] = new MapPoint(clanId, test); // chunk data now always stored with MapPoint
                    }
                }
                PLUGIN.getServer().getPluginManager().callEvent(new AsyncMapFormatEvent(e.getPlayer(), mapPoints, clanIds));
            }
        }.runTaskAsynchronously(PLUGIN);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMapFormatEvent(AsyncMapFormatEvent e) {
        final MapPoint[][] mapPoints = e.getMapPoints();
        final Set<String> clanIds = e.getClanIds();
        int rep = 0;
        final Map<String, String> colors = new HashMap<>();
        final Map<String, Character> representations = new HashMap<>();
        for (String clanId : clanIds) {
            final int temp = rep;
            colors.computeIfAbsent(clanId, id -> {
                /*int code = temp + 5;
                if (code <= 9) {
                    return ChatColor.getByChar((char) ('0' + code));
                }
                code -= 10;
                return ChatColor.getByChar((char)('A' + code));*/
                String dummyId = "dummy";
                if (DefaultClan.action.getClanID(e.getPlayer().getUniqueId()) != null) {
                    dummyId = DefaultClan.action.getClanID(e.getPlayer().getUniqueId());
                }
                String finalDummyId = dummyId;
                return CompletableFuture.supplyAsync(() -> DefaultClan.action.clanRelationColor(clanId, finalDummyId)).join();
            });
            representations.computeIfAbsent(clanId, id -> (char) ('A' + temp));
            ++rep;
        }
        for (MapPoint[] mapPoint : mapPoints) {
            for (final MapPoint tempPoint : mapPoint) {
                final String tempId = tempPoint.clanId;
                if (tempId != null) {
                    tempPoint.setColor(colors.get(tempId));
                    tempPoint.setRepresentation(representations.get(tempId));
                    tempPoint.setHover(ChatColor.GOLD + "Clan: " + colors.get(tempId) + "%clanTag%" + "\n" +
                            ChatColor.GOLD + "Power: " + ChatColor.RESET + "%clanPower%"); // Uses custom placeholder instead :D
                }
                if (tempPoint.isCenter()) {
                    tempPoint.setColor(ChatColor.GOLD.toString());
                    tempPoint.setRepresentation('X');
                    final String hover = tempPoint.getRawHover();
                    if (hover != null) {
                        tempPoint.setHover(hover.concat(ChatColor.GREEN + "\nYou are here"));
                        continue;
                    }
                    tempPoint.setHover(ChatColor.GREEN + "You are here");
                }
                // Here's a snippet of how to add Chunk coordinate display to a map view:
                /*final String hover = tempPoint.getRawHover();
                final String chunkData = ChatColor.GOLD + "Chunk: " + ChatColor.GRAY + "[" +
                        ChatColor.RESET + "%chunkX%,%chunkZ%" + ChatColor.GRAY + "]";
                tempPoint.setHover((hover == null) ? chunkData : hover.concat("\n" + chunkData));*/
            }
        }
        final String clanLegend = CompletableFuture.supplyAsync(() -> {
            if (clanIds.isEmpty()) return "";
            final StringBuilder sb = new StringBuilder();
            for (String clanId : clanIds) {
                sb.append(", ").append(colors.get(clanId)).append(representations.get(clanId)).append(" = ")
                        .append(ClansPro.getInstance().getClan(clanId).getColor())
                        .append(ClansPro.getInstance().getClanName(clanId)).append(ChatColor.RESET);
            }
            return sb.toString();
        }).join();
        e.setAddedLinesBottom(Collections.singletonList(ChatColor.YELLOW + "X = " + ChatColor.BOLD + "You" + ChatColor.RESET + clanLegend));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMapFormatDoneMonitor(AsyncMapFormatEvent e) {
        final List<BaseComponent[]> strings = new ArrayList<>();
        final MapPoint[][] mapPoints = e.getMapPoints();
        for (MapPoint[] mapPoint : mapPoints) {
            final BaseComponent[] line = new BaseComponent[mapPoint.length];
            int i = 0;
            for (final MapPoint tempPoint : mapPoint) {
                if (tempPoint.getHover() == null) {
                    line[i] = new ColoredString(tempPoint.getColor() + tempPoint.getRepresentation(),
                            (on1_16) ? ColoredString.ColorType.HEX : ColoredString.ColorType.MC).toComponent();
                } else {
                    if (tempPoint.appliance != null) {
                        line[i] = textLib.execute(tempPoint.appliance, textLib.textHoverable(
                                "",
                                tempPoint.getColor() + tempPoint.getRepresentation(),
                                DefaultClan.action.color(tempPoint.getHover())));
                    } else {
                        line[i] = textLib.textHoverable(
                                "",
                                tempPoint.getColor() + tempPoint.getRepresentation(),
                                DefaultClan.action.color(tempPoint.getHover()));
                    }
                }
                if (i < mapPoint.length - 1) line[i].addExtra(" ");
                ++i;
            }
            strings.add(line);
        }
        final Player player = e.getPlayer();
        final List<String> header = e.getAddedLinesTop();
        final List<String> footer = e.getAddedLinesBottom();
        if (header != null) {
            for (String extra : e.getAddedLinesTop()) {
                player.sendMessage(extra);
            }
        }
        int i = 0;
        final DirectivePoint directivePoint = DirectivePoint.get(player);
        final BlockFace facing = chooseDirection(player.getLocation().getYaw()).orElse(null);
        final String[][] rose = facing != null ? Rose.getForFacing(facing) : null;
        final boolean compareDirectionals = compareDirectionals(facing, directivePoint);
        for (BaseComponent[] components : strings) {
            // Insert compass rose
            if (i < 5) {
                final int lastIndex = components.length - 1;
                if (rose != null) {
                    if (i == 0) {
                        components[lastIndex].addExtra("    ");
                        final TextComponent forward = new TextComponent(rose[0][0]);
                        if (compareDirectionals) {
                            forward.setColor(ChatColor.GOLD);
                        }
                        components[lastIndex].addExtra(forward);
                    } else if (i == 1) {
                        components[lastIndex].addExtra("  ");
                        final TextComponent left = new TextComponent("\\");
                        final TextComponent right = new TextComponent("/");
                        if (!compareDirectionals) {
                            if (leftOrRight(facing, directivePoint)) {
                                left.setColor(ChatColor.GOLD);
                            } else {
                                right.setColor(ChatColor.GOLD);
                            }
                        }
                        components[lastIndex].addExtra(left);
                        components[lastIndex].addExtra("   ");
                        components[lastIndex].addExtra(right);
                    } else if (i == 2) {
                        components[lastIndex].addExtra(" ");
                        components[lastIndex].addExtra(new TextComponent(rose[1][0]));
                        components[lastIndex].addExtra("     ");
                        components[lastIndex].addExtra(new TextComponent(rose[1][1]));
                    } else if (i == 3) {
                        components[lastIndex].addExtra("  ");
                        components[lastIndex].addExtra(new TextComponent("/"));
                        components[lastIndex].addExtra("   ");
                        components[lastIndex].addExtra(new TextComponent("\\"));
                    } else if (i == 4) {
                        components[lastIndex].addExtra("    ");
                        components[lastIndex].addExtra(new TextComponent(rose[2][0]));
                    }
                }
                ++i;
            }
            player.spigot().sendMessage(components);
        }
        if (footer != null) {
            for (String extra : e.getAddedLinesBottom()) {
                player.sendMessage(DefaultClan.action.color(extra));
            }
        }
    }

    private void sendMapCurrentLoc(final Player player) {
        // TODO: Use the rotation to draw a compass rose
        // negative Z = NORTH
        final float yaw = player.getLocation().getYaw();
        final Optional<BlockFace> optional = CompletableFuture.supplyAsync(() -> chooseDirection(yaw)).join();
        if (!optional.isPresent()) {
            player.sendMessage("Too diagonal! Please face a more specific direction and run this command again.");
            return; // TODO: Don't run mapping, ask that player needs to face a more specific direction
        }
        final BlockFace compassDirection = optional.get();
        final Chunk chunk = player.getLocation().getChunk();
        final int playerChunkX = chunk.getX();
        final int playerChunkZ = chunk.getZ();
        // strings[0] = clanId, strings[1] = claimId, strings[2] = worldName
        final List<Claim> chunkMap = new ArrayList<>(ClansAPI.getInstance().getClaimManager().getClaims());
        new BukkitRunnable() { // Process claim chunk data and send off to DrawEvent
            @Override
            public void run() {
                final Map<ChunkPosition, String> clanChunks = new HashMap<>(); // key = chunk, value = clanId
                for (Claim entry : chunkMap) {
                    final int[] value = entry.getPos();
                    if (value == null) continue;
                    if (Math.abs(value[0] - playerChunkX) >= CHUNK_RADIUS) {
                        continue;
                    }
                    if (Math.abs(value[1] - playerChunkZ) >= CHUNK_RADIUS) {
                        continue;
                    }
                    clanChunks.put(new ChunkPosition(value), entry.getOwner());
                }
                final Set<String> clanIdStrings = new HashSet<>(clanChunks.values());
                final ChunkPosition playerChunk = new ChunkPosition(playerChunkX, playerChunkZ);
                PLUGIN.getServer().getPluginManager().callEvent(new AsyncMapDrawEvent(player, playerChunk, compassDirection, clanChunks, clanIdStrings));
            }
        }.runTaskAsynchronously(PLUGIN);
    }

    /**
     * Returns null if within 10 degrees of 45's.
     */
    public static Optional<BlockFace> chooseDirection(float degree) {
        // scale to increments of 5
        if (degree < 0f) {
            degree += 360f;
        }
        if (degree >= 320f || degree <= 40f) {
            return Optional.of(BlockFace.SOUTH);
        } else if (degree >= 60f && degree <= 130f) {
            return Optional.of(BlockFace.WEST);
        } else if (degree >= 140f && degree <= 220f) {
            return Optional.of(BlockFace.NORTH);
        } else if (degree >= 230f && degree <= 310f) {
            return Optional.of(BlockFace.EAST);
        }
        return Optional.empty();
    }

    static boolean compareDirectionals(BlockFace face, DirectivePoint directivePoint) {
        switch (directivePoint) {
            case East:
                return face == BlockFace.EAST;
            case West:
                return face == BlockFace.WEST;
            case North:
                return face == BlockFace.NORTH;
            case South:
                return face == BlockFace.SOUTH;
            default:
                return false;
        }
    }

    static boolean leftOrRight(BlockFace mainFace, DirectivePoint directivePoint) {
        switch (mainFace) {
            case EAST:
                return directivePoint == DirectivePoint.North_East;
            case WEST:
                return directivePoint == DirectivePoint.South_West;
            case NORTH:
                return directivePoint == DirectivePoint.North_West;
            case SOUTH:
                return directivePoint == DirectivePoint.South_East;
            default:
                throw new IllegalArgumentException("Must be cardinal");
        }
    }
}
