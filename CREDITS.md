# Credits & Third-Party Code

## Original mod

FOECollab is a community continuation of the original **FishOnMC-Extras** mod. All credit for
the original concept goes to its original author.

## FishOnMC-Extras-R (DannyPX) — GPL-3.0

Several features in FOECollab were **ported and adapted (i.e. modified)** from
**[DannyPX/FishOnMC-Extras-R](https://github.com/DannyPX/FishOnMC-Extras-R)** ("FOE-R"), used
**with the creator's explicit permission**.

FOE-R is licensed under the **GNU General Public License v3.0 (GPL-3.0)**. A full copy of that
license is included in [`LICENSE-GPL-3.0.txt`](LICENSE-GPL-3.0.txt).

The original FOE-R is Mojang-mapped (`dannypx.foe.*`); the ported code was rewritten for
FOECollab's Yarn mappings and architecture, so it is **modified** from the original. Per GPL-3.0
§5, this file serves as the prominent notice that the following were derived from FOE-R and
changed:

- **Small item-stack display** — compact stack counts that read the real bait/lure count from NBT.
- **Bait HUD `activeBait` fix** — read the equipped bait from the rod's `activeBait` list.
- **Armor quality % slot marker** — tier-coloured quality shown on identified armor.
- **Customizable inventory buttons** — the user-editable button list, the maker/editor screen, and
  the modern "box" button look. The button-box textures
  (`assets/foecollab/textures/gui/sprites/elements/box*_atlas.png`) are FOE-R's art assets,
  included here under GPL-3.0.
- **Armor crafter tooltip** — show who crafted/identified an armor piece while holding the
  extra-info keybind.
- **Custom HUDs ("custom codes")** — the user-creatable HUD system: the placeholder/function
  expression language (`%player.x%`, `%condition.(...)%`, `%substring_back.(...)%`, the math/string/
  boolean functions and the data-source placeholders) ported from FOE-R's `PlaceholderHandler` /
  `FunctionParser` / `CustomHud*`, plus a HUD maker/editor screen. Rewritten for FOECollab's data
  sources and rendered in FoE's own HUD style.

## Licensing of the combination

FOECollab as a whole is distributed under the **GNU Affero General Public License v3.0
(AGPL-3.0-only)** — see [`LICENSE`](LICENSE). Combining GPL-3.0 code (FOE-R) into an AGPL-3.0
project is expressly permitted by GPL-3.0 §13 / AGPL-3.0 §13; the full source of FOECollab is
available at <https://github.com/FOECollab/FOE-Collab>.
