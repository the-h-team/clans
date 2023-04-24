package io.github.sanctum.clans.messaging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class Messages {
    private static final Messages INSTANCE = new Messages();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private @NotNull ResourceBundle clans;
    private @NotNull ResourceBundle commands;

    private Messages() {
        this.clans = PropertyResourceBundle.getBundle("lang/clans");
        this.commands = PropertyResourceBundle.getBundle("lang/commands");
    }

    public static Raw clans(@PropertyKey(resourceBundle = "lang.clans") String key) {
        return new Raw(INSTANCE.clans.getString(key));
    }

    public static Raw commands(@PropertyKey(resourceBundle = "lang.commands") String key) {
        return new Raw(INSTANCE.commands.getString(key));
    }

    public static class Raw implements ComponentLike {
        final String text;

        public Raw(@NotNull String text) {
            this.text = text;
        }

        @Override
        public @NotNull Component asComponent() {
            return INSTANCE.miniMessage.deserialize(text);
        }

        public ComponentLike resolveWith(TagResolver tagResolver) {
            return () -> INSTANCE.miniMessage.deserialize(text, tagResolver);
        }

        public ComponentLike resolveWith(TagResolver... tagResolvers) {
            return () -> INSTANCE.miniMessage.deserialize(text, tagResolvers);
        }
    }
}
