package io.github.foecollab.handler;

import io.github.foecollab.FOECollab;
import io.github.foecollab.FOMC.Constant;
import io.github.foecollab.FOMC.Types.Fish;
import io.github.foecollab.config.FOEConfig;
import io.github.foecollab.handler.packet.PacketHandler;
import io.github.foecollab.util.SimpleTagFont;
import io.github.foecollab.util.TextHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class FishCatchHandler {
	private static FishCatchHandler INSTANCE = new FishCatchHandler();
	private final FOEConfig config = FOEConfig.getConfig();

	private Text title = Text.empty();
	private Text subtitle = Text.empty();
	private final List<UUID> trackFishList = new ArrayList<>();
	private boolean fishFound = false;
	private boolean preCheck = false;
	private boolean isFull = false;
	private long fishCaughtTime = 0L;
	private boolean hasUsedRod = false;

	public long lastTimeUsedRod = 0L;

	public static FishCatchHandler instance() {
		if (INSTANCE == null) {
			INSTANCE = new FishCatchHandler();
		}
		return INSTANCE;
	}

	public void tick(MinecraftClient minecraftClient) {
		if (minecraftClient.player == null || !LoadingHandler.instance().isLoadingDone
				|| minecraftClient.world == null) {
			return;
		}

		if (minecraftClient.player.fishHook != null && !hasUsedRod) {
			hasUsedRod = true;
		} else if (hasUsedRod && minecraftClient.player.fishHook == null) {
			hasUsedRod = false;
			this.lastTimeUsedRod = System.currentTimeMillis();
		}

		if (this.preCheck) {
			this.updateTrackedFish(minecraftClient.player);
			this.preCheck = false;
			FOECollab.LOGGER.info("[FoE] Tracked Fish: {}", this.trackFishList.size());
		}

		if (this.fishFound) {
			if (System.currentTimeMillis() - this.fishCaughtTime < 2000L) {
				if (!this.isFull) {
					int checkedStacks = 0;
					for (int i = minecraftClient.player.getInventory().getMainStacks().size() - 1; i >= 0; i--) {
						ItemStack stack = minecraftClient.player.getInventory().getMainStacks().get(i);
						if (stack.isEmpty()) {
							continue;
						}
						checkedStacks++;
						this.processStack(stack, minecraftClient);

					}
					if (checkedStacks > 0) {
						FOECollab.LOGGER.debug("[FoE] Checked {} stacks for fish catch", checkedStacks);
					}
				}

				if (FullInventoryHandler.instance().slotsLeft == 0) {
					this.isFull = true;
				}

			} else {
				FOECollab.LOGGER.warn("[FoE] Fish not found after 2s - title: '{}', subtitle: '{}', isFull: {}",
						this.title.getString(), this.subtitle.getString(), this.isFull);
				this.fishFound = false;
				this.isFull = false;
				this.updateTrackedFish(minecraftClient.player);
			}
		}

		ProfileDataHandler.instance().tickTimer();
	}

	public void tickEntities(Entity entity, MinecraftClient minecraftClient) {
		if (this.fishFound && this.isFull) {
			if (entity instanceof ItemEntity itemEntity) {
				ItemStack stack = itemEntity.getStack();
				if (stack.isEmpty()) {
					return;
				}
				this.processStack(stack, minecraftClient);
			}
		}
	}

	public void onJoinServer() {
		this.preCheck = true;
	}

	public void onLeaveServer() {
		this.fishFound = false;
	}

	public void catchTitle(Text title) {
		if (title.getString().length() != 1 || title.equals(Text.empty())) {
			return;
		}

		if (isFish(title.getString().charAt(0))) {
			this.title = title;
			this.fishFound = true;
			this.fishCaughtTime = System.currentTimeMillis();
		}
	}

	public void catchSubtitle(Text title) {
		if (title.getString().contains(Constant.COMMON.TAG.getString())
				|| title.getString().contains(Constant.RARE.TAG.getString())
				|| title.getString().contains(Constant.EPIC.TAG.getString())
				|| title.getString().contains(Constant.LEGENDARY.TAG.getString())
				|| title.getString().contains(Constant.MYTHICAL.TAG.getString())
				|| title.getString().contains(Constant.SPECIAL.TAG.getString())) {
			this.subtitle = title;
		}
	}

	public void reset() {
		LoadingHandler.instance().isLoadingDone = false;
		if (MinecraftClient.getInstance().player != null) {
			this.updateTrackedFish(MinecraftClient.getInstance().player);
		}
	}

	public boolean onReceiveMessage(Text text) {
		if (text.getString().startsWith("PET DROP! You pulled")) {
			int oldPetDryStreak = ProfileDataHandler.instance().profileData.petDryStreak;

			ProfileDataHandler.instance().updatePetCaughtStatsOnCatch();
			FOECollab.LOGGER.info("[FoE] Tracking Pet");

			if (config.fishTracker.dryStreakMessageToggles.otherMessageToggles.showPet) {
				sendItemDryStreakMessage("pet", oldPetDryStreak);
			}
		}

		if (text.getString().startsWith("RARE CATCH! You pulled") && text.getString().contains("Shard")) {
			int oldShardDryStreak = ProfileDataHandler.instance().profileData.shardDryStreak;
			
			// Parse shard multiplier
			// For Prospect Amulet or future additions to the server
			int shardCount = 1;
			String msg = text.getString();
			int aIndex = msg.indexOf("a ");
			int xIndex = msg.indexOf("x ");
			if (aIndex != -1 && xIndex != -1 && xIndex > aIndex) {
				try {
					shardCount = Integer.parseInt(msg.substring(aIndex + 2, xIndex).trim());
				} catch (NumberFormatException e) {
					shardCount = 1;
				}
			}
			
			ProfileDataHandler.instance().updateShardCaughtStatsOnCatch(shardCount);
			FOECollab.LOGGER.info("[FoE] Tracking Shard x{}", shardCount);
			DailyQuestHandler.instance().updateQuest("Shards Caught");

			if (config.fishTracker.dryStreakMessageToggles.otherMessageToggles.showShard) {
				sendItemDryStreakMessage("shard", oldShardDryStreak);
			}
		}

		if (text.getString().startsWith("RARE CATCH! You pulled")
				&& text.getString().contains("Lightning in a Bottle")) {
			int oldLightningBottleDryStreak = ProfileDataHandler.instance().profileData.lightningBottleDryStreak;

			ProfileDataHandler.instance().updateLightningBottleCaughtStatsOnCatch();
			FOECollab.LOGGER.info("[FoE] Tracking Lightning Bottle");

			if (config.fishTracker.dryStreakMessageToggles.otherMessageToggles.showLightningBottle) {
				sendItemDryStreakMessage("lightning bottle", oldLightningBottleDryStreak);
			}
		}

		if (text.getString().startsWith("RARE CATCH! You pulled") && text.getString().contains("Infusion Capsule")) {
			int oldInfusionCapsuleDryStreak = ProfileDataHandler.instance().profileData.infusionCapsuleDryStreak;

			ProfileDataHandler.instance().updateInfusionCapsuleCaughtStatsOnCatch();
			FOECollab.LOGGER.info("[FoE] Tracking Infusion Capsule");

			if (config.fishTracker.dryStreakMessageToggles.otherMessageToggles.showInfusionCapsule) {
				sendItemDryStreakMessage("infusion capsule", oldInfusionCapsuleDryStreak);
			}
		}

		return false; // Don't suppress any messages
	}

	private boolean isFish(char character) {
		return (int) character > 0xE000 && (int) character < 0xE999;
	}

	private void processStack(ItemStack stack, MinecraftClient minecraftClient) {
		Fish fish = Fish.getFish(stack);
		if (fish != null && this.fishFound) {
			String stackName = stack.getName().getString();
			String subtitle = this.subtitle.getString();
			boolean catcherMatches = minecraftClient.player != null
					&& Objects.equals(fish.catcher, minecraftClient.player.getUuid());
			boolean notTracked = !trackFishList.contains(fish.id);
			boolean subtitleMatches = subtitle.contains(stackName);

			// Diagnostic only — this runs for every fish stack in the inventory on every
			// tick of the catch window, so keep it at debug to avoid log/CPU spam while grinding.
			if (FOECollab.LOGGER.isDebugEnabled()) {
				FOECollab.LOGGER.debug(
						"[FoE] Found fish: {} (variant: {}) - catcher: {}, notTracked: {}, subtitleMatch: {} (subtitle: '{}' contains name: '{}')",
						stackName, fish.variant.ID, catcherMatches, notTracked, subtitleMatches, subtitle, stackName);
			}
		}
		if (fish != null
				&& minecraftClient.player != null
				&& Objects.equals(fish.catcher, minecraftClient.player.getUuid())
				&& !trackFishList.contains(fish.id)
				&& this.subtitle.getString().contains(stack.getName().getString())) {
			FOECollab.LOGGER.info("[FoE] Tracking {}", stack.getName().getString());

			if (config.fishTracker.fishTrackerToggles.otherToggles.useNewTitle) {
				this.sendToTitleHud(fish, this.title, this.subtitle);
			}

			ProfileDataHandler.instance().updateStatsOnCatch(fish);
			ProfileDataHandler.instance().updateStatsOnCatch();
			QuestHandler.instance().updateQuest(fish);
			DailyQuestHandler.instance().updateQuest("Total Caught");
			if (fish.rarity == Constant.MYTHICAL) {
				DailyQuestHandler.instance().updateQuest("Mythical Caught");
			}
			PetEquipHandler.instance().updatePet(minecraftClient.player);

			if (config.contestTracker.shouldShowFullContest() && config.contestTracker.refreshOnContestPB) {
				// Check if caught fish is for contest and refresh if it's heavier
				var typecheck = ContestHandler.instance().type.replace("Heaviest", "").trim().toLowerCase();
				ContestHandler contestHandler = ContestHandler.instance();

				// Check if we are in the right location for the contest
				boolean locationMatches = Objects.equals(
						Objects.requireNonNull(Constant.valueOfTag(contestHandler.location)) == Constant.SPAWNHUB
								? Constant.CYPRESS_LAKE.ID
								: Objects.requireNonNull(Constant.valueOfTag(contestHandler.location).ID),
						BossBarHandler.instance().currentLocation.ID);

				if (contestHandler.isContest && typecheck.contains(fish.groupId.toLowerCase())
						&& locationMatches && (fish.weight > contestHandler.biggestFish)) {
					ContestHandler.instance().biggestFish = fish.weight;
					ContestHandler.instance().setRefreshReason("personal_best");
					minecraftClient.player.networkHandler.sendChatCommand("contest");

					// Send packet to notify other players of contest PB
					if (config.contestTracker.recieveLocalPBs) {
						PacketHandler.CONTEST_PB_PACKET.sendContestPBPacket(fish.groupId,
								minecraftClient.player.getName().getString(), fish.weight,
								ScoreboardHandler.instance().level);
					}

					FOECollab.LOGGER.info("[FoE] Refreshed Contest Stats - New heaviest fish: {} lbs",
							fish.weight);
				}
			}

			this.fishFound = false;
			this.isFull = false;
			this.updateTrackedFish(minecraftClient.player);
			this.title = Text.empty();
			this.subtitle = Text.empty();

		}

	}

	private void updateTrackedFish(PlayerEntity player) {
		trackFishList.clear();
		for (int i = player.getInventory().getMainStacks().size() - 1; i >= 0; i--) {
			ItemStack stack = player.getInventory().getMainStacks().get(i);

			if (stack.isEmpty()) {
				continue;
			}

			Fish fish = Fish.getFish(stack);
			if (fish != null
					&& Objects.equals(fish.catcher, player.getUuid())
					&& !trackFishList.contains(fish.id)) {
				trackFishList.add(fish.id);
			}
		}
	}

	private void sendToTitleHud(Fish fish, Text icon, Text name) {
		// Send to TitleHud
		List<Text> title = new ArrayList<>();
		title.add(icon.copy().formatted(Formatting.WHITE));
		title.add(Text.empty());
		// Render the rarity tag as the compact first-letter square, matching chat/tab/tooltips
		// (only when "Simplified tag icons" is on, otherwise keep the server's original glyph).
		title.add(SimpleTagFont.apply(name, config.cleanerDisplay.simpleRankTags, config.cleanerDisplay.simpleRarityTags));
		title.add(fish.size.TAG);
		if (FullInventoryHandler.instance().slotsLeft == 0) {
			title.add(Text.literal("Inventory Full!").formatted(Formatting.RED));
		}
		List<Text> subtitle = new ArrayList<>();
		if (config.fishTracker.fishTrackerToggles.otherToggles.showStatsOnCatch) {
			if (config.titlePopup.showWeight) {
				subtitle.add(Text.literal("ᴡᴇɪɢʜᴛ").formatted(Formatting.BOLD).withColor(0xFFFFFF));
				subtitle.add(TextHelper.concat(
						Text.literal(TextHelper.fmt(fish.weight, 2)),
						Text.literal("ʟʙ").withColor(0xAAAAAA),
						Text.literal(" (").withColor(0x555555),
						Text.literal(TextHelper.fmt(fish.weight * 0.453592f, 2)),
						Text.literal("ᴋɢ").withColor(0xAAAAAA),
						Text.literal(")").withColor(0x555555)).withColor(0xFFFFFF));
			}
			if (config.titlePopup.showLength) {
				subtitle.add(Text.literal("ʟᴇɴɢᴛʜ").formatted(Formatting.BOLD).withColor(0xFFFFFF));
				subtitle.add(TextHelper.concat(
						Text.literal(TextHelper.fmt(fish.length, 2)),
						Text.literal("ɪɴ").withColor(0xAAAAAA),
						Text.literal(" (").withColor(0x555555),
						Text.literal(TextHelper.fmt(fish.length * 2.54f, 2)),
						Text.literal("ᴄᴍ").withColor(0xAAAAAA),
						Text.literal(")").withColor(0x555555)).withColor(0xFFFFFF));
			}
		}

		TitleHandler.instance().setTitleHud(title,
				config.fishTracker.fishTrackerToggles.otherToggles.showStatsOnCatchTime * 1000L,
				MinecraftClient.getInstance(), subtitle);
	}

	public void onFishCaughtSendDryStreak(Fish fish) {
		List<Text> lines = new ArrayList<>();

		if (fish.rarity == Constant.COMMON
				&& config.fishTracker.dryStreakMessageToggles.rarityMessageToggles.showCommon ||
				fish.rarity == Constant.RARE
						&& config.fishTracker.dryStreakMessageToggles.rarityMessageToggles.showRare
				||
				fish.rarity == Constant.EPIC
						&& config.fishTracker.dryStreakMessageToggles.rarityMessageToggles.showEpic
				||
				fish.rarity == Constant.LEGENDARY
						&& config.fishTracker.dryStreakMessageToggles.rarityMessageToggles.showLegendary
				||
				fish.rarity == Constant.MYTHICAL
						&& config.fishTracker.dryStreakMessageToggles.rarityMessageToggles.showMythical) {

			lines.add(buildFishDryStreakLine(fish.rarity,
					ProfileDataHandler.instance().profileData.rarityDryStreak.getOrDefault(fish.rarity, 0)));
		}

		if (fish.size == Constant.BABY
				&& config.fishTracker.dryStreakMessageToggles.sizeMessageToggles.showBaby ||
				fish.size == Constant.JUVENILE
						&& config.fishTracker.dryStreakMessageToggles.sizeMessageToggles.showJuvenile
				||
				fish.size == Constant.ADULT
						&& config.fishTracker.dryStreakMessageToggles.sizeMessageToggles.showAdult
				||
				fish.size == Constant.LARGE
						&& config.fishTracker.dryStreakMessageToggles.sizeMessageToggles.showLarge
				||
				fish.size == Constant.GIGANTIC
						&& config.fishTracker.dryStreakMessageToggles.sizeMessageToggles.showGigantic) {

			lines.add(buildFishDryStreakLine(fish.size,
					ProfileDataHandler.instance().profileData.fishSizeDryStreak.getOrDefault(fish.size, 0)));
		}

		if (fish.variant == Constant.ALBINO
				&& config.fishTracker.dryStreakMessageToggles.variantMessageToggles.showAlbino ||
				fish.variant == Constant.MELANISTIC
						&& config.fishTracker.dryStreakMessageToggles.variantMessageToggles.showMelanistic
				||
				fish.variant == Constant.TROPHY
						&& config.fishTracker.dryStreakMessageToggles.variantMessageToggles.showTrophy
				||
				fish.variant == Constant.FABLED
						&& config.fishTracker.dryStreakMessageToggles.variantMessageToggles.showFabled) {

			lines.add(buildFishDryStreakLine(fish.variant,
					ProfileDataHandler.instance().profileData.variantDryStreak.getOrDefault(fish.variant, 0)));
		}

		// Send all of this catch's dry-streak lines as ONE chat message (joined by newlines) so a
		// copy-message keybind from another mod grabs every line at once, while they still render on
		// separate lines.
		sendCombinedDryStreakMessage(lines);
	}

	private Text buildFishDryStreakLine(Constant fish, int lastCaught) {
		boolean showText = config.fishTracker.dryStreakMessageToggles.showText;
		TextDisplayHandler.TextDisplay formatting = config.fishTracker.dryStreakMessageToggles.textCapitalization;
		Text fishText = fish.TAG;
		String lower;
		boolean useAn;

		if (!(config.fishTracker.dryStreakMessageToggles.textCapitalization == TextDisplayHandler.TextDisplay.OFF)) {
			fishText = showText ? switch (fish) {
				// Rarities
				case COMMON -> Text.literal(TextDisplayHandler.formatText("Common", formatting)).withColor(0xFFFFFF);
				case RARE -> Text.literal(TextDisplayHandler.formatText("Rare", formatting)).withColor(0x2B85C4);
				case EPIC -> Text.literal(TextDisplayHandler.formatText("Epic", formatting)).withColor(0x1CD832);
				case LEGENDARY ->
					Text.literal(TextDisplayHandler.formatText("Legendary", formatting)).withColor(0xD98103);
				case MYTHICAL ->
					Text.literal(TextDisplayHandler.formatText("Mythical", formatting)).withColor(0xC93832);

				// Sizes
				case BABY -> Text.literal(TextDisplayHandler.formatText("Baby", formatting)).withColor(0x468CE7);
				case JUVENILE ->
					Text.literal(TextDisplayHandler.formatText("Juvenile", formatting)).withColor(0x22EA08);
				case ADULT -> Text.literal(TextDisplayHandler.formatText("Adult", formatting)).withColor(0x1C7DA0);
				case LARGE -> Text.literal(TextDisplayHandler.formatText("Large", formatting)).withColor(0xFF9000);
				case GIGANTIC ->
					Text.literal(TextDisplayHandler.formatText("Gigantic", formatting)).withColor(0xAF3333);

				// Variants
				case ALBINO -> Text.literal(TextDisplayHandler.formatText("Albino", formatting)).withColor(0xC6C3A1);
				case MELANISTIC ->
					Text.literal(TextDisplayHandler.formatText("Melanistic", formatting)).withColor(0x1C1C1C);
				case TROPHY -> Text.literal(TextDisplayHandler.formatText("Trophy", formatting)).withColor(0xD8C13C);
				case FABLED -> Text.literal(TextDisplayHandler.formatText("Fabled", formatting)).withColor(0xCE2326);

				default -> fish.TAG;
			} : fish.TAG;

			lower = fish.toString().toLowerCase(Locale.ROOT).trim();
		} else {
			lower = fish.ID.toLowerCase(Locale.ROOT).trim();
		}
		useAn = !lower.isEmpty() && "aeiou".indexOf(lower.charAt(0)) >= 0;
		return buildDryStreakLine(fishText, useAn ? "an " : "a ", lastCaught);
	}

	private void sendItemDryStreakMessage(String item, int lastCaught) {
		Constant constant = Constant.valueOfId(item);
		Text itemText = (constant != Constant.DEFAULT) ? constant.TAG.copy() : Text.literal(item);

		String lower = item.toLowerCase(Locale.ROOT).trim();
		boolean useAn = !lower.isEmpty() && "aeiou".indexOf(lower.charAt(0)) >= 0;
		sendDryStreakMessage(itemText, useAn ? "an " : "a ", lastCaught);
	}

	private void sendDryStreakMessage(Text typeText, String article, int lastCaught) {
		var client = MinecraftClient.getInstance();
		if (client.player != null) {
			client.inGameHud.getChatHud().addMessage(buildDryStreakLine(typeText, article, lastCaught));
		}
	}

	/// Builds one "You went N fish dry before catching a X" dry-streak chat line.
	private Text buildDryStreakLine(Text typeText, String article, int lastCaught) {
		int dryAmount = Math.max(0, ProfileDataHandler.instance().profileData.allFishCaughtCount - lastCaught - 1);
		return TextHelper.concat(
				Text.literal("FOE ").formatted(Formatting.DARK_GREEN, Formatting.BOLD),
				Text.literal("» ").formatted(Formatting.DARK_GRAY),
				Text.literal("You went ").formatted(Formatting.GRAY),
				Text.literal(TextHelper.fmnt(dryAmount)).formatted(Formatting.YELLOW),
				Text.literal(" fish dry before catching " + article).formatted(Formatting.GRAY),
				typeText);
	}

	/// Sends several dry-streak lines as a single chat message (newline-separated) so another mod's
	/// copy-message keybind grabs every line at once instead of only the one clicked. Still renders
	/// on separate lines in chat.
	private void sendCombinedDryStreakMessage(List<Text> lines) {
		if (lines.isEmpty()) {
			return;
		}
		var client = MinecraftClient.getInstance();
		if (client.player == null) {
			return;
		}
		List<Text> parts = new ArrayList<>();
		for (int i = 0; i < lines.size(); i++) {
			if (i > 0) {
				parts.add(Text.literal("\n"));
			}
			parts.add(lines.get(i));
		}
		client.inGameHud.getChatHud().addMessage(TextHelper.concat(parts.toArray(new Text[0])));
	}
}
