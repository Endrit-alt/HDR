package com.endrit.hdr;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(ConfigKeys.PLUGIN_CONFIG_GROUP_NAME)
@SuppressWarnings("PMD.ExcessivePublicCount")
public interface HDRConfig extends Config {
	String OPEN_WORLD_SECTION = "openWorld";
	String COX_SECTION = "cox";
	String TOA_LOBBY_SECTION = "toaLobby";
	String TOA_SECTION = "toa";
	String TOB_SECTION = "tob";
	String NEX_SECTION = "nex";
	String NIGHTMARE_SECTION = "nightmare";
	String ROYAL_TITANS_SECTION = "royalTitans";
	String FORTIS_COLOSSEUM_SECTION = "fortisColosseum";
	String DOOM_OF_MOKHAIOTL_SECTION = "doomOfMokhaiotl";
	String POH_SECTION = "poh";

	@ConfigSection(
		name = "Open world",
		description = "Saturation targeting for non-special regions.",
		position = 100
	)
	String OPEN_WORLD = OPEN_WORLD_SECTION;

	@ConfigSection(
		name = "Chambers of Xeric",
		description = "Saturation targeting for Chambers of Xeric regions.",
		position = 200,
		closedByDefault = true
	)
	String COX = COX_SECTION;

	@ConfigSection(
		name = "ToA lobby",
		description = "Saturation targeting for Tombs of Amascut lobby regions.",
		position = 300,
		closedByDefault = true
	)
	String TOA_LOBBY = TOA_LOBBY_SECTION;

	@ConfigSection(
		name = "Tombs of Amascut",
		description = "HDR recoloring for Tombs of Amascut encounter regions.",
		position = 400,
		closedByDefault = true
	)
	String TOA = TOA_SECTION;

	@ConfigSection(
		name = "Theatre of Blood",
		description = "HDR recoloring for Theatre of Blood encounter regions.",
		position = 500,
		closedByDefault = true
	)
	String TOB = TOB_SECTION;

	@ConfigSection(
		name = "Nex",
		description = "HDR recoloring for Nex.",
		position = 600,
		closedByDefault = true
	)
	String NEX = NEX_SECTION;

	@ConfigSection(
		name = "Nightmare",
		description = "HDR recoloring for Nightmare.",
		position = 700,
		closedByDefault = true
	)
	String NIGHTMARE = NIGHTMARE_SECTION;

	@ConfigSection(
		name = "Royal Titans",
		description = "HDR recoloring for Royal Titans.",
		position = 800,
		closedByDefault = true
	)
	String ROYAL_TITANS = ROYAL_TITANS_SECTION;

	@ConfigSection(
		name = "Fortis Colosseum",
		description = "HDR recoloring for Fortis Colosseum.",
		position = 900,
		closedByDefault = true
	)
	String FORTIS_COLOSSEUM = FORTIS_COLOSSEUM_SECTION;

	@ConfigSection(
		name = "Doom of Mokhaiotl",
		description = "HDR recoloring for Doom of Mokhaiotl.",
		position = 950,
		closedByDefault = true
	)
	String DOOM_OF_MOKHAIOTL = DOOM_OF_MOKHAIOTL_SECTION;

	@ConfigSection(
		name = "Player-owned house (POH)",
		description = "HDR recoloring for player-owned house regions.",
		position = 1000,
		closedByDefault = true
	)
	String POH = POH_SECTION;

	@ConfigItem(
		keyName = ConfigKeys.OPEN_WORLD_ENABLED,
		name = "Enabled",
		description = "Enables HDR recoloring for open-world regions.",
		position = 0,
		section = OPEN_WORLD
	)
	default boolean isOpenWorldEnabled() {
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.FINAL_LIGHTNESS_ADJUSTMENT,
		name = "Final lightness adjustment",
		description = "Adds or subtracts this many lightness levels on top of the hardcoded open-world baseline",
		position = 1,
		section = OPEN_WORLD
	)
	@Range(min = -127, max = 127)
	default int getFinalLightnessAdjustment() {
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.TARGET_SATURATION_ADJUSTMENT,
		name = "Saturation adjustment",
		description = "Adjusts saturation only for open-world tiles near the target color. Negative values desaturate, positive values increase saturation.",
		position = 2,
		section = OPEN_WORLD
	)
	@Units(Units.PERCENT)
	@Range(min = -100, max = 100)
	default int getTargetSaturationAdjustment() {
		return -50;
	}

