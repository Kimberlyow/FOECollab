package io.github.foecollab.FOMC;

import io.github.foecollab.FOMC.Types.Defaults;
import io.github.foecollab.util.TextHelper;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static io.github.foecollab.FOMC.Types.Defaults.DEFAULT_COLOR;

public enum Constant {
	// Fish Size
	BABY("baby", Text.literal("ʙᴀʙʏ").withColor(0x468CE7), 0x468CE7),
	JUVENILE("juvenile", Text.literal("ᴊᴜᴠᴇɴɪʟᴇ").withColor(0x22EA08), 0x22EA08),
	ADULT("adult", Text.literal("ᴀᴅᴜʟᴛ").withColor(0x1C7DA0), 0x1C7DA0),
	LARGE("large", Text.literal("ʟᴀʀɢᴇ").withColor(0xFF9000), 0xFF9000),
	GIGANTIC("gigantic", Text.literal("ɢɪɢᴀɴᴛɪᴄ").withColor(0xAF3333), 0xAF3333),

	// Rarity
	COMMON("common", Text.literal("\uF033").formatted(Formatting.WHITE), 0xFFFFFF),
	RARE("rare", Text.literal("\uF034").formatted(Formatting.WHITE), 0x2B85C4),
	EPIC("epic", Text.literal("\uF035").formatted(Formatting.WHITE), 0x1CD832),
	LEGENDARY("legendary", Text.literal("\uF036").formatted(Formatting.WHITE), 0xD98103),
	MYTHICAL("mythical", Text.literal("\uF037").formatted(Formatting.WHITE), 0xC93832),

	// Event Rarities
	SPECIAL("special", Text.literal("\uF092").formatted(Formatting.WHITE), 0xDD7ACF),

	// Location
	SPAWNHUB("spawnhub", Text.literal("Cypress Lake").withColor(0x5CAE65), DEFAULT_COLOR),
	CYPRESS_LAKE("spawn", Text.literal("Cypress Lake").withColor(0x5CAE65), 0x5CAE65),
	KENAI_RIVER("kenai", Text.literal("Kenai River").withColor(0x68D499), 0x68D499),
	LAKE_BIWA("biwa", Text.literal("Lake Biwa").withColor(0xFBC0FA), 0xFBC0FA),
	MURRAY_RIVER("murray", Text.literal("Murray River").withColor(0xCD5916), 0xCD5916),
	EVERGLADES("everglades", Text.literal("Everglades").withColor(0x2EBB8D), 0x2EBB8D),
	KEY_WEST("keywest", Text.literal("Key West").withColor(0xFBF17C), 0xFBF17C),
	TOLEDO_BEND("toledobend", Text.literal("Toledo Bend Reservoir").withColor(0x99A7D0), 0x99A7D0),
	GREAT_LAKES("greatlakes", Text.literal("Great Lakes").withColor(0x3CABF3), 0x3CABF3),
	DANUBE_RIVER("danube", Text.literal("Danube River").withColor(0xFBC598), 0xFBC598),
	OIL_RIG("oilrig", Text.literal("Oil Rig").withColor(0xFCEB47), 0xFCEB47),
	AMAZON_RIVER("amazon", Text.literal("Amazon River").withColor(0x3EA729), 0x3EA729),
	MEDITERRANEAN_SEA("mediterranean", Text.literal("Mediterranean Sea").withColor(0xF0FB37), 0xF0FB37),
	CAPE_COD("capecod", Text.literal("Cape Cod").withColor(0xBBF5FB), 0xBBF5FB),
	HAWAII("hawaii", TextHelper.concat(
			Text.literal("H").withColor(0xFB933B),
			Text.literal("a").withColor(0xFCB140),
			Text.literal("w").withColor(0xEACD4D),
			Text.literal("a").withColor(0xB2E66C),
			Text.literal("i").withColor(0x75F0A6),
			Text.literal("i").withColor(0x35F4EF)), 0x35F4EF),
	LOFOTEN_ISLANDS("lofotenislands", TextHelper.concat(
			Text.literal("L").withColor(0xCDDAD7),
			Text.literal("o").withColor(0xCCDFD2),
			Text.literal("f").withColor(0xCCE3CC),
			Text.literal("o").withColor(0xCBE8C7),
			Text.literal("t").withColor(0xCBECC1),
			Text.literal("e").withColor(0xCAF1BC),
			Text.literal("n ").withColor(0xCFF2C3),
			Text.literal("I").withColor(0xD9F3D0),
			Text.literal("s").withColor(0xDEF4D7),
			Text.literal("l").withColor(0xE4F5DD),
			Text.literal("a").withColor(0xE9F6E4),
			Text.literal("n").withColor(0xEEF6EB),
			Text.literal("d").withColor(0xF3F7F1),
			Text.literal("s").withColor(0xF8F8F8)), 0xF8F8F8),
	CAIRNS("cairns", Text.literal("Cairns").withColor(0xA1C2FB), 0xA1C2FB),
	CREW_ISLAND("crewisland", Text.literal("Crew Island"), Defaults.DEFAULT_COLOR),
	UNKNOWN("unknown", Text.literal("Unknown"), Defaults.DEFAULT_COLOR),

