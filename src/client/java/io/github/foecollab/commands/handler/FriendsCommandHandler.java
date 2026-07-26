package io.github.foecollab.commands.handler;

import io.github.foecollab.handler.FriendsHandler;
import io.github.foecollab.handler.ProfileDataHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.*;

public class FriendsCommandHandler {
    private FriendsCommandHandler() {
    }

    public static List<Text> getFriendsCommandResponse(String type, String username) {
        List<Text> responseList = new ArrayList<>();
        switch (type) {
            case "add":
                handleAddRemove(true, username, responseList);
                break;
            case "remove":
                handleAddRemove(false, username, responseList);
                break;
            case "list":
                appendOnlineFriends(responseList);
                break;
            default:
                responseList.add(Text.literal("Invalid friends command type specified."));
                break;
        }
        return responseList;
    }

    private static void handleAddRemove(boolean add, String username, List<Text> responseList) {
        if (username == null || username.isEmpty()) {
            responseList.add(Text.literal("No User specified."));
            return;
        }
        Optional<UUID> onlineUuid = getUuid(username);
        if (onlineUuid.isEmpty()) {
            responseList.add(Text.literal("Player " + username + " is not online or does not exist."));
            return;
        }

        UUID uuid = onlineUuid.get();
        if (add) {
            if (Objects.equals(MinecraftClient.getInstance().getSession().getUuidOrNull(), uuid)) {
                responseList.add(Text.literal(getSelfFriendQuote(true)));
                return;
            }
            if (ProfileDataHandler.instance().profileData.friends.stream().anyMatch(id -> id.equals(uuid))) {
                responseList.add(Text.literal("Player " + username + " is already your friend."));
                return;
            }
            FriendsHandler.instance().addFriend(uuid);
            responseList.add(Text.literal("Friend " + username + " added."));
        } else {
            if (Objects.equals(MinecraftClient.getInstance().getSession().getUuidOrNull(), uuid)) {
                responseList.add(Text.literal(getSelfFriendQuote(false)));
                return;
            }
            if (ProfileDataHandler.instance().profileData.friends.stream().noneMatch(id -> id.equals(uuid))) {
                responseList.add(Text.literal("Player " + username + " is not your friend."));
                return;
            }
            FriendsHandler.instance().removeFriend(uuid);
            responseList.add(Text.literal("Friend " + username + " removed."));
        }
    }

    private static Optional<UUID> getUuid(String username) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getNetworkHandler() == null
                || client.getNetworkHandler().getPlayerList() == null) {
            return Optional.empty();
        }

        for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
            if (entry != null && entry.getProfile() != null
                    && Objects.equals(entry.getProfile().name(), username)) {
                return Optional.of(entry.getProfile().id());
            }
        }

        for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
            if (entry != null && entry.getProfile() != null
                    && entry.getProfile().name() != null
                    && entry.getProfile().name().equalsIgnoreCase(username)) {
                return Optional.of(entry.getProfile().id());
            }
        }

        return Optional.empty();
    }

    private static void appendOnlineFriends(List<Text> responseList) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getNetworkHandler() == null
                || client.getNetworkHandler().getPlayerList() == null) {
            responseList.add(Text.literal("Unable to list friends right now."));
            return;
        }

        List<UUID> friends = ProfileDataHandler.instance().profileData.friends;
        List<String> onlineNames = new ArrayList<>();

        for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
            if (entry != null && entry.getProfile() != null
                    && friends.stream().anyMatch(id -> id.equals(entry.getProfile().id()))) {
                onlineNames.add(entry.getProfile().name());
            }
        }

        responseList.add(Text.literal("Online friends: " + onlineNames.size()));
        if (onlineNames.isEmpty()) {
            responseList.add(Text.literal("\nNo friends are online."));
            return;
        }

        for (String name : onlineNames) {
            responseList.add(Text.literal("\n- " + name));
        }
    }

    private static String getSelfFriendQuote(boolean happy) {
        if (happy) {
            int index = (int) (Math.random() * SELF_FRIEND_QUOTES.size());
            return SELF_FRIEND_QUOTES.get(index);
        } else {
            int index = (int) (Math.random() * SELF_UNFRIEND_QUOTES.size());
            return SELF_UNFRIEND_QUOTES.get(index);
        }
        
        
    }

    private static final List<String> SELF_FRIEND_QUOTES = List.of(
            "You're already your best teammate.",
            "Self-respect unlocked!",
            "No need to add yourself - you've got this.",
            "You are enough. Keep fishing forward.",
            "Be your own co-captain today.",
            "You shine brightest when you back yourself.",
            "I'm so happy for you!",
            "Self-fullfillment achieved!");

    private static final List<String> SELF_UNFRIEND_QUOTES = List.of(
            "You can't unfriend yourself - you're stuck with you!",
            "Trying to unfriend yourself? That's a plot twist.",
            "You are your own ride or die - no unfriending allowed.",
            "Unfriending yourself? That's a no-go zone.",
            "You can't unfriend yourself - you're your own best friend!",
            "Unfriending yourself? That's a self-sabotage move.",
            "You can't unfriend yourself - you're your own MVP!",
            "Love yourself, even if you try to unfriend yourself!",
            "It's a wonderful day to be your own friend.");

}