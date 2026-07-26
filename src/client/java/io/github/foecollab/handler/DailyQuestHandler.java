package io.github.foecollab.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DailyQuestHandler {
    private static DailyQuestHandler INSTANCE = new DailyQuestHandler();
    private static final Pattern DAILY_QUEST_PATTERN = Pattern.compile("\\|\\s*(?<goal>.+?)\\s*\\((?<progress>\\d+)\\s*/\\s*(?<needed>\\d+)\\)");

    public List<Quest> quests = new ArrayList<>();
    public boolean questMenuState = false;
    private boolean hasInitialized = false;

    public static DailyQuestHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new DailyQuestHandler();
        }
        return INSTANCE;
    }

	public boolean onReceiveMessage(Text message) {
		if (!LoadingHandler.instance().isOnServer) {
			return false;
		}

		String plain = message.getString();

        if (plain.startsWith("QUEST Complete [")) {
            updateQuest("Quests Completed");

        } else if (plain.startsWith("DAILY MISSIONS » Contest Participation")) {
            updateQuest("Contest Participation");
        }

		return false;
	}

    public void tick(MinecraftClient minecraftClient) {
        if (questMenuState && minecraftClient.player != null) {
                quests.clear();
            
                for (int i = 0; i < minecraftClient.player.currentScreenHandler.slots.size(); i++) {
                ItemStack itemStack = minecraftClient.player.currentScreenHandler.getSlot(i).getStack().copy();
                if (minecraftClient.player.currentScreenHandler.getSlot(i).inventory != minecraftClient.player
                        .getInventory()) {
                        if (itemStack.getItem() == Items.END_CRYSTAL
                                && "Daily Missions".equals(itemStack.getName().getString())) {
                            hasInitialized = true;
                            List<Text> loreLines = Objects.requireNonNull(itemStack.get(DataComponentTypes.LORE)).lines();
                        int questIndex = 1;
                        for (Text lore : loreLines) {
                            String loreLine = lore.getString();
                            Optional<Quest> parsedQuest = parseQuestLine(loreLine, questIndex);
                            if (parsedQuest.isPresent() && !parsedQuest.get().questDone()) {
                                quests.add(parsedQuest.get());
                                questIndex++;
                            }
                        }
                    }
                }
            }
        }
    }

    public void updateQuest(String goal) {
        if (goal == null || goal.isBlank()) {
            return;
        }

        Quest matchedQuest = findQuestByGoal(goal);
        if (matchedQuest == null) {
            return;
        }

        matchedQuest.incrementProgress();
        if (matchedQuest.questDone()) {
            quests.remove(matchedQuest);
        }
    }

    private Quest findQuestByGoal(String goal) {
        Quest match = null;
        for (Quest quest : quests) {
            if (quest.goal == null || quest.goal.isBlank()) {
                continue;
            }

            if (quest.goal.equals(goal)) {
                return quest;
            }
                        if (match == null && quest.goal.contains(goal)) {
                match = quest;
            }
        }
        return match;
    }

    private Optional<Quest> parseQuestLine(String loreLine, int slot) {
        if (!loreLine.contains("|") || !loreLine.contains("(") || !loreLine.contains(")")) {
            return Optional.empty();
        }

        Matcher matcher = DAILY_QUEST_PATTERN.matcher(loreLine);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String goal = matcher.group("goal").trim();
        int progress = Integer.parseInt(matcher.group("progress"));
        int needed = Integer.parseInt(matcher.group("needed"));
        return Optional.of(new Quest(goal, progress, needed, slot));
    }

    public boolean isDailyQuestInitialized() {
        return hasInitialized;
    }

    public static class Quest {
        public String goal;
        public int progress;
        public int needed;
        private boolean questDone;
        public final boolean isStarted;
        public final int slot;

        public Quest(String goal, int progress, int needed, int slot){
            this.goal = goal;
            this.progress = progress;
            this.needed = needed;
            this.slot = slot;
            this.isStarted = true;
            this.questDone = this.needed > 0 && this.progress >= this.needed;
        }

        public Quest(int slot) {
            this.slot = slot;
            this.isStarted = false;
        }

        public Quest(boolean questDone, int slot) {
            this.questDone = questDone;
            this.slot = slot;
            this.isStarted = true;
        }

        public void incrementProgress() {
            this.progress++;
            this.questDone = this.needed > 0 && this.progress >= this.needed;
        }

        public boolean questDone() {
            return (this.needed > 0 && this.progress >= this.needed) || questDone;
        }
    }
}
