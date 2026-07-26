package io.github.foecollab.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import static io.github.foecollab.config.ConfigConstants.*;

@Config(name = "foecollab")
public class FOEConfig implements ConfigData {
    public static FOEConfig getConfig() {
        return AutoConfig.getConfigHolder(FOEConfig.class).getConfig();
    }

    //region Trackers
    @ConfigEntry.Category(value = TRACKERS)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerFishHUDConfig.FishTracker fishTracker = new TrackerFishHUDConfig.FishTracker();

    @ConfigEntry.Category(value = TRACKERS)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerPetEquipHUDConfig.PetEquipTracker petEquipTracker = new TrackerPetEquipHUDConfig.PetEquipTracker();

    @ConfigEntry.Category(value = TRACKERS)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerFullInventoryHUDConfig.FullInventoryTracker fullInventoryTracker = new TrackerFullInventoryHUDConfig.FullInventoryTracker();

    @ConfigEntry.Category(value = TRACKERS)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerContestHUDConfig.ContestTracker contestTracker = new TrackerContestHUDConfig.ContestTracker();

    @ConfigEntry.Category(value = TRACKERS)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerEquipmentHUDConfig.EquipmentTracker equipmentTracker = new TrackerEquipmentHUDConfig.EquipmentTracker();

    @ConfigEntry.Category(value = TRACKERS)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerBaitHUDConfig.BaitTracker baitTracker = new TrackerBaitHUDConfig.BaitTracker();

    @ConfigEntry.Category(value = TRACKERS)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerCrewHUDConfig.CrewTracker crewTracker = new TrackerCrewHUDConfig.CrewTracker();

    @ConfigEntry.Category(value = TRACKERS)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerFriendHUDConfig.FriendTracker friendTracker = new TrackerFriendHUDConfig.FriendTracker();

    @ConfigEntry.Category(value = TRACKERS)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerQuestHUDConfig.QuestTracker questTracker = new TrackerQuestHUDConfig.QuestTracker();

    @ConfigEntry.Category(value = TRACKERS)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerQuestHUDConfig.DailyQuestTracker dailyQuestTracker = new TrackerQuestHUDConfig.DailyQuestTracker();

    @ConfigEntry.Category(value = TRACKERS)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerEventConfig.EventTracker eventTracker = new TrackerEventConfig.EventTracker();

    @ConfigEntry.Category(value = TRACKERS)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerTimerConfig.TimerTracker timerTracker = new TrackerTimerConfig.TimerTracker();

    @ConfigEntry.Category(value = TRACKERS)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerBobberConfig.BobberTracker bobberTracker = new TrackerBobberConfig.BobberTracker();

    @ConfigEntry.Category(value = TRACKERS)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerChummerHUDConfig.ChummerTracker chummerTracker = new TrackerChummerHUDConfig.ChummerTracker();
    //endregion

    //region Tooltips
    @ConfigEntry.Category(value = TOOLTIPS)
    @ConfigEntry.Gui.CollapsibleObject
    public TooltipPetConfig.PetTooltip petTooltip = new TooltipPetConfig.PetTooltip();

    @ConfigEntry.Category(value = TOOLTIPS)
    @ConfigEntry.Gui.CollapsibleObject
    public TooltipItemFrameConfig.ItemFrameTooltip itemFrameTooltip = new TooltipItemFrameConfig.ItemFrameTooltip();

    @ConfigEntry.Category(value = TOOLTIPS)
    @ConfigEntry.Gui.CollapsibleObject
    public TooltipArmorStatsConfig.ArmorStatsTooltip armorStatsTooltip = new TooltipArmorStatsConfig.ArmorStatsTooltip();

    @ConfigEntry.Category(value = TOOLTIPS)
    @ConfigEntry.Gui.CollapsibleObject
    public TooltipFishStatsConfig.FishStatsTooltip fishStatsTooltip = new TooltipFishStatsConfig.FishStatsTooltip();
    //endregion

    //region Other
    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.Tooltip
    public boolean muteAdvancementSound = true;

    // Mouse-wheel hotbar slot cycling while in game (chat/menu scrolling is never affected).
    @ConfigEntry.Category(value = OTHER)
    public boolean hotbarScrolling = true;