	// Pet Base
	LOCATION_BASE("lbase", Text.empty(), Defaults.DEFAULT_COLOR),
	CLIMATE_BASE("cbase", Text.empty(), Defaults.DEFAULT_COLOR),

	// Variants
	NORMAL("normal", Text.empty(), Defaults.DEFAULT_COLOR),
	ALBINO("albino", Text.literal("\uF041").formatted(Formatting.WHITE), 0xc4c19f),
	MELANISTIC("melanistic", Text.literal("\uF042").formatted(Formatting.WHITE), 0x1c1c1c),
	TROPHY("trophy", Text.literal("\uF043").formatted(Formatting.WHITE), 0xd5bf3b),
	FABLED("fabled", Text.literal("\uF044").formatted(Formatting.WHITE), 0x82171e),

	// Event Variants
	ALTERNATE("alternate", Text.literal("\uF098").formatted(Formatting.WHITE), 0x9cb4fc),
	SPOOKY("spooky", Text.literal("\uF102").formatted(Formatting.WHITE), 0x2e2e8f),
	FROZEN("frozen", Text.literal("\uF179").formatted(Formatting.WHITE), 0x7fb0e7),

	// Rare Catches
	LIGHTNING_BOTTLE("lightning bottle", TextHelper.concat(
			Text.literal("L").withColor(0xEFE038),
			Text.literal("i").withColor(0xEFC32C),
			Text.literal("g").withColor(0xEABC34),
			Text.literal("h").withColor(0xE4B43C),
			Text.literal("t").withColor(0xEABC34),
			Text.literal("n").withColor(0xEFC32C),
			Text.literal("i").withColor(0xEFE038),
			Text.literal("n").withColor(0xEFFC44),
			Text.literal("g in a Bottle").withColor(0xE4B43C)), 0xe1b23b),
	INFUSION_CAPSULE("infusion capsule", Text.literal("Infusion Capsule").formatted(Formatting.WHITE),
			Defaults.DEFAULT_COLOR),
	SHARD("shard", Text.literal("Shard").formatted(Formatting.GOLD), 0xfca800),
	PROSPECTING_AMULET("prospectingamulet", TextHelper.concat(
			Text.literal("P").withColor(0xE4CC2B),
			Text.literal("r").withColor(0xE3CE2D),
			Text.literal("o").withColor(0xE3D030),
			Text.literal("s").withColor(0xE2D332),
			Text.literal("p").withColor(0xE1D534),
			Text.literal("e").withColor(0xE1D737),
			Text.literal("c").withColor(0xE0D939),
			Text.literal("t").withColor(0xDFDC3B),
			Text.literal("i").withColor(0xDFDE3E),
			Text.literal("n").withColor(0xDEE040),
			Text.literal("g ").withColor(0xDEE243),
			Text.literal("A").withColor(0xDCE747),
			Text.literal("m").withColor(0xDCE94A),
			Text.literal("u").withColor(0xDBEB4C),
			Text.literal("l").withColor(0xDAEE4E),
			Text.literal("e").withColor(0xDAF051),
			Text.literal("t").withColor(0xD9F253)), 0xded436),
	// Bigfoot Drops
	BIGFOOT_FUR("bigfoot fur", Text.literal("Bigfoot Fur").formatted(Formatting.WHITE), Defaults.DEFAULT_COLOR),
	BIGFOOT_TOOTH("bigfoot tooth", Text.literal("Bigfoot Tooth").formatted(Formatting.WHITE), Defaults.DEFAULT_COLOR),

