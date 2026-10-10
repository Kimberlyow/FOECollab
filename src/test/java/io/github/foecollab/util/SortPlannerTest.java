package io.github.foecollab.util;

import io.github.foecollab.util.SortPlanner.Click;
import io.github.foecollab.util.SortPlanner.Entry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static io.github.foecollab.util.SortPlanner.plan;
import static io.github.foecollab.util.SortPlanner.planCrossMerge;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SortPlannerTest {
    private static Entry bait(int slot, String name, String rarity) {
        return new Entry(slot, "bait", name, rarity);
    }

    private static Entry lure(int slot, String name, String rarity) {
        return lure(slot, name, rarity, "Bluegill", "1/4oz");
    }

    private static Entry lure(int slot, String name, String rarity, String color, String size) {
        return new Entry(slot, "lure", name, rarity, color, size);
    }

    private static List<Click> picks(int... slots) {
        List<Click> l = new ArrayList<>();
        for (int s : slots) l.add(Click.pick(s));
        return l;
    }

    @Test
    void alreadySortedNeedsNoClicks() {
        var found = List.of(bait(9, "a", "epic"), bait(10, "b", "rare"), bait(11, "c", "common"));
        assertTrue(plan(found, true, true, false).isEmpty());
    }

    @Test
    void duplicatesMergeIntoLowestSlot() {
        var found = List.of(bait(12, "worms", "common"), bait(9, "worms", "common"));
        assertEquals(List.of(picks(12, 9)), plan(found, true, false, false));
    }

    @Test
    void noMergeKeepsDuplicatesApart() {
        var found = List.of(bait(12, "worms", "common"), bait(9, "worms", "common"));
        assertTrue(plan(found, false, true, false).isEmpty());
    }

    @Test
    void commonFirstReversesTheRarityOrder() {
        var found = List.of(bait(9, "a", "legendary"), bait(10, "b", "epic"), bait(11, "c", "common"));
        // descending is already sorted; ascending reverses it: legendary and common swap
        assertTrue(plan(found, true, true, false).isEmpty());
        assertEquals(List.of(picks(11, 9, 11)), plan(found, true, true, true));
    }

    @Test
    void luresWithSameNameColorAndSizeMerge() {
        var found = List.of(lure(9, "crankbait", "rare"), lure(10, "crankbait", "rare"));
        assertEquals(List.of(picks(10, 9)), plan(found, true, true, false));
    }

    @Test
    void luresWithDifferentColorOrSizeNeverMerge() {
        var found = List.of(lure(9, "crankbait", "rare", "Bluegill", "1/4oz"),
                lure(10, "crankbait", "rare", "Tiger Crawl", "1/4oz"),
                lure(11, "crankbait", "rare", "Bluegill", "1/2oz"),
                lure(12, "popper", "rare", "Bluegill", "1/4oz"));
        assertTrue(plan(found, true, true, false).isEmpty());
    }

    @Test
    void luresWithoutColorOrSizeNeverMerge() {
        var found = List.of(new Entry(9, "lure", "crankbait", "rare"), new Entry(10, "lure", "crankbait", "rare"));
        assertTrue(plan(found, true, true, false).isEmpty());
    }

    @Test
    void mythicAndOtherItemsAreIgnored() {
        var found = List.of(bait(9, "x", "mythic"), bait(10, "y", "common"),
                new Entry(11, "", "", ""), bait(12, "z", "legendary"));
        // only y (common) and z (legendary) are sorted: legendary first, so they swap (slots 10 and 12)
        assertEquals(List.of(picks(12, 10, 12)), plan(found, true, true, false));
    }

    @Test
    void crossMergePicksChestBaitOntoInventoryBaitThenShiftsBack() {
        var chest = List.of(bait(3, "worms", "common"), bait(4, "chicken", "rare"));
        var inv = List.of(bait(60, "worms", "common"), bait(61, "nightcrawler", "common"));
        assertEquals(List.of(picks(3, 60), List.of(Click.shift(60))), planCrossMerge(chest, inv));
    }

    @Test
    void crossMergeSkipsUnmatchedBaitsAndLuresWithOtherColorOrSize() {
        var chest = List.of(lure(3, "crankbait", "rare", "Bluegill", "1/4oz"));
        var inv = List.of(lure(60, "crankbait", "rare", "Bluegill", "1/2oz"), bait(61, "worms", "common"));
        assertTrue(planCrossMerge(chest, inv).isEmpty());
    }

    @Test
    void crossMergeMatchesLuresByNameColorAndSize() {
        var chest = List.of(lure(3, "crankbait", "rare", "Tiger Crawl", "1/2oz"),
                lure(4, "crankbait", "rare", "Bluegill", "1/4oz"));
        var inv = List.of(lure(60, "crankbait", "rare", "Bluegill", "1/4oz"));
        assertEquals(List.of(picks(4, 60), List.of(Click.shift(60))), planCrossMerge(chest, inv));
    }

    // ---- integration: replay the whole flow on a fake chest + inventory ----

    /// Slots 0-8 are the chest, 9+ the inventory. Click rules the planner assumes: pickup on an
    /// empty cursor takes the item, then on an empty slot places it, on a same-name bait adds the
    /// stock, otherwise swaps; shift click on an inventory slot moves the item to the first
    /// empty chest slot (the server's choice).
    private static final class Fake {
        final TreeMap<Integer, Item> slots = new TreeMap<>();
        Item cursor;

        void apply(List<Click> group) {
            for (Click c : group) {
                if (c.shift()) {
                    Item it = slots.remove(c.slot());
                    for (int i = 0; i < 9; i++) {
                        if (!slots.containsKey(i)) {
                            slots.put(i, it);
                            break;
                        }
                    }
                    continue;
                }
                Item there = slots.get(c.slot());
                if (cursor == null) {
                    cursor = slots.remove(c.slot());
                } else if (there == null) {
                    slots.put(c.slot(), cursor);
                    cursor = null;
                } else if (c.slot() >= 9 && there.type.equals("bait") && there.name.equals(cursor.name)) {
                    there.stock += cursor.stock;
                    cursor = null;
                } else {
                    slots.put(c.slot(), cursor);
                    cursor = there;
                }
            }
            assertNull(cursor, "group must leave the cursor empty");
        }

        List<Entry> scan(boolean chest) {
            List<Entry> out = new ArrayList<>();
            slots.forEach((s, it) -> {
                if ((s < 9) == chest) out.add(new Entry(s, it.type, it.name, it.rarity));
            });
            return out;
        }

        void run(List<List<Click>> groups) {
            groups.forEach(this::apply);
        }

        void put(int slot, String name, String type, String rarity, int stock) {
            slots.put(slot, new Item(name, type, rarity, stock));
        }
    }

    @Test
    void fullFlowMergesIntoChestStashAndSortsBothSides() {
        Fake f = new Fake();
        f.put(0, "worms", "bait", "common", 600);
        f.put(1, "cut piranha", "bait", "rare", 645);
        f.put(2, "florida crawfish", "bait", "epic", 131);
        f.put(4, "crankbait", "lure", "rare", 30);
        f.put(20, "worms", "bait", "common", 45);
        f.put(21, "worms", "bait", "common", 5);
        f.put(22, "rotten chicken", "bait", "legendary", 1);
        f.put(23, "cut piranha", "bait", "rare", 10);
        f.put(24, "mystery", "bait", "mythic", 1);

        f.run(plan(f.scan(false), true, false, false));
        f.run(planCrossMerge(f.scan(true), f.scan(false)));
        f.run(plan(f.scan(false), true, true, false));
        f.run(plan(f.scan(true), false, true, false));

        // chest: stash merged, legendary/epic/rare/common order, lure after baits of its rarity
        List<String> chest = new ArrayList<>();
        f.scan(true).forEach(e -> chest.add(e.name() + ":" + f.slots.get(e.slot()).stock));
        assertEquals(List.of("florida crawfish:131", "cut piranha:655", "crankbait:30", "worms:650"), chest);
        // inventory keeps only what had no twin in the chest, mythic untouched
        List<String> inv = new ArrayList<>();
        f.scan(false).forEach(e -> inv.add(e.name()));
        assertEquals(List.of("rotten chicken", "mystery"), inv);
    }

    private static final class Item {
        final String name, type, rarity;
        int stock;

        Item(String name, String type, String rarity, int stock) {
            this.name = name;
            this.type = type;
            this.rarity = rarity;
            this.stock = stock;
        }
    }
}
