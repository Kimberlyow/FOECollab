package io.github.foecollab.screens.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Scrollable reference of every custom-HUD {@code %placeholder%} (and the function/condition codes),
 * grouped under category headers. Each row shows the description on top with the token beneath it;
 * left-clicking a token fires {@code onSelect} (the screen copies it to the clipboard). The list can
 * be narrowed with {@link #applyFilter(String)} — a substring match on the token or its description,
 * e.g. {@code %sco} keeps the scoreboard variables and {@code wall} finds the wallet. The catalog mirrors
 * {@link io.github.foecollab.customhud.PlaceholderSources} and
 * {@link io.github.foecollab.customhud.PlaceholderEngine}.
 */
public class VariableListWidget extends ElementListWidget<VariableListWidget.Entry> {
    private static final List<Category> CATALOG = buildCatalog();

    private final Consumer<String> onSelect;

    public VariableListWidget(MinecraftClient client, int width, int height, int top, Consumer<String> onSelect) {
        super(client, width, height, top, 28);
        this.onSelect = onSelect;
        applyFilter("");
    }

    @Override
    public int getRowWidth() {
        return Math.min(380, this.width - 20);
    }

    @Override
    protected int getScrollbarX() {
        return this.getX() + this.width - 6;
    }

    /** Rebuilds the visible rows, keeping only variables whose token or description contains {@code query}. */
    public void applyFilter(String query) {
        clearEntries();
        int rowWidth = getRowWidth();
        String q = query == null ? "" : query.trim().toLowerCase();
        for (Category cat : CATALOG) {
            List<Var> matches = new ArrayList<>();
            for (Var v : cat.vars()) {
                if (matches(v.token(), v.description(), q)) {
                    matches.add(v);
                }
            }
            if (!matches.isEmpty()) {
                addEntry(new HeaderEntry(cat.title()));
                for (Var v : matches) {
                    addEntry(new VarEntry(v.token(), v.description(), rowWidth, onSelect));
                }
            }
        }
    }

    /**
     * Substring match against the token or its human description, lenient about the leading
     * {@code %} on the token. So {@code %sco} and {@code sco} both hit the scoreboard tokens, and
     * {@code wall} surfaces {@code %scoreboard.wallet%} (its description is "Wallet balance").
     */
    private static boolean matches(String token, String description, String q) {
        if (q.isEmpty()) {
            return true;
        }
        String t = token.toLowerCase();
        if (t.contains(q)) {
            return true;
        }
        String tNoPct = t.startsWith("%") ? t.substring(1) : t;
        String qNoPct = q.startsWith("%") ? q.substring(1) : q;
        if (tNoPct.contains(qNoPct)) {
            return true;
        }
        return description.toLowerCase().contains(q);
    }

    private static List<Category> buildCatalog() {
        List<Category> c = new ArrayList<>();
        c.add(new Category("Player", List.of(
                new Var("%player.name%", "Your username"),
                new Var("%player.health%", "Current health points"),
                new Var("%player.max_health%", "Maximum health"),
                new Var("%player.hunger%", "Food level (0-20)"),
                new Var("%player.saturation%", "Saturation level"),
                new Var("%player.armor%", "Armor points"),
                new Var("%player.air%", "Remaining air"),
                new Var("%player.level%", "Experience level"),
                new Var("%player.xp_progress%", "Percent toward next level"),
                new Var("%player.x%", "Block X coordinate"),
                new Var("%player.y%", "Block Y coordinate"),
                new Var("%player.z%", "Block Z coordinate"),
                new Var("%player.exact_x%", "Precise X (1 decimal)"),
                new Var("%player.exact_y%", "Precise Y (1 decimal)"),
                new Var("%player.exact_z%", "Precise Z (1 decimal)"),
                new Var("%player.direction%", "Facing direction (N/E/S/W)"),
                new Var("%player.yaw%", "Look yaw"),
                new Var("%player.pitch%", "Look pitch"),
                new Var("%player.dimension%", "Current dimension"),
                new Var("%player.held_item%", "Name of held item"))));

        c.add(new Category("Scoreboard (FishOnMC)", List.of(
                new Var("%scoreboard.name%", "Your name"),
                new Var("%scoreboard.level%", "Your level"),
                new Var("%scoreboard.wallet%", "Wallet balance"),
                new Var("%scoreboard.credits%", "Credits"),
                new Var("%scoreboard.catches%", "Catches"),
                new Var("%scoreboard.catch_rate%", "Catch rate"),
                new Var("%scoreboard.crew_name%", "Your crew's name"),
                new Var("%scoreboard.crew_level%", "Your crew's level"),
                new Var("%scoreboard.location_min%", "Location range (min)"),
                new Var("%scoreboard.location_max%", "Location range (max)"))));

        c.add(new Category("Stats (session / all-time)", List.of(
                new Var("%stats.fish_caught%", "Fish caught this session"),
                new Var("%stats.all_fish_caught%", "Fish caught all-time"),
                new Var("%stats.total_xp%", "XP this session"),
                new Var("%stats.all_total_xp%", "XP all-time"),
                new Var("%stats.total_value%", "Value earned this session"),
                new Var("%stats.all_total_value%", "Value earned all-time"),
                new Var("%stats.pets%", "Pets this session"),
                new Var("%stats.all_pets%", "Pets all-time"),
                new Var("%stats.shards%", "Shards this session"),
                new Var("%stats.all_shards%", "Shards all-time"),
                new Var("%stats.quests%", "Quests completed this session"),
                new Var("%stats.all_quests%", "Quests completed all-time"),
                new Var("%stats.lightning_bottles%", "Lightning bottles (session)"),
                new Var("%stats.infusion_capsules%", "Infusion capsules (session)"),
                new Var("%stats.fish_time%", "Session timer (HH:MM:SS)"),
                new Var("%stats.fish_time_seconds%", "Session time in seconds"))));

        c.add(new Category("Detailed stats (all-time)", List.of(
                new Var("%stats_data.data.fish.total%", "Total fish caught"),
                new Var("%stats_data.data.fish.rarity.common.count%", "Count of a rarity — swap common (rare/epic/legendary/mythical)"),
                new Var("%stats_data.data.fish.rarity.common.dry_streak%", "Catches since last of that rarity"),
                new Var("%stats_data.data.fish.size.gigantic.count%", "Count of a size — swap gigantic (baby/juvenile/adult/large)"),
                new Var("%stats_data.data.fish.size.gigantic.dry_streak%", "Catches since last of that size"),
                new Var("%stats_data.data.fish.variant.albino.count%", "Count of a variant — swap albino (melanistic/trophy/fabled)"),
                new Var("%stats_data.data.fish.variant.albino.dry_streak%", "Catches since last of that variant"),
                new Var("%stats_data.data.pet.total%", "Pets fished up"),
                new Var("%stats_data.data.pet.quest%", "Pets from quests"),
                new Var("%stats_data.data.pet.both%", "Pets total (fished + quest)"),
                new Var("%stats_data.data.pet.dry_streak%", "Catches since last pet"),
                new Var("%stats_data.data.item.armorShard.count%", "Armor shards fished up"),
                new Var("%stats_data.data.item.armorShard.quest%", "Armor shards from quests"),
                new Var("%stats_data.data.item.armorShard.both%", "Armor shards total"),
                new Var("%stats_data.data.item.armorShard.dry_streak%", "Catches since last shard"),
                new Var("%stats_data.data.item.lightningBottle.count%", "Lightning bottles caught"),
                new Var("%stats_data.data.item.lightningBottle.dry_streak%", "Catches since last lightning bottle"),
                new Var("%stats_data.data.item.infusionCapsule.count%", "Infusion capsules caught"),
                new Var("%stats_data.data.item.infusionCapsule.dry_streak%", "Catches since last infusion capsule"))));

        c.add(new Category("Labels & glyphs", List.of(
                new Var("%constant_data.data.fish.rarity.common%", "Rarity glyph — swap common"),
                new Var("%constant_data.data.fish.size.gigantic%", "Size label — swap gigantic"),
                new Var("%constant_data.data.fish.variant.albino%", "Variant glyph — swap albino"))));

        c.add(new Category("Location & boss bar", List.of(
                new Var("%boss_bar.location%", "Current location tag"),
                new Var("%boss_bar.time%", "In-game time of day"),
                new Var("%boss_bar.weather%", "Current weather"),
                new Var("%boss_bar.temperature%", "Current temperature"),
                new Var("%location.name%", "Location name"),
                new Var("%location.id%", "Location id"))));

        c.add(new Category("Tab list", List.of(
                new Var("%tab.player%", "Tab header player info"),
                new Var("%tab.instance%", "Instance name"),
                new Var("%tab.is_instance%", "In an instance? (true/false)"),
                new Var("%tab.count%", "Players online"))));

        c.add(new Category("World", List.of(
                new Var("%world.time%", "World time (0-24000)"),
                new Var("%world.day%", "World day number"),
                new Var("%world.raining%", "Is it raining? (true/false)"),
                new Var("%world.thundering%", "Is it thundering? (true/false)"))));

        c.add(new Category("Real-world clock", List.of(
                new Var("%time.clock%", "Clock HH:MM (24h)"),
                new Var("%time.clock12%", "Clock h:MM AM/PM"),
                new Var("%time.hour%", "Hour (24h)"),
                new Var("%time.hour12%", "Hour (12h)"),
                new Var("%time.minute%", "Minute"),
                new Var("%time.second%", "Second"),
                new Var("%time.ampm%", "AM or PM"),
                new Var("%time.date%", "Date YYYY-MM-DD"))));

        c.add(new Category("Conditions & functions (hide a line when false, or compute a value)", List.of(
                new Var("%condition.(<player.health><10)%&cLow HP!", "Show the rest of the line only when the test passes"),
                new Var("%is_blank.(<scoreboard.crew_name>)%", "True when the value is empty"),
                new Var("%is_not_blank.(<scoreboard.crew_name>)%", "True when the value is not empty"),
                new Var("%not.(<is_blank.(<scoreboard.crew_name>)>)%", "Invert a true/false value"),
                new Var("%and.(<a>,<b>)%", "True when both sides are true"),
                new Var("%or.(<a>,<b>)%", "True when either side is true"),
                new Var("%xor.(<a>,<b>)%", "True when exactly one side is true"),
                new Var("%round.(<player.xp_progress>,1)%", "Round a number to N decimals"),
                new Var("%min.(<a>,<b>)%", "Smaller of two numbers"),
                new Var("%max.(<a>,<b>)%", "Larger of two numbers"),
                new Var("%abs.(<x>)%", "Absolute value"),
                new Var("%ceil.(<x>)%", "Round up"),
                new Var("%floor.(<x>)%", "Round down"),
                new Var("%expression.(<stats.fish_caught>+1)%", "Arithmetic (+ - * /)"),
                new Var("%substring_front.(<player.name>,3)%", "First N characters"),
                new Var("%substring_back.(<player.name>,3)%", "Characters from index N"),
                new Var("%index_of.(<player.name>,a)%", "Position of a substring"))));

        c.add(new Category("Formatting", List.of(
                new Var("&", "Colour/format codes: &a green, &c red, &e yellow, &l bold, &o italic, &r reset"),
                new Var("\\%", "A literal percent sign"))));
        return c;
    }

    public record Var(String token, String description) {
    }

    private record Category(String title, List<Var> vars) {
    }

    public abstract static class Entry extends ElementListWidget.Entry<Entry> {
    }

    /** A non-clickable category title. */
    public static class HeaderEntry extends Entry {
        private final Text title;

        HeaderEntry(String title) {
            this.title = Text.literal(title).formatted(Formatting.GOLD, Formatting.BOLD);
        }

        @Override
        public List<? extends Element> children() {
            return List.of();
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of();
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float delta) {
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            context.drawText(tr, title, getX(), getY() + 11, 0xFFFFFFFF, true);
        }
    }

    /** A clickable variable: its description on top, the token (button) beneath. Click copies the token. */
    public static class VarEntry extends Entry {
        private final ButtonWidget button;
        private final String description;
        private final int rowWidth;

        VarEntry(String token, String description, int rowWidth, Consumer<String> onSelect) {
            this.description = description;
            this.rowWidth = rowWidth;
            this.button = ButtonWidget.builder(Text.literal(token).formatted(Formatting.YELLOW), b -> onSelect.accept(token))
                    .dimensions(0, 0, rowWidth, 14)
                    .tooltip(Tooltip.of(Text.literal("Click to copy").formatted(Formatting.GRAY)))
                    .build();
        }

        @Override
        public List<? extends Element> children() {
            return List.of(button);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(button);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float delta) {
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            String desc = tr.trimToWidth(description, rowWidth - 4);
            context.drawText(tr, Text.literal(desc).formatted(Formatting.GRAY), getX() + 2, getY() + 2, 0xFFFFFFFF, false);
            button.setX(getX());
            button.setY(getY() + 12);
            button.setWidth(rowWidth);
            button.render(context, mouseX, mouseY, delta);
        }
    }
}