	// Pet Items
	BIGFOOTS_AMULET("bigfootsamulet", TextHelper.concat(
			Text.literal("B").withColor(0xB7884A),
			Text.literal("i").withColor(0xB7884A),
			Text.literal("g").withColor(0xB7884A),
			Text.literal("f").withColor(0xB6884F),
			Text.literal("o").withColor(0xB58854),
			Text.literal("o").withColor(0xB58958),
			Text.literal("t").withColor(0xB4895D),
			Text.literal("'").withColor(0xB38962),
			Text.literal("s ").withColor(0xBE7858),
			Text.literal("A").withColor(0xD55744),
			Text.literal("m").withColor(0xE04639),
			Text.literal("u").withColor(0xEC362F),
			Text.literal("l").withColor(0xF72525),
			Text.literal("e").withColor(0xF72525),
			Text.literal("t").withColor(0xF72525)
	), 0xF72525),
	BIGFOOTS_NECKLACE("bigfootsnecklace", TextHelper.concat(
			Text.literal("B").withColor(0xB7884A),
			Text.literal("i").withColor(0xB7884A),
			Text.literal("g").withColor(0xB7884A),
			Text.literal("f").withColor(0xB6884E),
			Text.literal("o").withColor(0xB68852),
			Text.literal("o").withColor(0xB58956),
			Text.literal("t").withColor(0xB4895A),
			Text.literal("'").withColor(0xB4895E),
			Text.literal("s ").withColor(0xB38962),
			Text.literal("N").withColor(0xC3A78D),
			Text.literal("e").withColor(0xCBB6A2),
			Text.literal("c").withColor(0xD2C4B7),
			Text.literal("k").withColor(0xDAD3CD),
			Text.literal("l").withColor(0xE2E2E2),
			Text.literal("a").withColor(0xE2E2E2),
			Text.literal("c").withColor(0xE2E2E2),
			Text.literal("e").withColor(0xE2E2E2)
	), 0xE2E2E2),
	WADES_AMULET("wadesamulet", TextHelper.concat(
			Text.literal("W").withColor(0x9BAFDB),
			Text.literal("a").withColor(0x95ADDA),
			Text.literal("d").withColor(0x8FABD9),
			Text.literal("e").withColor(0x89AAD8),
			Text.literal("'").withColor(0x83A8D7),
			Text.literal("s ").withColor(0x7CA6D6),
			Text.literal("A").withColor(0x589BD0),
			Text.literal("m").withColor(0x5299CF),
			Text.literal("u").withColor(0x4B97CE),
			Text.literal("l").withColor(0x4596CD),
			Text.literal("e").withColor(0x3F94CC),
			Text.literal("t").withColor(0x3992CB)
	), 0x3992CB),
	MIDAS_AMULET("midasamulet", TextHelper.concat(
			Text.literal("M").withColor(0x938420),
			Text.literal("i").withColor(0x9D9024),
			Text.literal("d").withColor(0xA79D28),
			Text.literal("a").withColor(0xB1A92C),
			Text.literal("s ").withColor(0xBAB530),
			Text.literal("A").withColor(0xCECE38),
			Text.literal("m").withColor(0xC8CB39),
			Text.literal("u").withColor(0xC2C839),
			Text.literal("l").withColor(0xBBC53A),
			Text.literal("e").withColor(0xB5C23A),
			Text.literal("t").withColor(0xAFBF3B)
	), 0xAFBF3B),
	ONYX_AMULET("onyxamulet", TextHelper.concat(
			Text.literal("O").withColor(0x5A5E62),
			Text.literal("n").withColor(0x5D6266),
			Text.literal("y").withColor(0x606669),
			Text.literal("x Amulet").withColor(0x63696D)
	), 0x63696D),
	IVORY_AMULET("ivoryamulet", TextHelper.concat(
			Text.literal("I").withColor(0xE3C6E2),
			Text.literal("v").withColor(0xE2CDE3),
			Text.literal("o").withColor(0xE2D4E5),
			Text.literal("r").withColor(0xE1DAE6),
			Text.literal("y Amulet").withColor(0xE1E1E8)
	), 0xE1E1E8),
	FABLED_AMULET("fabledamulet", TextHelper.concat(
			Text.literal("F").withColor(0xFA463F),
			Text.literal("a").withColor(0xFA443D),
			Text.literal("b").withColor(0xFB433B),
			Text.literal("l").withColor(0xFB4139),
			Text.literal("e").withColor(0xFC3F37),
			Text.literal("d Amulet").withColor(0xFC3E35)
	), 0xFC3E35),