	@ConfigItem(
		keyName = ConfigKeys.TARGET_SATURATION_COLOR,
		name = "Target color",
		description = "Only open-world tiles near this color's hue receive the saturation adjustment.",
		position = 3,
		section = OPEN_WORLD
	)
	default Color getTargetSaturationColor() {
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = ConfigKeys.TARGET_SATURATION_HUE_RANGE,
		name = "Hue range",
		description = "How wide the open-world matched color range is on RuneScape's 0-63 hue scale.",
		position = 4,
		section = OPEN_WORLD
	)
	@Range(min = 0, max = 32)
	default int getTargetSaturationHueRange() {
		return 5;
	}

	@ConfigItem(
		keyName = ConfigKeys.COX_ENABLED,
		name = "Enabled",
		description = "Enables HDR recoloring for Chambers of Xeric regions.",
		position = 0,
		section = COX
	)
	default boolean isCoxEnabled() {
		return true;
	}

	@ConfigItem(
		keyName = ConfigKeys.COX_FINAL_LIGHTNESS_ADJUSTMENT,
		name = "Final lightness adjustment",
		description = "Adds or subtracts this many lightness levels on top of the hardcoded Chambers of Xeric baseline",
		position = 1,
		section = COX
	)
	@Range(min = -127, max = 127)
	default int getCoxFinalLightnessAdjustment() {
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.COX_TARGET_SATURATION_ADJUSTMENT,
		name = "Saturation adjustment",
		description = "Adjusts saturation only for Chambers of Xeric tiles near the target color. Negative values desaturate, positive values increase saturation.",
		position = 2,
		section = COX
	)
	@Units(Units.PERCENT)
	@Range(min = -100, max = 100)
	default int getCoxTargetSaturationAdjustment() {
		return -50;
	}

	@ConfigItem(
		keyName = ConfigKeys.COX_TARGET_SATURATION_COLOR,
		name = "Target color",
		description = "Only Chambers of Xeric tiles near this color's hue receive the saturation adjustment.",
		position = 3,
		section = COX
	)
	default Color getCoxTargetSaturationColor() {
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = ConfigKeys.COX_TARGET_SATURATION_HUE_RANGE,
		name = "Hue range",
		description = "How wide the Chambers of Xeric matched color range is on RuneScape's 0-63 hue scale.",
		position = 4,
		section = COX
	)
	@Range(min = 0, max = 32)
	default int getCoxTargetSaturationHueRange() {
		return 5;
	}

	@ConfigItem(
		keyName = ConfigKeys.TOA_LOBBY_ENABLED,
		name = "Enabled",
		description = "Enables HDR recoloring for Tombs of Amascut lobby regions.",
		position = 0,
		section = TOA_LOBBY
	)
	default boolean isToaLobbyEnabled() {
		return true;
	}

	@ConfigItem(
		keyName = ConfigKeys.TOA_LOBBY_FINAL_LIGHTNESS_ADJUSTMENT,
		name = "Final lightness adjustment",
		description = "Adds or subtracts this many lightness levels on top of the hardcoded ToA lobby baseline",
		position = 1,
		section = TOA_LOBBY
	)
	@Range(min = -127, max = 127)
	default int getToaLobbyFinalLightnessAdjustment() {
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.TOA_LOBBY_TARGET_SATURATION_ADJUSTMENT,
		name = "Saturation adjustment",
		description = "Adjusts saturation only for ToA lobby tiles near the target color. Negative values desaturate, positive values increase saturation.",
		position = 2,
		section = TOA_LOBBY
	)
	@Units(Units.PERCENT)
	@Range(min = -100, max = 100)
	default int getToaLobbyTargetSaturationAdjustment() {
		return -50;
	}

