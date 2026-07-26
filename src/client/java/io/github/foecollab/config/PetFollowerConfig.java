package io.github.foecollab.config;

import io.github.foecollab.handler.HiderHandler;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class PetFollowerConfig {
    public static class PetFollower {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public HiderHandler.FollowingPetState ownPet = HiderHandler.FollowingPetState.OFF;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public HiderHandler.FollowingPetState otherPets = HiderHandler.FollowingPetState.OFF;
    }
}
