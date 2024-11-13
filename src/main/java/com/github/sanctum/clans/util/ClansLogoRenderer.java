package com.github.sanctum.clans.util;

import com.github.sanctum.clans.model.ClansAPI;
import com.github.sanctum.clans.model.Insignia;
import com.github.sanctum.labyrinth.library.Item;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.*;
import org.jetbrains.annotations.NotNull;

public class ClansLogoRenderer extends MapRenderer {

    MapFont font;

    public ClansLogoRenderer() {
        try {
            this.font = BananaTypeFont.from(ClansAPI.getInstance().getPlugin().getResource("font/minecraft-font-unicode.btf"));
        } catch (Exception e) {

        }
    }

    @Override
    public void render(@NotNull MapView mapView, @NotNull MapCanvas mapCanvas, @NotNull Player player) {
        // like a runnable can be used to draw whatever!!
        ClansAPI.getInstance().getAssociate(player).ifPresent(ass -> {
            for (int z = 0; z <= ass.getLogo().size() - 1; z++) {
                for (int x = 0; x <= ass.getLogo().get(z).length() - 1; x++) { // go up
                    mapCanvas.drawText(0, z * 12, this.font, ClansMapPalette.convertHexCodes(ass.getLogo().get(z)));
                }
            }
            mapCanvas.drawText(0, 100, this.font, "Showing logo " + ass.getClan().getName());
        });
    }


    public @NotNull ItemStack applyToItemStack(World world) {
        return new Item.Edit(Material.FILLED_MAP).build(damageable -> {}, itemMeta -> {
            MapMeta meta = (MapMeta)itemMeta;
            MapView view = Bukkit.createMap(world);
            view.getRenderers().clear(); // clear it
            view.addRenderer(this);
            meta.setMapView(view);
        });
    }

}