	@ConfigItem(
		keyName = ConfigKeys.TOA_LOBBY_TARGET_SATURATION_COLOR,
		name = "Target color",
		description = "Only ToA lobby tiles near this color's hue receive the saturation adjustment.",
		position = 3,
		section = TOA_LOBBY
	)
	default Color getToaLobbyTargetSaturationColor() {
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = ConfigKeys.TOA_LOBBY_TARGET_SATURATION_HUE_RANGE,
		name = "Hue range",
		description = "How wide the ToA lobby matched color range is on RuneScape's 0-63 hue scale.",
		position = 4,
		section = TOA_LOBBY
	)
	@Range(min = 0, max = 32)
	default int getToaLobbyTargetSaturationHueRange() {
		return 5;
	}

	@ConfigItem(
		keyName = ConfigKeys.TOA_ENABLED,
		name = "Enabled",
		description = "Enables HDR recoloring for Tombs of Amascut encounter regions.",
		position = 0,
		section = TOA
	)
	default boolean isToaEnabled() {
		return true;
	}

	@ConfigItem(
		keyName = ConfigKeys.TOA_FINAL_LIGHTNESS_ADJUSTMENT,
		name = "Final lightness adjustment",
		description = "Adds or subtracts this many lightness levels on top of the hardcoded Tombs of Amascut baseline",
		position = 1,
		section = TOA
	)
	@Range(min = -127, max = 127)
	default int getToaFinalLightnessAdjustment() {
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.TOA_TARGET_SATURATION_ADJUSTMENT,
		name = "Saturation adjustment",
		description = "Adjusts saturation only for Tombs of Amascut tiles near the target color. Negative values desaturate, positive values increase saturation.",
		position = 2,
		section = TOA
	)
	@Units(Units.PERCENT)
	@Range(min = -100, max = 100)
	default int getToaTargetSaturationAdjustment() {
		return -50;
	}

	@ConfigItem(
		keyName = ConfigKeys.TOA_TARGET_SATURATION_COLOR,
		name = "Target color",
		description = "Only Tombs of Amascut tiles near this color's hue receive the saturation adjustment.",
		position = 3,
		section = TOA
	)
	default Color getToaTargetSaturationColor() {
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = ConfigKeys.TOA_TARGET_SATURATION_HUE_RANGE,
		name = "Hue range",
		description = "How wide the Tombs of Amascut matched color range is on RuneScape's 0-63 hue scale.",
		position = 4,
		section = TOA
	)
	@Range(min = 0, max = 32)
	default int getToaTargetSaturationHueRange() {
		return 5;
	}

	@ConfigItem(
		keyName = ConfigKeys.TOB_ENABLED,
		name = "Enabled",
		description = "Enables HDR recoloring for Theatre of Blood encounter regions.",
		position = 0,
		section = TOB
	)
	default boolean isTobEnabled() {
		return true;
	}

	@ConfigItem(
		keyName = ConfigKeys.TOB_FINAL_LIGHTNESS_ADJUSTMENT,
		name = "Final lightness adjustment",
		description = "Adds or subtracts this many lightness levels on top of the hardcoded Theatre of Blood baseline",
		position = 1,
		section = TOB
	)
	@Range(min = -127, max = 127)
	default int getTobFinalLightnessAdjustment() {
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.TOB_TARGET_SATURATION_ADJUSTMENT,
		name = "Saturation adjustment",
		description = "Adjusts saturation only for Theatre of Blood tiles near the target color. Negative values desaturate, positive values increase saturation.",
		position = 2,
		section = TOB
	)
	@Units(Units.PERCENT)
	@Range(min = -100, max = 100)
	default int getTobTargetSaturationAdjustment() {
		return -50;
	}

	@ConfigItem(
		keyName = ConfigKeys.TOB_TARGET_SATURATION_COLOR,
		name = "Target color",
		description = "Only Theatre of Blood tiles near this color's hue receive the saturation adjustment.",
		position = 3,
		section = TOB
	)
	default Color getTobTargetSaturationColor() {
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = ConfigKeys.TOB_TARGET_SATURATION_HUE_RANGE,
		name = "Hue range",
		description = "How wide the Theatre of Blood matched color range is on RuneScape's 0-63 hue scale.",
		position = 4,
		section = TOB
	)
	@Range(min = 0, max = 32)
	default int getTobTargetSaturationHueRange() {
		return 5;
	}