	// Pet Rating
	SICKLY("sickly", Text.literal("sɪᴄᴋʟʏ").withColor(0xFF74403B), 0xFF74403B),
	BAD("bad", Text.literal("ʙᴀᴅ").withColor(0xFFFF5555), 0xFFFF5555),
	BELOW_AVERAGE("below_average", Text.literal("ʙᴇʟᴏᴡ ᴀᴠᴇʀᴀɢᴇ").withColor(0xFFFCFC54), 0xFFFCFC54),
	AVERAGE("average", Text.literal("ᴀᴠᴇʀᴀɢᴇ").withColor(0xFFFCA800), 0xFFFCA800),
	GOOD("good", Text.literal("ɢᴏᴏᴅ").withColor(0xFF54FC54), 0xFF54FC54),
	GREAT("great", Text.literal("ɢʀᴇᴀᴛ").withColor(0xFF00A800), 0xFF00A800),
	EXCELLENT("excellent", Text.literal("ᴇxᴄᴇʟʟᴇɴᴛ").withColor(0xFF54FCFC), 0xFF54FCFC),
	AMAZING("amazing", Text.literal("ᴀᴍᴀᴢɪɴɢ").withColor(0xFFFC54FC), 0xFFFC54FC),
	PERFECT("perfect", Text.literal("ᴘᴇʀꜰᴇᴄᴛ").withColor(0xFFA800A8), 0xFFA800A8),

