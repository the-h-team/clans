package com.github.sanctum.clans.construct.extra;

import com.github.sanctum.clans.ClansJavaPlugin;
import com.github.sanctum.clans.construct.api.ClansAPI;
import com.github.sanctum.labyrinth.data.WideConsumer;
import com.github.sanctum.labyrinth.library.Applicable;
import com.github.sanctum.labyrinth.library.Metrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.NotNull;

public abstract class StartProcedure {

	protected final ClansJavaPlugin instance;
	protected final List<Applicable> data;

	public StartProcedure(ClansJavaPlugin instance) {
		Bukkit.getServicesManager().register(ClansAPI.class, instance, instance, ServicePriority.Normal);
		this.instance = instance;
		this.data = new ArrayList<>();
	}

	public StartProcedure then(final @NotNull WideConsumer<StartProcedure, ClansJavaPlugin> consumer) {
		data.add(() -> consumer.accept(this, instance));
		return this;
	}

	public void run() {
		data.forEach(Applicable::run);
	}

	public void runMetrics(Consumer<Metrics> metrics) {
		Metrics.register(instance, 10461, metrics);
	}

	public void sendBorder() {
		instance.getLogger().info("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
	}

	public List<String> getLogo() {
		return new ArrayList<>(Arrays.asList("   ▄▄▄·▄▄▄        ▄▄ ", "  ▐█ ▄█▀▄ █·▪     ██▌" + "  User ID: ", "   ██▀·▐▀▀▄  ▄█▀▄ ▐█·" + "   " + instance.USER_ID, "  ▐█▪·•▐█•█▌▐█▌.▐▌.▀ " + "  Unique ID: ", "  .▀   .▀  ▀ ▀█▄▀▪ ▀ " + "   " + instance.NONCE));
	}

}