	@ConfigItem(
		keyName = ConfigKeys.NEX_ENABLED,
		name = "Enabled",
		description = "Enables HDR recoloring for Nex.",
		position = 0,
		section = NEX
	)
	default boolean isNexEnabled() {
		return true;
	}

	@ConfigItem(
		keyName = ConfigKeys.NEX_FINAL_LIGHTNESS_ADJUSTMENT,
		name = "Final lightness adjustment",
		description = "Adds or subtracts this many lightness levels on top of the hardcoded Nex baseline",
		position = 1,
		section = NEX
	)
	@Range(min = -127, max = 127)
	default int getNexFinalLightnessAdjustment() {
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.NEX_TARGET_SATURATION_ADJUSTMENT,
		name = "Saturation adjustment",
		description = "Adjusts saturation only for Nex tiles near the target color. Negative values desaturate, positive values increase saturation.",
		position = 2,
		section = NEX
	)
	@Units(Units.PERCENT)
	@Range(min = -100, max = 100)
	default int getNexTargetSaturationAdjustment() {
		return -50;
	}

	@ConfigItem(
		keyName = ConfigKeys.NEX_TARGET_SATURATION_COLOR,
		name = "Target color",
		description = "Only Nex tiles near this color's hue receive the saturation adjustment.",
		position = 3,
		section = NEX
	)
	default Color getNexTargetSaturationColor() {
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = ConfigKeys.NEX_TARGET_SATURATION_HUE_RANGE,
		name = "Hue range",
		description = "How wide the Nex matched color range is on RuneScape's 0-63 hue scale.",
		position = 4,
		section = NEX
	)
	@Range(min = 0, max = 32)
	default int getNexTargetSaturationHueRange() {
		return 5;
	}

	@ConfigItem(
		keyName = ConfigKeys.NIGHTMARE_ENABLED,
		name = "Enabled",
		description = "Enables HDR recoloring for Nightmare.",
		position = 0,
		section = NIGHTMARE
	)
	default boolean isNightmareEnabled() {
		return true;
	}

	@ConfigItem(
		keyName = ConfigKeys.NIGHTMARE_FINAL_LIGHTNESS_ADJUSTMENT,
		name = "Final lightness adjustment",
		description = "Adds or subtracts this many lightness levels on top of the hardcoded Nightmare baseline",
		position = 1,
		section = NIGHTMARE
	)
	@Range(min = -127, max = 127)
	default int getNightmareFinalLightnessAdjustment() {
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.NIGHTMARE_TARGET_SATURATION_ADJUSTMENT,
		name = "Saturation adjustment",
		description = "Adjusts saturation only for Nightmare tiles near the target color. Negative values desaturate, positive values increase saturation.",
		position = 2,
		section = NIGHTMARE
	)
	@Units(Units.PERCENT)
	@Range(min = -100, max = 100)
	default int getNightmareTargetSaturationAdjustment() {
		return -50;
	}

	@ConfigItem(
		keyName = ConfigKeys.NIGHTMARE_TARGET_SATURATION_COLOR,
		name = "Target color",
		description = "Only Nightmare tiles near this color's hue receive the saturation adjustment.",
		position = 3,
		section = NIGHTMARE
	)
	default Color getNightmareTargetSaturationColor() {
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = ConfigKeys.NIGHTMARE_TARGET_SATURATION_HUE_RANGE,
		name = "Hue range",
		description = "How wide the Nightmare matched color range is on RuneScape's 0-63 hue scale.",
		position = 4,
		section = NIGHTMARE
	)
	@Range(min = 0, max = 32)
	default int getNightmareTargetSaturationHueRange() {
		return 5;
	}

	@ConfigItem(
		keyName = ConfigKeys.ROYAL_TITANS_ENABLED,
		name = "Enabled",
		description = "Enables HDR recoloring for Royal Titans.",
		position = 0,
		section = ROYAL_TITANS
	)
	default boolean isRoyalTitansEnabled() {
		return true;
	}

