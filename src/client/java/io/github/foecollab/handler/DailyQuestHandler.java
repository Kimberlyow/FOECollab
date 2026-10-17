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
    // Layout since the 2026-10-11 server update: "- <rod> Goal [check]" then "<corner> (progress/needed)" on the next line,
    // under section headers such as "Base Missions" or "Lofoten Islands Missions".
    private static final Pattern MISSION_NAME_PATTERN = Pattern.compile("^\\s*-\\s+\\x{1F3A3}\\s+(?<goal>.*?)\\s*(?<done>\\u2714)?\\s*$");
    private static final Pattern MISSION_PROGRESS_PATTERN = Pattern.compile("^\\s*\\u2517\\s*\\((?<progress>[\\d,]+)\\s*/\\s*(?<needed>[\\d,]+)\\)\\s*$");
    private static final Pattern MISSION_SECTION_PATTERN = Pattern.compile("^\\s*(?<name>\\S.*?) Missions\\s*$");
    /** Last character of the tooltip frame glyphs that start every lore line. */
    private static final char LORE_FRAME_END = '\uEEE2';
    public static final String ANYWHERE_GOAL = "Anywhere";
    /** The mythical rarity glyph, the goal of the mythical catch mission. */
    public static final String MYTHICAL_GOAL = "\uF037";

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
                            quests.addAll(parseQuests(loreLines.stream().map(Text::getString).toList()));
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

    /**
     * Unfinished missions of the Daily Missions tooltip, in order. Reads the two line layout and falls back to the old one line
     * layout ({@code | Goal (progress/needed)}).
     */
    static List<Quest> parseQuests(List<String> lore) {
        List<Quest> out = new ArrayList<>();
        String section = "";
        String goal = null;
        boolean goalDone = false;
        for (String raw : lore) {
            String line = raw.substring(raw.lastIndexOf(LORE_FRAME_END) + 1);

            Matcher name = MISSION_NAME_PATTERN.matcher(line);
            if (name.matches()) {
                goal = name.group("goal");
                goalDone = name.group("done") != null;
                continue;
            }

            Matcher progress = MISSION_PROGRESS_PATTERN.matcher(line);
            if (goal != null && progress.matches()) {
                Quest quest = new Quest(goal, parseCount(progress.group("progress")), parseCount(progress.group("needed")), out.size() + 1, section);
                if (!goalDone && !quest.questDone() && !goal.isBlank()) {
                    out.add(quest);
                }
                goal = null;
                continue;
            }

            Matcher header = MISSION_SECTION_PATTERN.matcher(line);
            if (header.matches() && !"Daily".equals(header.group("name"))) {
                section = line.strip();
                continue;
            }

            Optional<Quest> oldFormat = parseQuestLine(line, out.size() + 1);
            if (oldFormat.isPresent() && !oldFormat.get().questDone()) {
                out.add(oldFormat.get());
            }
        }
        return out;
    }

    private static int parseCount(String count) {
        return Integer.parseInt(count.replace(",", ""));
    }

    private static Optional<Quest> parseQuestLine(String loreLine, int slot) {
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
        /** Header the mission sits under in the tooltip ("Base Missions"), empty when unknown. */
        public final String section;

        public Quest(String goal, int progress, int needed, int slot){
            this(goal, progress, needed, slot, "");
        }

        public Quest(String goal, int progress, int needed, int slot, String section){
            this.section = section;
            this.goal = goal;
            this.progress = progress;
            this.needed = needed;
            this.slot = slot;
            this.isStarted = true;
            this.questDone = this.needed > 0 && this.progress >= this.needed;
        }

        public Quest(int slot) {
            this.section = "";
            this.slot = slot;
            this.isStarted = false;
        }

        public Quest(boolean questDone, int slot) {
            this.section = "";
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