	// Pets
	PET("pet", Text.literal("Pet").withColor(0xFD95F6), 0xFD95F6),
	BULLFROG("bullfrog", TextHelper.concat(
			Text.literal("B").withColor(0x84CA54),
			Text.literal("u").withColor(0x7FC054),
			Text.literal("l").withColor(0x79B754),
			Text.literal("l").withColor(0x74AD54),
			Text.literal("f").withColor(0x6FA354),
			Text.literal("r").withColor(0x6A9954),
			Text.literal("o").withColor(0x649054),
			Text.literal("g Pet").withColor(0x5F8654)), 0x5F8654),
	BEAR("bear", TextHelper.concat(
			Text.literal("B").withColor(0x593E3B),
			Text.literal("e").withColor(0x583C3A),
			Text.literal("a").withColor(0x573B3A),
			Text.literal("r Pet").withColor(0x563939)), 0x563939),
	FOX("fox", TextHelper.concat(
			Text.literal("F").withColor(0xF99752),
			Text.literal("o").withColor(0xF2A75D),
			Text.literal("x Pet").withColor(0xEBB668)), 0xEBB668),
	KANGAROO("kangaroo", TextHelper.concat(
			Text.literal("K").withColor(0xD19E58),
			Text.literal("a").withColor(0xD1A460),
			Text.literal("n").withColor(0xD1AA69),
			Text.literal("g").withColor(0xD1B071),
			Text.literal("a").withColor(0xD1B779),
			Text.literal("r").withColor(0xD1BD81),
			Text.literal("o").withColor(0xD1C38A),
			Text.literal("o Pet").withColor(0xD1C992)), 0xD1C992),
	MARSH_RABBIT("marshrabbit", TextHelper.concat(
			Text.literal("M").withColor(0x968F73),
			Text.literal("a").withColor(0x928E71),
			Text.literal("r").withColor(0x8D8D70),
			Text.literal("s").withColor(0x898C6E),
			Text.literal("h").withColor(0x858B6D),
			Text.literal(""),
			Text.literal("R").withColor(0x7C896A),
			Text.literal("a").withColor(0x788868),
			Text.literal("b").withColor(0x748767),
			Text.literal("b").withColor(0x708665),
			Text.literal("i").withColor(0x6B8564),
			Text.literal("t Pet").withColor(0x678462)), 0x678462),
	SEA_TURTLE("seaturtle", TextHelper.concat(
			Text.literal("S").withColor(0x69BE7B),
			Text.literal("e").withColor(0x71C27E),
			Text.literal("a").withColor(0x79C781),
			Text.literal(""),
			Text.literal("T").withColor(0x89D087),
			Text.literal("u").withColor(0x92D48A),
			Text.literal("r").withColor(0x9AD98D),
			Text.literal("t").withColor(0xA2DD90),
			Text.literal("l").withColor(0xAAE293),
			Text.literal("e Pet").withColor(0xB2E696)), 0xB2E696),
	DUCK("duck", TextHelper.concat(
			Text.literal("D").withColor(0xEBEAA8),
			Text.literal("u").withColor(0xE2E2A5),
			Text.literal("c").withColor(0xD9DAA3),
			Text.literal("k Pet").withColor(0xD0D2A0)), 0xD0D2A0),
	EAGLE("eagle", TextHelper.concat(
			Text.literal("E").withColor(0xBEBEBE),
			Text.literal("a").withColor(0xBAB8B6),
			Text.literal("g").withColor(0xB5B3AE),
			Text.literal("l").withColor(0xB1ADA5),
			Text.literal("e Pet ").withColor(0xACA79D)), 0xACA79D),
	WOLF("wolf", TextHelper.concat(
			Text.literal("W").withColor(0x818587),
			Text.literal("o").withColor(0x7C8083),
			Text.literal("l").withColor(0x787B7F),
			Text.literal("f Pet").withColor(0x73767B)), 0x73767B),
	PELICAN("pelican", TextHelper.concat(
			Text.literal("P").withColor(0xD9CBA6),
			Text.literal("e").withColor(0xDFC59B),
			Text.literal("l").withColor(0xE6BE90),
			Text.literal("i").withColor(0xECB886),
			Text.literal("c").withColor(0xF2B27B),
			Text.literal("a").withColor(0xF9AB70),
			Text.literal("n Pet").withColor(0xFFA565)), 0xFFA565),
	CAPYBARA("capybara", TextHelper.concat(
			Text.literal("C").withColor(0x725E39),
			Text.literal("a").withColor(0x7F663F),
			Text.literal("p").withColor(0x8C6E45),
			Text.literal("y").withColor(0x99764B),
			Text.literal("b").withColor(0xA77D51),
			Text.literal("a").withColor(0xB48557),
			Text.literal("r").withColor(0xC18D5D),
			Text.literal("a Pet").withColor(0xCE9563)), 0xCE9563),
	LYNX("lynx", TextHelper.concat(
			Text.literal("L").withColor(0xA1A278),
			Text.literal("y").withColor(0xA4A571),
			Text.literal("n").withColor(0xA6A96A),
			Text.literal("x Pet").withColor(0xA9AC63)), 0xA9AC63),
	SHARK("shark", TextHelper.concat(
			Text.literal("S").withColor(0x6C8BE4),
			Text.literal("h").withColor(0x7190DB),
			Text.literal("a").withColor(0x7694D2),
			Text.literal("r").withColor(0x7B99C9),
			Text.literal("k Pet").withColor(0x809DC0)), 0x809DC0),
	DOLPHIN("dolphin", TextHelper.concat(
			Text.literal("D").withColor(0xBAC7E4),
			Text.literal("o").withColor(0xB8C7DE),
			Text.literal("l").withColor(0xB6C6D8),
			Text.literal("p").withColor(0xB4C6D2),
			Text.literal("h").withColor(0xB1C6CB),
			Text.literal("i").withColor(0xAFC5C5),
			Text.literal("n Pet").withColor(0xADC5BF)), 0xADC5BF),
	SHEEP("sheep", TextHelper.concat(
			Text.literal("S").withColor(0xADADAD),
			Text.literal("h").withColor(0x757575),
			Text.literal("e").withColor(0x8E918C),
			Text.literal("e").withColor(0xA6ACA3),
			Text.literal("p Pet").withColor(0xDFDFDF)), 0xDFDFDF),
	KOALA("koala", TextHelper.concat(
			Text.literal("K").withColor(0xAEBFD1),
			Text.literal("o").withColor(0xB1C4D0),
			Text.literal("a").withColor(0xB3C8CF),
			Text.literal("l").withColor(0xB6CDCE),
			Text.literal("a Pet").withColor(0xB8D1CD)), 0xB8D1CD),