	@ConfigItem(
		keyName = ConfigKeys.ROYAL_TITANS_FINAL_LIGHTNESS_ADJUSTMENT,
		name = "Final lightness adjustment",
		description = "Adds or subtracts this many lightness levels on top of the hardcoded Royal Titans baseline",
		position = 1,
		section = ROYAL_TITANS
	)
	@Range(min = -127, max = 127)
	default int getRoyalTitansFinalLightnessAdjustment() {
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.ROYAL_TITANS_TARGET_SATURATION_ADJUSTMENT,
		name = "Saturation adjustment",
		description = "Adjusts saturation only for Royal Titans tiles near the target color. Negative values desaturate, positive values increase saturation.",
		position = 2,
		section = ROYAL_TITANS
	)
	@Units(Units.PERCENT)
	@Range(min = -100, max = 100)
	default int getRoyalTitansTargetSaturationAdjustment() {
		return -50;
	}

	@ConfigItem(
		keyName = ConfigKeys.ROYAL_TITANS_TARGET_SATURATION_COLOR,
		name = "Target color",
		description = "Only Royal Titans tiles near this color's hue receive the saturation adjustment.",
		position = 3,
		section = ROYAL_TITANS
	)
	default Color getRoyalTitansTargetSaturationColor() {
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = ConfigKeys.ROYAL_TITANS_TARGET_SATURATION_HUE_RANGE,
		name = "Hue range",
		description = "How wide the Royal Titans matched color range is on RuneScape's 0-63 hue scale.",
		position = 4,
		section = ROYAL_TITANS
	)
	@Range(min = 0, max = 32)
	default int getRoyalTitansTargetSaturationHueRange() {
		return 5;
	}

	@ConfigItem(
		keyName = ConfigKeys.FORTIS_COLOSSEUM_ENABLED,
		name = "Enabled",
		description = "Enables HDR recoloring for Fortis Colosseum.",
		position = 0,
		section = FORTIS_COLOSSEUM
	)
	default boolean isFortisColosseumEnabled() {
		return true;
	}

	@ConfigItem(
		keyName = ConfigKeys.FORTIS_COLOSSEUM_FINAL_LIGHTNESS_ADJUSTMENT,
		name = "Final lightness adjustment",
		description = "Adds or subtracts this many lightness levels on top of the hardcoded Fortis Colosseum baseline",
		position = 1,
		section = FORTIS_COLOSSEUM
	)
	@Range(min = -127, max = 127)
	default int getFortisColosseumFinalLightnessAdjustment() {
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.FORTIS_COLOSSEUM_TARGET_SATURATION_ADJUSTMENT,
		name = "Saturation adjustment",
		description = "Adjusts saturation only for Fortis Colosseum tiles near the target color. Negative values desaturate, positive values increase saturation.",
		position = 2,
		section = FORTIS_COLOSSEUM
	)
	@Units(Units.PERCENT)
	@Range(min = -100, max = 100)
	default int getFortisColosseumTargetSaturationAdjustment() {
		return -50;
	}

	@ConfigItem(
		keyName = ConfigKeys.FORTIS_COLOSSEUM_TARGET_SATURATION_COLOR,
		name = "Target color",
		description = "Only Fortis Colosseum tiles near this color's hue receive the saturation adjustment.",
		position = 3,
		section = FORTIS_COLOSSEUM
	)
	default Color getFortisColosseumTargetSaturationColor() {
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = ConfigKeys.FORTIS_COLOSSEUM_TARGET_SATURATION_HUE_RANGE,
		name = "Hue range",
		description = "How wide the Fortis Colosseum matched color range is on RuneScape's 0-63 hue scale.",
		position = 4,
		section = FORTIS_COLOSSEUM
	)
	@Range(min = 0, max = 32)
	default int getFortisColosseumTargetSaturationHueRange() {
		return 5;
	}

