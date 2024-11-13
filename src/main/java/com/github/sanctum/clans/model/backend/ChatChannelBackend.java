package com.github.sanctum.clans.model.backend;

import com.github.sanctum.clans.model.ClanError;
import com.github.sanctum.labyrinth.formatting.FancyMessage;
import com.github.sanctum.labyrinth.formatting.Message;
import com.github.sanctum.panther.file.Node;

import java.util.function.Function;

public class ChatChannelBackend {

    private final Node node;
    private Function<String, String> function;

    public ChatChannelBackend(Node node) {
        this.node = node;
    }

    public void setFormatter(Function<String, String> function) {
        this.function = function;
    }

    public String getDefault() {
        for (String section : node.getKeys(false)) {
            if (!section.equalsIgnoreCase("filters")) { // make sure its not the filters at the end
                Node sec = node.getNode(section);
                FileSection s = new FileSection(function.apply(sec.getNode("text").toPrimitive().getString()), null, null);
                return function.apply(s.text);
            }
        }
        throw new ClanError("The first section of this channel is missing! Please configure one.");
    }

    public Message getFormat() {
        FancyMessage message = new FancyMessage();
        for (String section : node.getKeys(false)) {
            if (!section.equalsIgnoreCase("filters")) { // make sure its not the filters at the end
                Node sec = node.getNode(section);
                FileSection s = new FileSection(function.apply(sec.getNode("text").toPrimitive().getString()), sec.getNode("hover").get() != null ? function.apply(sec.getNode("hover").toPrimitive().getString()) : null, sec.getNode("click").get() != null ? function.apply(sec.getNode("click").toPrimitive().getString()) : null);
                message.append(s.conformToMessage());
            }
        }
        return message;
    }

    static class FileSection {

        private final String text, hover, click;
        private final FancyMessage messageBuilder = new FancyMessage();

        FileSection(String text, String hover, String click) {
            this.text = text;
            this.hover = hover;
            this.click = click;
        }

        public Message conformToMessage() {
            messageBuilder.then(text);
            if (hover != null) messageBuilder.hover(hover);
            if (click != null) messageBuilder.command(click);
            return messageBuilder;
        }
    }

}