	// Water Types
	FRESHWATER("freshwater", Text.literal("Freshwater").withColor(0x3F87EF), 0x3F87EF),
	SALTWATER("saltwater", Text.literal("Saltwater").withColor(0x86D9E6), 0x86D9E6),
	ANY_WATER("any", Text.literal("Any"), DEFAULT_COLOR),
	GLOBAL_WATER("global", Text.literal("Anywhere"), DEFAULT_COLOR),

	// Text Tags
	TEXTCOMMON("textcommon", Text.literal("\uEEE4\uEEE1 퀃 \uEEE8\uEEE7\uEEE5\uEEE2 "), DEFAULT_COLOR),
	TEXTRARE("textrare", Text.literal("\uEEE4\uEEE1 퀇 \uEEE8\uEEE7\uEEE5\uEEE2 "), DEFAULT_COLOR),
	TEXTEPIC("textepic", Text.literal("\uEEE4\uEEE1 퀑 \uEEE8\uEEE7\uEEE5\uEEE2 "), DEFAULT_COLOR),
	TEXTLEGENDARY("textlegendary", Text.literal("\uEEE4\uEEE1 퀕 \uEEE8\uEEE7\uEEE5\uEEE2 "), DEFAULT_COLOR),
	TEXTMYTHICAL("textmythical", Text.literal("\uEEE4\uEEE1 퀙 \uEEE8\uEEE7\uEEE5\uEEE2 "), DEFAULT_COLOR),
	TEXTSPECIAL("textspecial", Text.literal("\uEEE4\uEEE1 퀃 \uEEE8\uEEE7\uEEE5\uEEE2 ").withColor(0xC746B4),
			DEFAULT_COLOR),
	TEXTDEFAULT("textdefault", Text.literal("\uEEE4\uEEE1 퀃 \uEEE8\uEEE7\uEEE5\uEEE2 ").withColor(0x5C4B34),
			DEFAULT_COLOR),

	// Ranks
	ANGLER("angler", Text.literal("\uF032").formatted(Formatting.WHITE), 0x20bbd7),
	SAILOR("sailor", Text.literal("\uF031").formatted(Formatting.WHITE), 0x96f564),
	MARINER("mariner", Text.literal("\uF030").formatted(Formatting.WHITE), 0x66f8ae),
	CAPTAIN("captain", Text.literal("\uF029").formatted(Formatting.WHITE), 0xfca307),
	ADMIRAL("admiral", Text.literal("\uF028").formatted(Formatting.WHITE), 0xae5af6),
	STAFF("staff", Text.literal("\uF024").formatted(Formatting.WHITE), 0x000000),
	DESIGNER("designer", Text.literal("\uF026").formatted(Formatting.WHITE), DEFAULT_COLOR),
	BUILDER("builder", Text.literal("\uF027").formatted(Formatting.WHITE), DEFAULT_COLOR),
	MANAGER("manager", Text.literal("\uF023").formatted(Formatting.WHITE), DEFAULT_COLOR),
	ADMIN("admin", Text.literal("\uF022").formatted(Formatting.WHITE), DEFAULT_COLOR),
	OWNER("owner", Text.literal("\uF021").formatted(Formatting.WHITE), DEFAULT_COLOR),
	COMMUNITYMANAGER("communitymanager", Text.literal("\uF088").formatted(Formatting.WHITE), DEFAULT_COLOR),
	FOE("foe", Text.literal("\uE00B").formatted(Formatting.WHITE), 0x325330),
	FOE_PURPLE("foe_purple", Text.literal("\uE00D").formatted(Formatting.WHITE), 0x8325A0),

	// Weather Types
	RAIN("☂", Text.literal("☂"), 0x5555FF),
	SUN("☀", Text.literal("☀"), 0xFFFF55),
	THUNDERSTORM("⚡", Text.literal("⚡"), 0xFFFF55),
	BLOOMINGOASIS("♣", Text.literal("♣"), 0xFC54FC),
	FABLEDWEATHER("⭐", Text.literal("⭐"), 0xF7453E),
	GOLDRUSH("⚠", Text.literal("⚠"), 0xF7EA3E),
	MOON("○", Text.literal("○"), 0x5FC0E6),