    @ConfigEntry.Category(value = OTHER)
    public boolean hideHealthAndHunger = false;

    // Master toggle for user-created custom HUDs (edited via the FoE main screen → Custom HUDs).
    @ConfigEntry.Category(value = OTHER)
    public boolean customHuds = true;

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.Tooltip
    public boolean loadServerPackLocally = false;

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.CollapsibleObject
    public TooltipShortenerConfig.CleanerDisplay cleanerDisplay = new TooltipShortenerConfig.CleanerDisplay();

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.CollapsibleObject
    public NotificationsConfig.Notifications notifications = new NotificationsConfig.Notifications();

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.CollapsibleObject
    public ChatConfig.ChatSettings chatconfig = new ChatConfig.ChatSettings();

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.CollapsibleObject
    public TitleHudConfig.TitlePopup titlePopup = new TitleHudConfig.TitlePopup();

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.CollapsibleObject
    public BiteTitleConfig.BiteTitle biteTitle = new BiteTitleConfig.BiteTitle();

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.CollapsibleObject
    public BarHUDConfig.BarHUD barHUD = new BarHUDConfig.BarHUD();

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerScoreboardHUDConfig.ScoreboardTracker scoreboardTracker = new TrackerScoreboardHUDConfig.ScoreboardTracker();

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.CollapsibleObject
    public TrackerBossBarHUDConfig.BossBarTracker bossBarTracker = new TrackerBossBarHUDConfig.BossBarTracker();

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.CollapsibleObject
    public DiscordIPCConfig.DiscordIPC discordIPC = new DiscordIPCConfig.DiscordIPC();

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.CollapsibleObject
    public ItemMarkerConfig.ItemMarker itemMarker = new ItemMarkerConfig.ItemMarker();

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.CollapsibleObject
    public ItemStackDisplayConfig.ItemStackDisplay itemStackDisplay = new ItemStackDisplayConfig.ItemStackDisplay();

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.CollapsibleObject
    public PetFollowerConfig.PetFollower petFollower = new PetFollowerConfig.PetFollower();

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.CollapsibleObject
    public AutoTipConfig.AutoTip autoTip = new AutoTipConfig.AutoTip();

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.CollapsibleObject
    public BaitSortingHelperConfig.BaitSortingHelperVisibility baitSortingHelperVisibility = new BaitSortingHelperConfig.BaitSortingHelperVisibility();

    // backup stuff
    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.CollapsibleObject
    public StatsBackupConfig.StatsBackup statsBackup = new StatsBackupConfig.StatsBackup();
    //endregion

    //region Cosmetic
    @ConfigEntry.Category(value = COSMETIC)
    @ConfigEntry.Gui.CollapsibleObject
    public HudFontConfig.HudFontSettings hudFont = new HudFontConfig.HudFontSettings();

    @ConfigEntry.Category(value = COSMETIC)
    @ConfigEntry.Gui.CollapsibleObject
    public InventoryButtonConfig.InventoryButtonSettings inventoryButton = new InventoryButtonConfig.InventoryButtonSettings();

    @ConfigEntry.Category(value = COSMETIC)
    @ConfigEntry.Gui.CollapsibleObject
    public ThemingConfig.Theme theme = new ThemingConfig.Theme();

    @ConfigEntry.Category(value = COSMETIC)
    @ConfigEntry.Gui.CollapsibleObject
    public ThemingConfig.Flair flair = new ThemingConfig.Flair();
    //endregion

    //region Fun
    @ConfigEntry.Category(value = FUN)
    @ConfigEntry.Gui.CollapsibleObject
    @ConfigEntry.Gui.PrefixText
    public FunConfig.Fun fun = new FunConfig.Fun();
    //endregion

    //region Inventory Menu
    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.Excluded
    public boolean isButtonMenuOpen = true;

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.Excluded
    public boolean isCrewButtonMenuOpen = true;

    @ConfigEntry.Category(value = OTHER)
    @ConfigEntry.Gui.Excluded
    public boolean isTimerButtonMenuOpen = true;
    //endregion
}