	@ConfigItem(
		keyName = ConfigKeys.DOOM_OF_MOKHAIOTL_ENABLED,
		name = "Enabled",
		description = "Enables HDR recoloring for Doom of Mokhaiotl.",
		position = 0,
		section = DOOM_OF_MOKHAIOTL
	)
	default boolean isDoomOfMokhaiotlEnabled() {
		return true;
	}

	@ConfigItem(
		keyName = ConfigKeys.DOOM_OF_MOKHAIOTL_FINAL_LIGHTNESS_ADJUSTMENT,
		name = "Final lightness adjustment",
		description = "Adds or subtracts this many lightness levels on top of the hardcoded Doom of Mokhaiotl baseline",
		position = 1,
		section = DOOM_OF_MOKHAIOTL
	)
	@Range(min = -127, max = 127)
	default int getDoomOfMokhaiotlFinalLightnessAdjustment() {
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.DOOM_OF_MOKHAIOTL_TARGET_SATURATION_ADJUSTMENT,
		name = "Saturation adjustment",
		description = "Adjusts saturation only for Doom of Mokhaiotl tiles near the target color. Negative values desaturate, positive values increase saturation.",
		position = 2,
		section = DOOM_OF_MOKHAIOTL
	)
	@Units(Units.PERCENT)
	@Range(min = -100, max = 100)
	default int getDoomOfMokhaiotlTargetSaturationAdjustment() {
		return -50;
	}

	@ConfigItem(
		keyName = ConfigKeys.DOOM_OF_MOKHAIOTL_TARGET_SATURATION_COLOR,
		name = "Target color",
		description = "Only Doom of Mokhaiotl tiles near this color's hue receive the saturation adjustment.",
		position = 3,
		section = DOOM_OF_MOKHAIOTL
	)
	default Color getDoomOfMokhaiotlTargetSaturationColor() { return Color.decode("#FFA400"); }

	@ConfigItem(
		keyName = ConfigKeys.DOOM_OF_MOKHAIOTL_TARGET_SATURATION_HUE_RANGE,
		name = "Hue range",
		description = "How wide the Doom of Mokhaiotl matched color range is on RuneScape's 0-63 hue scale.",
		position = 4,
		section = DOOM_OF_MOKHAIOTL
	)
	@Range(min = 0, max = 32)
	default int getDoomOfMokhaiotlTargetSaturationHueRange() {
		return 5;
	}

	@ConfigItem(
		keyName = ConfigKeys.POH_ENABLED,
		name = "Enabled",
		description = "Enables HDR recoloring for player-owned house regions.",
		position = 0,
		section = POH
	)
	default boolean isPohEnabled() {
		return false;
	}

	@ConfigItem(
		keyName = ConfigKeys.POH_FINAL_LIGHTNESS_ADJUSTMENT,
		name = "Final lightness adjustment",
		description = "Adds or subtracts this many lightness levels on top of the hardcoded POH baseline",
		position = 1,
		section = POH
	)
	@Range(min = -127, max = 127)
	default int getPohFinalLightnessAdjustment() {
		return 0;
	}

	@ConfigItem(
		keyName = ConfigKeys.POH_TARGET_SATURATION_ADJUSTMENT,
		name = "Saturation adjustment",
		description = "Adjusts saturation only for POH tiles near the target color. Negative values desaturate, positive values increase saturation.",
		position = 2,
		section = POH
	)
	@Units(Units.PERCENT)
	@Range(min = -100, max = 100)
	default int getPohTargetSaturationAdjustment() {
		return -50;
	}

	@ConfigItem(
		keyName = ConfigKeys.POH_TARGET_SATURATION_COLOR,
		name = "Target color",
		description = "Only POH tiles near this color's hue receive the saturation adjustment.",
		position = 3,
		section = POH
	)
	default Color getPohTargetSaturationColor() {
		return Color.YELLOW;
	}

	@ConfigItem(
		keyName = ConfigKeys.POH_TARGET_SATURATION_HUE_RANGE,
		name = "Hue range",
		description = "How wide the POH matched color range is on RuneScape's 0-63 hue scale.",
		position = 4,
		section = POH
	)
	@Range(min = 0, max = 32)
	default int getPohTargetSaturationHueRange() {
		return 5;
	}
}
