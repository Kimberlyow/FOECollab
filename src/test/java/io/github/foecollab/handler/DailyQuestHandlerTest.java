package io.github.foecollab.handler;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DailyQuestHandlerTest {
    private static final String FRAME = " 퀃 ";
    private static final String ROD = "🎣";

    private static List<String> lore() {
        return List.of(
                FRAME + "   Daily Missions",
                FRAME + "   Base Missions",
                FRAME + "   - " + ROD + " Anywhere  ",
                FRAME + "      ┗ (204/250)",
                FRAME + "   - " + ROD + " Quests Completed  ✔",
                FRAME + "      ┗ (3/3)",
                FRAME + " ",
                FRAME + "   Lofoten Islands Missions",
                FRAME + "   - " + ROD + " Lumpfish  ",
                FRAME + "      ┗ (5/14)",
                FRAME + "   - " + ROD + " Opah  ✔",
                FRAME + "      ┗ (4/4)",
                FRAME + "   - " + ROD + " Atlantic Bluefin Tuna  ",
                FRAME + "      ┗ (0/3)",
                FRAME + " ",
                FRAME + "   Rerolls in 20h 13m 53s");
    }

    @Test
    void parsesOnlyUnfinishedMissionsWithTheirSection() {
        List<DailyQuestHandler.Quest> quests = DailyQuestHandler.parseQuests(lore());
        assertEquals(List.of("Anywhere", "Lumpfish", "Atlantic Bluefin Tuna"), quests.stream().map(q -> q.goal).toList());
        assertEquals("Lofoten Islands Missions", quests.get(1).section);
        assertEquals(5, quests.get(1).progress);
        assertEquals(14, quests.get(1).needed);
    }

    @Test
    void catchByDisplayNameAdvancesLocationMission() {
        DailyQuestHandler handler = DailyQuestHandler.instance();
        handler.quests.clear();
        handler.quests.addAll(DailyQuestHandler.parseQuests(lore()));

        handler.updateQuest("Lumpfish");

        assertEquals(6, handler.quests.get(1).progress);
        assertTrue(handler.quests.get(2).progress == 0);
    }
}
