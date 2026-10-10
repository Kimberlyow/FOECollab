package io.github.foecollab.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Turns the baits and lures found in a set of slots into a list of click groups. Pure logic with
/// no Minecraft types so it can be unit tested. Every group is a complete operation that leaves
/// the cursor empty, so the caller may stop between groups.
public final class SortPlanner {
    public static final String BAIT = "bait";
    public static final String LURE = "lure";

    /// Rarities from best to worst. Mythic is left out of sorting on purpose (too rare to matter).
    private static final List<String> RARITY_ORDER = List.of("legendary", "epic", "rare", "common");

    /// One bait or lure in a slot. {@code slot} is the container slot id used in click packets.
    public record Entry(int slot, String type, String name, String rarity) {}

    /// A click on a slot: a plain pickup click, or a shift click (quick move).
    public record Click(int slot, boolean shift) {
        public static Click pick(int slot) {
            return new Click(slot, false);
        }

        public static Click shift(int slot) {
            return new Click(slot, true);
        }
    }

    private SortPlanner() {}

    public static boolean isSortable(Entry e) {
        return (BAIT.equals(e.type()) || LURE.equals(e.type())) && RARITY_ORDER.contains(e.rarity());
    }

    private static List<Entry> sortable(List<Entry> found) {
        List<Entry> entries = new ArrayList<>();
        for (Entry e : found) {
            if (isSortable(e)) entries.add(e);
        }
        entries.sort(Comparator.comparingInt(Entry::slot));
        return entries;
    }

    /// Merge same-name baits (when {@code merge}) and lay everything out by rarity (when
    /// {@code sort}, common first when {@code commonFirst}) within one group of slots. Merging only works where baits can stack, which
    /// is the player inventory; in a chest or vault pass {@code merge = false}.
    public static List<List<Click>> plan(List<Entry> found, boolean merge, boolean sort, boolean commonFirst) {
        List<Entry> entries = sortable(found);
        List<List<Click>> groups = new ArrayList<>();

        // Merge baits with the same name into the lowest slot. Picking one up and clicking
        // another bait adds their stock on the server. Lures never merge.
        Map<String, Entry> mergeTarget = new HashMap<>();
        List<Entry> kept = new ArrayList<>();
        List<Integer> freed = new ArrayList<>();
        for (Entry e : entries) {
            if (!merge || !BAIT.equals(e.type())) {
                kept.add(e);
                continue;
            }
            Entry target = mergeTarget.get(e.name());
            if (target == null) {
                mergeTarget.put(e.name(), e);
                kept.add(e);
            } else {
                groups.add(List.of(Click.pick(e.slot()), Click.pick(target.slot())));
                freed.add(e.slot());
            }
        }
        if (!sort) return groups;

        // Lay the survivors out in rarity order in the lowest slots this sort touches.
        List<Integer> slots = new ArrayList<>();
        for (Entry e : kept) slots.add(e.slot());
        slots.addAll(freed);
        slots.sort(Comparator.naturalOrder());

        List<Entry> desired = new ArrayList<>(kept);
        desired.sort(Comparator
                .comparingInt((Entry e) -> commonFirst
                        ? -RARITY_ORDER.indexOf(e.rarity()) : RARITY_ORDER.indexOf(e.rarity()))
                .thenComparing(e -> BAIT.equals(e.type()) ? 0 : 1)
                .thenComparing(Entry::name)
                .thenComparingInt(Entry::slot));

        Map<Integer, Entry> state = new HashMap<>();
        for (Entry e : kept) state.put(e.slot(), e);

        for (int i = 0; i < desired.size(); i++) {
            int target = slots.get(i);
            Entry want = desired.get(i);
            Entry there = state.get(target);
            if (there == want) continue;
            int from = -1;
            for (var en : state.entrySet()) {
                if (en.getValue() == want) {
                    from = en.getKey();
                    break;
                }
            }
            if (there == null) {
                groups.add(List.of(Click.pick(from), Click.pick(target)));
                state.remove(from);
            } else {
                groups.add(List.of(Click.pick(from), Click.pick(target), Click.pick(from)));
                state.put(from, there);
            }
            state.put(target, want);
        }
        return groups;
    }

    /// For every bait in the inventory that has a same-name bait in the chest or vault: pick the
    /// chest bait up, click it onto the inventory bait (stock adds up there), then shift click the
    /// merged bait back into the chest. The merge and the shift click are separate groups so the
    /// server can answer in between. Chest baits never merge with each other.
    public static List<List<Click>> planCrossMerge(List<Entry> chest, List<Entry> inventory) {
        Map<String, Entry> inChest = new HashMap<>();
        for (Entry e : sortable(chest)) {
            if (BAIT.equals(e.type())) inChest.putIfAbsent(e.name(), e);
        }
        List<List<Click>> groups = new ArrayList<>();
        for (Entry inv : sortable(inventory)) {
            Entry match = BAIT.equals(inv.type()) ? inChest.get(inv.name()) : null;
            if (match == null) continue;
            groups.add(List.of(Click.pick(match.slot()), Click.pick(inv.slot())));
            groups.add(List.of(Click.shift(inv.slot())));
        }
        return groups;
    }
}
