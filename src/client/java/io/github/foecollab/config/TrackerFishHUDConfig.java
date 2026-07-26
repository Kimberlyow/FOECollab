package io.github.foecollab.config;

import io.github.foecollab.handler.TextDisplayHandler;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class TrackerFishHUDConfig {
    public static class FishTracker {
        public boolean showFishTrackerHUD = true;
        @ConfigEntry.Gui.Tooltip()
        public boolean isFishTrackerOnTimer = false;
        public boolean showTimerOnAllTime = true;
        @ConfigEntry.BoundedDiscrete(min = 30, max = 300)
        @ConfigEntry.Gui.Tooltip
        public int autoPauseTimer = 60;

        @ConfigEntry.Gui.CollapsibleObject
        public FishTrackerToggles fishTrackerToggles = new FishTrackerToggles();

        public static class FishTrackerToggles {
            @ConfigEntry.Gui.CollapsibleObject
            public GeneralToggles generalToggles = new GeneralToggles();

            public static class GeneralToggles {
                public boolean showFishCaught = true;
                @ConfigEntry.Gui.Tooltip
                public boolean showTimer = true;
                public boolean showFishPerHour = true;
                public boolean showTotalXp = false;
                public boolean showTotalValue = false;
                public boolean showQuestsCompleted = true;
                public boolean showPetCaught = true;
                public boolean showPetPerHour = false;
                public boolean showShardCaught = true;
                public boolean showShardPerHour = false;
                public boolean trackPetsAndShardsFromQuests = true;
                public boolean showQuestPetsAndShardsSeparately = false;
                public boolean showLightningBottleCaught = false;
                public boolean showInfusionCapsuleCaught = false;
            }

            @ConfigEntry.Gui.CollapsibleObject
            public RarityToggles rarityToggles = new RarityToggles();

            public static class RarityToggles {
                @ConfigEntry.Gui.Tooltip
                public boolean showRarities = true;
                public boolean showCommon = true;
                public boolean showRare = true;
                public boolean showEpic = true;
                public boolean showLegendary = true;
                public boolean showMythical = true;
            }

            @ConfigEntry.Gui.CollapsibleObject
            public FishSizeToggles fishSizeToggles = new FishSizeToggles();

            public static class FishSizeToggles {
                @ConfigEntry.Gui.Tooltip
                public boolean showFishSizes = false;
                public boolean showBaby = true;
                public boolean showJuvenile = true;
                public boolean showAdult = true;
                public boolean showLarge = true;
                public boolean showGigantic = true;
            }

            @ConfigEntry.Gui.CollapsibleObject
            public VariantToggles variantToggles = new VariantToggles();

            public static class VariantToggles {
                @ConfigEntry.Gui.Tooltip
                public boolean showVariants = true;
                public boolean showAlbino = true;
                public boolean showMelanistic = true;
                public boolean showTrophy = true;
                public boolean showFabled = true;
            }

            @ConfigEntry.Gui.CollapsibleObject
            public DryStreakToggles dryStreakToggles = new DryStreakToggles();

            public static class DryStreakToggles {
                @ConfigEntry.Gui.PrefixText
                public boolean showCommon = false;
                public boolean showRare = false;
                public boolean showEpic = false;
                public boolean showLegendary = false;
                public boolean showMythical = false;
                @ConfigEntry.Gui.PrefixText
                public boolean showBaby = false;
                public boolean showJuvenile = false;
                public boolean showAdult = false;
                public boolean showLarge = false;
                public boolean showGigantic = true;
                @ConfigEntry.Gui.PrefixText
                public boolean showAlbino = true;
                public boolean showMelanistic = true;
                public boolean showTrophy = true;
                public boolean showFabled = true;
                // public boolean showSpooky = false;
                @ConfigEntry.Gui.PrefixText
                public boolean showPet = false;
                public boolean showShard = false;
                public boolean showLightningBottle = false;
                public boolean showInfusionCapsule = false;
            }

            @ConfigEntry.Gui.CollapsibleObject
            public FishEventToggles fishEventToggles = new FishEventToggles();

            public static class FishEventToggles {
                @ConfigEntry.Gui.CollapsibleObject
                public RarityToggles rarityToggles = new RarityToggles();

                public static class RarityToggles {
                    @ConfigEntry.Gui.Tooltip
                    public boolean showSpecial = false;
                }

                @ConfigEntry.Gui.CollapsibleObject
                public VariantToggles variantToggles = new VariantToggles();

                public static class VariantToggles {
                    @ConfigEntry.Gui.Tooltip
                    public boolean showAlternate = false;
                    @ConfigEntry.Gui.Tooltip
                    public boolean showSpooky = false;
                    @ConfigEntry.Gui.Tooltip
                    public boolean showFrozen = false;
                }

                @ConfigEntry.Gui.CollapsibleObject
                public DryStreakToggles dryStreakToggles = new DryStreakToggles();

                public static class DryStreakToggles {
                    public boolean showSpecial = false;
                    public boolean showAlternate = false;
                    public boolean showSpooky = false;
                    public boolean showFrozen = false;
                }
            }

            @ConfigEntry.Gui.CollapsibleObject
            public OtherToggles otherToggles = new OtherToggles();

            public static class OtherToggles {
                public boolean showPercentages = true;
                @ConfigEntry.Gui.Tooltip
                public boolean useNewTitle = true;
                @ConfigEntry.Gui.Tooltip
                public boolean showStatsOnCatch = true;
                @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
                public int showStatsOnCatchTime = 5;
                public boolean abbreviateNumbers = false;
            }
        }

        @ConfigEntry.Gui.CollapsibleObject
        public DryStreakMessageToggles dryStreakMessageToggles = new DryStreakMessageToggles();
        public static class DryStreakMessageToggles {
            @ConfigEntry.Gui.Tooltip
            public boolean showText = false;
            @ConfigEntry.Gui.Tooltip
            @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
            public TextDisplayHandler.TextDisplay textCapitalization = TextDisplayHandler.TextDisplay.TAG;

            @ConfigEntry.Gui.CollapsibleObject
            public RarityMessageToggles rarityMessageToggles = new RarityMessageToggles();

            public static class RarityMessageToggles {
                public boolean showCommon = false;
                public boolean showRare = false;
                public boolean showEpic = false;
                public boolean showLegendary = false;
                public boolean showMythical = false;
            }

            @ConfigEntry.Gui.CollapsibleObject
            public SizeMessageToggles sizeMessageToggles = new SizeMessageToggles();

            public static class SizeMessageToggles {
                public boolean showBaby = false;
                public boolean showJuvenile = false;
                public boolean showAdult = false;
                public boolean showLarge = false;
                public boolean showGigantic = true;
            }

            @ConfigEntry.Gui.CollapsibleObject
            public VariantMessageToggles variantMessageToggles = new VariantMessageToggles();

            public static class VariantMessageToggles {
                public boolean showAlbino = true;
                public boolean showMelanistic = true;
                public boolean showTrophy = true;
                public boolean showFabled = true;
            }

            @ConfigEntry.Gui.CollapsibleObject
            public OtherMessageToggles otherMessageToggles = new OtherMessageToggles();

            public static class OtherMessageToggles {
                public boolean showPet = true;
                public boolean showShard = true;
                public boolean showLightningBottle = true;
                public boolean showInfusionCapsule = true;
            }
        }

        public boolean hideTitle = false;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public HudAlignment alignment = HudAlignment.RIGHT;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 100)
        public int backgroundOpacity = 40;
        @ConfigEntry.BoundedDiscrete(max = 20, min = 2)
        public int fontSize = 8;
        // hudX is measured from the screen's left edge; RIGHT alignment pins the box's right edge
        // there, so 100 = flush against the right side (the default for this right-aligned HUD).
        @ConfigEntry.Gui.Excluded
        public int hudX = 100;
        @ConfigEntry.Gui.Excluded
        public int hudY = 30;
    }
}