	// Stats
	LUCK("luck", Text.literal("♣ Luck").withColor(0x80DAC3), 0x80DAC3),
	SCALE("scale", Text.literal("⚓ Scale").withColor(0x4C88F1), 0x4C88F1),

	// Climate (changed duplicate of #ClimateConstants.java)
	SUBTROPICAL("subtropical_climate", TextHelper.concat(
			Text.literal("S").withColor(0x4FB07A),
			Text.literal("u").withColor(0x4FB683),
			Text.literal("b").withColor(0x4EBC8D),
			Text.literal("t").withColor(0x4EC296),
			Text.literal("r").withColor(0x4DC8A0),
			Text.literal("o").withColor(0x4DCEA9),
			Text.literal("p").withColor(0x4CD6B2),
			Text.literal("i").withColor(0x4BDEBB),
			Text.literal("c").withColor(0x49E7C4),
			Text.literal("a").withColor(0x48EFCD),
			Text.literal("l").withColor(0x47F7D6)), 0x47F7D6),
	SUBARCTIC("subarctic_climate", TextHelper.concat(
			Text.literal("S").withColor(0x53A1C1),
			Text.literal("u").withColor(0x64AAC8),
			Text.literal("b").withColor(0x75B3CF),
			Text.literal("a").withColor(0x86BBD5),
			Text.literal("r").withColor(0x97C4DC),
			Text.literal("c").withColor(0x97C1D8),
			Text.literal("t").withColor(0x98BED3),
			Text.literal("i").withColor(0x98BACF),
			Text.literal("c").withColor(0x98B7CA)), 0x98B7CA),
	SEMI_ARID("semi-arid_climate", TextHelper.concat(
			Text.literal("S").withColor(0xE6902E),
			Text.literal("e").withColor(0xE59833),
			Text.literal("m").withColor(0xE5A038),
			Text.literal("i").withColor(0xE4A73C),
			Text.literal("-").withColor(0xE3AF41),
			Text.literal("A").withColor(0xE3B14C),
			Text.literal("r").withColor(0xE4B357),
			Text.literal("i").withColor(0xE4B562),
			Text.literal("d").withColor(0xE4B76D)), 0xE4B76D),
	SAVANNA("savanna_climate", TextHelper.concat(
			Text.literal("S").withColor(0xBAC153),
			Text.literal("a").withColor(0xC8CB5A),
			Text.literal("v").withColor(0xD7D661),
			Text.literal("a").withColor(0xE5E068),
			Text.literal("n").withColor(0xE4DF6F),
			Text.literal("n").withColor(0xE3DE77),
			Text.literal("a").withColor(0xE2DD7E)), 0xE2DD7E),
	CONTINENTAL("continental_climate", TextHelper.concat(
			Text.literal("C").withColor(0xA4A9AB),
			Text.literal("o").withColor(0xABB2B2),
			Text.literal("n").withColor(0xB2BAB9),
			Text.literal("t").withColor(0xB8C3BF),
			Text.literal("i").withColor(0xBFCBC6),
			Text.literal("n").withColor(0xC6D4CD),
			Text.literal("e").withColor(0xCCD9D2),
			Text.literal("n").withColor(0xD2DDD8),
			Text.literal("t").withColor(0xD9E2DD),
			Text.literal("a").withColor(0xDFE6E3),
			Text.literal("l").withColor(0xE5EBE8)), 0xE5EBE8),
	RAINFOREST("rainforest_climate", TextHelper.concat(
			Text.literal("R").withColor(0x569579),
			Text.literal("a").withColor(0x4C9E7A),
			Text.literal("i").withColor(0x42A87B),
			Text.literal("n").withColor(0x39B17C),
			Text.literal("f").withColor(0x2FBA7D),
			Text.literal("o").withColor(0x2AC27F),
			Text.literal("r").withColor(0x2AC983),
			Text.literal("e").withColor(0x2AD086),
			Text.literal("s").withColor(0x2AD68A),
			Text.literal("t").withColor(0x2ADD8E)), 0x2ADD8E),
	MEDITERRANEAN("mediterranean_climate", TextHelper.concat(
			Text.literal("M").withColor(0x80C4EF),
			Text.literal("e").withColor(0x85C6EF),
			Text.literal("d").withColor(0x8AC8EF),
			Text.literal("i").withColor(0x8FCAEF),
			Text.literal("t").withColor(0x94CCEE),
			Text.literal("e").withColor(0x99CEEE),
			Text.literal("r").withColor(0x9ED0EE),
			Text.literal("r").withColor(0xA1D1EF),
			Text.literal("a").withColor(0xA4D3F0),
			Text.literal("n").withColor(0xA7D4F1),
			Text.literal("e").withColor(0xAAD5F1),
			Text.literal("a").withColor(0xADD7F2),
			Text.literal("n").withColor(0xB0D8F3)), 0xB0D8F3),
	OCEANIC("oceanic_climate", TextHelper.concat(
			Text.literal("O").withColor(0x397FAC),
			Text.literal("c").withColor(0x3A85B4),
			Text.literal("e").withColor(0x3C8CBD),
			Text.literal("a").withColor(0x3D92C5),
			Text.literal("n").withColor(0x3995CF),
			Text.literal("i").withColor(0x3599D9),
			Text.literal("c").withColor(0x319CE3)), 0x319CE3),
	MONSOON("monsoon_climate", TextHelper.concat(
			Text.literal("M").withColor(0x6141DF),
			Text.literal("o").withColor(0x654FE0),
			Text.literal("n").withColor(0x6A5CE0),
			Text.literal("s").withColor(0x6E6AE1),
			Text.literal("o").withColor(0x7278E1),
			Text.literal("o").withColor(0x7785E2),
			Text.literal("n").withColor(0x7B93E2)), 0x7B93E2),

