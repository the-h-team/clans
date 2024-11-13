package com.github.sanctum.clans.util;

import org.bukkit.ChatColor;
import org.bukkit.map.MapPalette;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClansMapPalette {

    public static String appendSemiColon(String t) {
        return t.replaceAll("(§[0-9a-fA-F]{1,3})", "$1;");
    }

    public static String convertHexCodes(String input) {
        // Define the regex pattern to match the hex codes
        String regex = "§x(§[0-9a-fA-F]){6}";

        // Create a Pattern object
        Pattern pattern = Pattern.compile(regex);

        // Create a Matcher object
        Matcher matcher = pattern.matcher(input);

        // Use StringBuilder for efficient string manipulation
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            // Get the matched string
            String matchedHex = matcher.group();
            // Replace §x with #
            String modifiedHex = "#" + matchedHex.replace("§", "");
            // Append the modified hex code to the result
            matcher.appendReplacement(result, modifiedHex);
        }

        // Append any remaining part of the input
        matcher.appendTail(result);

        return appendSemiColon(convertPalette(convertChatColorToHex(result.toString().replaceAll("#x", "#"))));
    }

    public static String convertPalette(String input) {
        // Regex to match hex color codes prefixed with #
        String regex = "#[0-9a-fA-F]{6}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        // Use StringBuilder for building the output string
        StringBuilder result = new StringBuilder();

        int lastMatchEnd = 0;

        while (matcher.find()) {
            // Append the text before the match
            result.append(input, lastMatchEnd, matcher.start());

            // Get the matched hex color code
            String hexColor = matcher.group();
            // Convert to an AWT Color
            Color color = Color.decode(hexColor);
            // Get the closest color from MapPalette
            byte closestColor = MapPalette.matchColor(color);
            // Append the signal symbol and the corresponding color byte to the result
            result.append("§").append(closestColor);

            // Update the last match end position
            lastMatchEnd = matcher.end();
        }

        // Append any remaining part of the input
        result.append(input, lastMatchEnd, input.length());

        return result.toString();
    }

    public static String convertChatColorToHex(String input) {
        StringBuilder result = new StringBuilder();

        // Process the input character by character
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // If we encounter the signal symbol
            if (c == '§') {
                // Check if there's another character after it
                if (i + 1 < input.length()) {
                    char nextChar = input.charAt(i + 1);
                    // Convert ChatColor to hex if valid
                    ChatColor chatColor = ChatColor.getByChar(nextChar);
                    if (chatColor != null) {
                        String hexColor = getHexColor(chatColor);
                        result.append(hexColor);
                        i++; // Skip the next character as it was processed
                        continue;
                    }
                }
            }

            // Append the original character if not processed
            result.append(c);
        }

        return result.toString();
    }

    private static String getHexColor(ChatColor chatColor) {
        switch (chatColor) {
            case BLACK: return "#000000";
            case DARK_BLUE: return "#0000AA";
            case DARK_GREEN: return "#00AA00";
            case DARK_AQUA: return "#00AAAA";
            case DARK_RED: return "#AA0000";
            case DARK_PURPLE: return "#AA00AA";
            case GOLD: return "#FFAA00";
            case GRAY: return "#AAAAAA";
            case DARK_GRAY: return "#555555";
            case BLUE: return "#0000FF";
            case GREEN: return "#00FF00";
            case AQUA: return "#00FFFF";
            case RED: return "#FF0000";
            case LIGHT_PURPLE: return "#FF55FF";
            case YELLOW: return "#FFFF00";
            case WHITE: return "#FFFFFF";
            case MAGIC: return "#FFFFFF"; // For magic, you could use a default color
            case RESET: return "#FFFFFF"; // Reset color; use white or any default
            default: return "#FFFFFF"; // Fallback
        }
    }

}
