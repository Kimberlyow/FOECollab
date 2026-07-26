package io.github.foecollab.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class FriendsHandler {
    private static FriendsHandler INSTANCE = new FriendsHandler();
    private List<UUID> friends = new ArrayList<>();

    public boolean isFriendsNearby = false;
    public boolean isFriendsInRenderDistance = false;

    private AtomicBoolean froundFriends = new AtomicBoolean(false);
    private AtomicBoolean isNearby = new AtomicBoolean(false);

    public static FriendsHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new FriendsHandler();
        }
        return INSTANCE;
    }

    public void beforeTickEntitiess() {
        froundFriends.set(false);
        isNearby.set(false);
    }

    public void tickEntities(Entity entity, MinecraftClient minecraftClient) {
        if (minecraftClient.player != null
                && entity instanceof PlayerEntity friend
                && ProfileDataHandler.instance().profileData.friends.stream().anyMatch(uuid -> uuid.equals(friend.getUuid()))
                && !friend.getUuid().equals(minecraftClient.player.getUuid())
        ) {
            if (friend.getEntityPos().distanceTo(minecraftClient.player.getEntityPos()) < 10) {
                isNearby.set(true);
            }
            froundFriends.set(true);
        }
    }

    public void addFriend(UUID uuid) {
        List<UUID> friends = getProfileFriendsCopy();
        if(friends.stream().noneMatch(id -> id.equals(uuid))) {
            friends.add(uuid);
        }
        this.friends = friends;
        ProfileDataHandler.instance().profileData.friends = friends;
    }

    public void removeFriend(UUID uuid) {
        List<UUID> friends = getProfileFriendsCopy();
        friends.removeIf(id -> id.equals(uuid));
        this.friends = friends;
        ProfileDataHandler.instance().profileData.friends = friends;
    }

    private List<UUID> getProfileFriendsCopy() {
        if (ProfileDataHandler.instance().profileData.friends == null) {
            ProfileDataHandler.instance().profileData.friends = new ArrayList<>();
        }
        return new ArrayList<>(ProfileDataHandler.instance().profileData.friends);
    }

    public void afterTickEntities() {
        this.isFriendsNearby = isNearby.get();
        this.isFriendsInRenderDistance = froundFriends.get();
    }

}