	// Armor Quality
	BROKEN("broken", Text.literal("ʙʀᴏᴋᴇɴ").withColor(0xFF74403B), 0xFF74403B),
	TORN("torn", Text.literal("ᴛᴏʀɴ").withColor(0xFFFF5555), 0xFFFF5555),
	DAMAGED("damaged", Text.literal("ᴅᴀᴍᴀɢᴇᴅ").withColor(0xFFFCFC54), 0xFFFCFC54),
	BLEMISHED("blemished", Text.literal("ʙʟᴇᴍɪsʜᴇᴅ").withColor(0xFFFCA800), 0xFFFCA800),
	WELL_WORN("well_worn", Text.literal("ᴡᴇʟʟ ᴡᴏʀɴ").withColor(0xFF54FC54), 0xFF54FC54),
	USED("used", Text.literal("ᴜsᴇᴅ").withColor(0xFF00A800), 0xFF00A800),
	MINT("mint", Text.literal("ᴍɪɴᴛ").withColor(0xFF54FCFC), 0xFF54FCFC),
	SUBLIME("sublime", Text.literal("sᴜʙʟɪᴍᴇ").withColor(0xFFFC54FC), 0xFFFC54FC),
	SUPERIOR("superior", Text.literal("sᴜᴘᴇʀɪᴏʀ").withColor(0xFFA800A8), 0xFFA800A8),

	DEFAULT("default", Text.empty(), Defaults.DEFAULT_COLOR);

	public final String ID;
	public final Text TAG;
	public final int COLOR;

	Constant(String id, Text tag, int color) {
		this.ID = id;
		this.TAG = tag;
		this.COLOR = color;
	}

	public static Constant valueOfId(String id) {
		for (Constant c : values()) {
			if (c.ID.equals(id.toLowerCase())) {
				return c;
			}
		}
		return DEFAULT;
	}

	public static Constant valueOfTag(String tag) {
		for (Constant c : values()) {
			if (c.TAG.getString().equals(tag)) {
				return c;
			}
		}
		return DEFAULT;
	}

	public static int colorOfId(String id) {
		for (Constant c : values()) {
			if (c.ID.equals(id.toLowerCase())) {
				return c.COLOR;
			}
		}
		return Defaults.DEFAULT_COLOR;
	}

	@Override
	public String toString() {
		return this.ID;
	}

}