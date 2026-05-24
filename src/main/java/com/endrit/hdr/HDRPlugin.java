package com.endrit.hdr;

import com.google.inject.Provides;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.Scene;
import net.runelite.api.SceneTileModel;
import net.runelite.api.SceneTilePaint;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.PreMapLoad;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
		name = "HDR"
)
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.TooManyFields"})
public class HDRPlugin extends Plugin {
	public static final int NEXT_REFRESH_UNSET = -1;

	private static final int TILE_HUE_REDUCTION = 0;
	private static final int TILE_SATURATION_REDUCTION = 0;
	private static final boolean TILE_FLAT_SHADING = false;
	private static final boolean OVERRIDE_TILE_COLOR_ENABLED = false;
	private static final int OVERRIDE_TILE_COLOR = 0;
	private static final int LOCAL_TILE_SIZE = 128;
	private static final int BRIGHT_REGION_SAMPLE_RADIUS_TILES = 24;
	private static final int BRIGHT_REGION_MIN_TILES = 64;
	private static final int BRIGHT_REGION_MEDIAN_PERCENTILE = 50;
	private static final int BRIGHT_REGION_BRIGHT_LIGHTNESS = 65;
	private static final int NEUTRAL_SNOW_MAX_SATURATION = 0;
	private static final int PALE_SNOW_MAX_SATURATION = 2;
	private static final int PALE_SNOW_MIN_LIGHTNESS = 58;
	private static final int COLD_SNOW_MIN_HUE = 22;
	private static final int COLD_SNOW_MAX_HUE = 48;
	private static final int COLD_SNOW_MAX_SATURATION = 7;
	private static final int COLD_SNOW_MIN_LIGHTNESS = 32;
	private static final int SNOW_NEIGHBORHOOD_RADIUS = 2;
	private static final int SNOW_FILL_MIN_DIRECT_NEIGHBORS = 2;
	private static final int SNOW_FILL_MIN_SUPPORT_NEIGHBORS = 4;
	private static final int SNOW_STRONG_MIN_DIRECT_NEIGHBORS = 5;
	private static final int SNOW_STRONG_MIN_SUPPORT_NEIGHBORS = 10;
	private static final int SNOW_FILL_MIN_HUE = 22;
	private static final int SNOW_FILL_MAX_HUE = 52;
	private static final int SNOW_FILL_MAX_SATURATION = 7;
	private static final int SNOW_FILL_MIN_LIGHTNESS = 28;
	private static final int BRIGHT_REGION_PERMILLE = 1_000;
	private static final int PERCENT_PERMILLE = 10;
	private static final int BRIGHT_REGION_MAX_LEVEL = 13;
	private static final int SNOW_FINAL_LIGHTNESS_OFFSET = -8;
	private static final int SNOW_PROFILE_LEVEL = 8;
	private static final int SNOW_EDGE_PROFILE_LEVEL = 5;

	private static final Set<Integer> COX_REGION_IDS = Set.of(
			13_136, 13_137, 13_393, 13_138, 13_394, 13_139, 13_395,
			13_140, 13_396, 13_141, 13_397, 13_145, 13_401
	);
	private static final Set<Integer> COX_OLM_REGION_IDS = Set.of(12_889);
	private static final Set<Integer> LIGHT_ONLY_OPEN_WORLD_REGION_IDS = Set.of(7_316);
	private static final Set<Integer> SNOW_PROFILE_EXCLUDED_REGION_IDS = Set.of(12_895);

	private static final Set<Integer> TOA_REGION_IDS = Set.of(
			13_454, 15_700, 14_164, 14_676, 15_188, 15_184, 15_696
	);
	private static final Set<Integer> TOB_REGION_IDS = Set.of(
			12_613, 13_125, 13_122, 13_123, 13_379, 12_612, 12_611
	);
	private static final Set<Integer> NIGHTMARE_REGION_IDS = Set.of(15_515);
	private static final Set<Integer> ROYAL_TITANS_REGION_IDS = Set.of(11_669);
	private static final Set<Integer> FORTIS_COLOSSEUM_REGION_IDS = Set.of(7_216);
	private static final Set<Integer> DOOM_OF_MOKHAIOTL_REGION_IDS = Set.of(5_269, 13_668, 14_180);
	private static final Set<Integer> POH_REGION_IDS = Set.of(8_302, 8_303);
	private static final Map<Integer, AreaToggle> REGION_AREA_TOGGLES = buildRegionAreaToggles();

	private static final int OLM_REGION_ID = 12_889;
	private static final int OLM_ROOM_PLANE = 0;
	private static final int[][] OLM_ROOM_TILE_RANGES = {
			{27, 37, 45},
			{26, 36, 46},
			{25, 36, 46},
			{24, 36, 52},
			{23, 35, 52},
			{22, 34, 52},
			{21, 34, 52},
			{20, 35, 52},
			{19, 36, 52},
			{18, 39, 52},
			{17, 39, 52},
			{38, 42, 52},
			{39, 42, 52},
			{40, 36, 52},
			{41, 36, 52},
			{42, 36, 53},
			{43, 36, 54},
			{44, 36, 54},
			{45, 36, 53},
			{46, 36, 52},
			{47, 36, 51},
			{48, 36, 49},
			{23, 28, 23},
			{24, 21, 18},
			{25, 18, 15},
			{26, 16, 14},
			{27, 16, 14},
			{28, 16, 14},
			{29, 17, 14},
			{30, 16, 14},
			{32, 18, 17},
			{33, 18, 17},
			{34, 18, 17},
			{35, 18, 17},
			{36, 18, 17},
			{37, 19, 17},
			{38, 30, 17},
			{39, 30, 17},
			{40, 30, 17},
			{41, 30, 17},
			{42, 30, 17},
			{43, 30, 17}
	};

	private static final Set<Integer> TILE_RECOLOR_BLACKLIST = buildTileRecolorBlacklist();

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private HDRConfig config;

	private int nextReloadTick = NEXT_REFRESH_UNSET;
	private boolean hasDetectedAreaToggle;
	private AreaToggle lastDetectedAreaToggle = AreaToggle.OPEN_WORLD;
	private BrightnessStats lastRawOpenWorldBrightnessStats = BrightnessStats.EMPTY;
	private final Map<Integer, TileHsl> rawOpenWorldTileHsl = new ConcurrentHashMap<>();
	private final Map<Integer, RegionProfile> openWorldTileProfiles = new ConcurrentHashMap<>();

	private final Map<RegionProfile, ColorMap> colorMaps = buildColorMaps();

	@Override
	protected void startUp() {
		reloadMap();
	}

	@Override
	protected void shutDown() {
		hasDetectedAreaToggle = false;
		lastRawOpenWorldBrightnessStats = BrightnessStats.EMPTY;
		rawOpenWorldTileHsl.clear();
		openWorldTileProfiles.clear();
		reloadMap();
	}

	public void reloadMap() {
		clientThread.invokeLater(() -> {
			if (client.getGameState() == GameState.LOGGED_IN) {
				client.setGameState(GameState.LOADING);
			}
		});
	}

	@Subscribe
	@SuppressWarnings("unused")
	public void onConfigChanged(ConfigChanged event) {
		if (event.getGroup().equals(ConfigKeys.PLUGIN_CONFIG_GROUP_NAME)) {
			nextReloadTick = client.getTickCount() + 1;
		}
	}

	@Subscribe
	@SuppressWarnings("unused")
	public void onPreMapLoad(PreMapLoad preMapLoad) {
		long start = System.nanoTime();
		recolorMap(preMapLoad.getScene());
		long end = System.nanoTime();
		long duration = end - start;
		log.debug("Map recolor done in {}ms", duration / 1_000_000);
	}

	@Subscribe
	@SuppressWarnings("unused")
	public void onGameTick(GameTick gameTick) {
		if (nextReloadTick != NEXT_REFRESH_UNSET && client.getTickCount() >= nextReloadTick) {
			reloadMap();
			nextReloadTick = NEXT_REFRESH_UNSET;
			return;
		}
		reloadOnAreaTransition();
	}

	private void reloadOnAreaTransition() {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}

		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || localPlayer.getLocalLocation() == null) {
			return;
		}

		WorldPoint currentWorldPoint = WorldPoint.fromLocalInstance(client, localPlayer.getLocalLocation());
		if (currentWorldPoint == null) {
			currentWorldPoint = localPlayer.getWorldLocation();
		}

		AreaToggle currentAreaToggle = getAreaToggle(currentWorldPoint);
		if (!hasDetectedAreaToggle) {
			lastDetectedAreaToggle = currentAreaToggle;
			hasDetectedAreaToggle = true;
			return;
		}

		AreaToggle previousAreaToggle = lastDetectedAreaToggle;
		lastDetectedAreaToggle = currentAreaToggle;
		if (shouldReloadOnAreaToggleChange(previousAreaToggle, currentAreaToggle)) {
			reloadMap();
		}
	}

	private boolean shouldReloadOnAreaToggleChange(AreaToggle previousAreaToggle, AreaToggle currentAreaToggle) {
		// Original logic: reload when entering an instance from the open world
		if (previousAreaToggle == AreaToggle.OPEN_WORLD && currentAreaToggle != AreaToggle.OPEN_WORLD) {
			return true;
		}

		// New logic: force a scene rebuild when transitioning up or down the Olm rope
		if ((previousAreaToggle == AreaToggle.COX && currentAreaToggle == AreaToggle.COX_OLM) ||
				(previousAreaToggle == AreaToggle.COX_OLM && currentAreaToggle == AreaToggle.COX)) {
			return true;
		}

		return false;
	}

	@Subscribe
	@SuppressWarnings("unused")
	public void onMenuOpened(MenuOpened event) {
		if (!client.isKeyPressed(KeyCode.KC_CONTROL)) {
			return;
		}

		Scene scene = client.getScene();
		if (scene == null) {
			return;
		}

		client.getMenu().createMenuEntry(-1)
				.setOption("Copy HDR brightness stats")
				.setTarget(lastRawOpenWorldBrightnessStats.profile.name())
				.setType(MenuAction.RUNELITE)
				.onClick(entry -> copyToClipboard(formatOpenWorldBrightnessStats(lastRawOpenWorldBrightnessStats)));

		Tile tile = client.getSelectedSceneTile();
		if (tile == null) {
			return;
		}

		WorldPoint worldPoint = getTileWorldPoint(scene, tile);
		if (worldPoint == null) {
			return;
		}

		String displayKey = formatTileKey(worldPoint);
		String copiedKey = formatTileKeyInitializer(worldPoint);
		client.getMenu().createMenuEntry(-1)
				.setOption("Copy HDR tile key")
				.setTarget(displayKey)
				.setType(MenuAction.RUNELITE)
				.onClick(entry -> copyToClipboard(copiedKey));
	}

	@Provides
	@SuppressWarnings({"unused", "PMD.CommentDefaultAccessModifier"})
	HDRConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(HDRConfig.class);
	}

	private void recolorMap(Scene scene) {
		boolean isInstance = scene.isInstance();
		log.debug("Recolor map... instance={}", isInstance);
		cacheRawOpenWorldTileHsl(scene);
		lastRawOpenWorldBrightnessStats = getOpenWorldBrightnessStats(scene);
		logOpenWorldBrightnessStats(lastRawOpenWorldBrightnessStats);

		long colorMapsStart = System.nanoTime();
		for (RegionProfile profile : RegionProfile.values()) {
			LightnessRange lightnessRange = calculateDynamicLightnessRange(scene, profile);
			int lightnessReductionThreshold = calculateLightnessReductionThreshold(lightnessRange, profile.lightnessReductionTargetPercent);
			int shadowThreshold = calculateShadowThreshold(lightnessRange, profile.shadowTargetPercent);
			updateColorMap(profile, lightnessReductionThreshold, shadowThreshold);
		}
		long colorMapsEnd = System.nanoTime();

		long tilesDuration = 0;
		Tile[][][] tiles = getSceneTiles(scene);
		for (Tile[][] zTiles : tiles) {
			for (Tile[] xTiles : zTiles) {
				for (Tile tile : xTiles) {
					tilesDuration += recolorTile(scene, tile);
				}
			}
		}

		log.debug("Color maps updated in {}ms, tiles colored in {}ms",
				(colorMapsEnd - colorMapsStart) / 1_000_000,
				tilesDuration / 1_000_000
		);
	}

	private void cacheRawOpenWorldTileHsl(Scene scene) {
		rawOpenWorldTileHsl.clear();
		openWorldTileProfiles.clear();
		Tile[][][] tiles = getSceneTiles(scene);
		for (Tile[][] zTiles : tiles) {
			for (Tile[] xTiles : zTiles) {
				for (Tile tile : xTiles) {
					cacheRawOpenWorldTileHsl(scene, tile);
				}
			}
		}
	}

	private void cacheRawOpenWorldTileHsl(Scene scene, Tile tile) {
		if (tile == null) {
			return;
		}

		Tile bridgeTile = tile.getBridge();
		if (bridgeTile != null) {
			cacheRawOpenWorldTileHsl(scene, bridgeTile);
		}

		WorldPoint worldPoint = getTileWorldPoint(scene, tile);
		if (worldPoint == null || getAreaToggle(worldPoint) != AreaToggle.OPEN_WORLD) {
			return;
		}

		TileHsl hsl = getRepresentativeTileHsl(tile);
		if (!hsl.isEmpty()) {
			rawOpenWorldTileHsl.put(tileKey(worldPoint), hsl);
		}
	}

	private long recolorTile(Scene scene, Tile tile) {
		if (tile == null) {
			return 0;
		}

		long start = System.nanoTime();

		Tile bridgeTile = tile.getBridge();
		if (bridgeTile != null) {
			recolorTile(scene, bridgeTile);
		}

		WorldPoint worldPoint = getTileWorldPoint(scene, tile);
		if (shouldSkipTile(tile, worldPoint)) {
			return System.nanoTime() - start;
		}

		RegionProfile profile = getTileRegionProfile(worldPoint, tile);
		ColorMap colorMap = getColorMap(profile);
		SceneTilePaint paint = tile.getSceneTilePaint();

		if (paint != null && paint.getTexture() == -1) {
			int newNw = colorMap.getModifiedHsl(paint.getNwColor());
			int newNe = colorMap.getModifiedHsl(paint.getNeColor());
			int newSw = colorMap.getModifiedHsl(paint.getSwColor());
			int newSe = colorMap.getModifiedHsl(paint.getSeColor());

			if (OVERRIDE_TILE_COLOR_ENABLED) {
				newNw = OVERRIDE_TILE_COLOR;
				newNe = OVERRIDE_TILE_COLOR;
				newSw = OVERRIDE_TILE_COLOR;
				newSe = OVERRIDE_TILE_COLOR;
			} else if (TILE_FLAT_SHADING) {
				newNe = newNw;
				newSw = newNw;
				newSe = newNw;
			}

			paint.setNwColor(newNw);
			paint.setNeColor(newNe);
			paint.setSwColor(newSw);
			paint.setSeColor(newSe);

			tile.setSceneTilePaint(paint);
		}

		SceneTileModel model = tile.getSceneTileModel();
		if (model != null) {
			ColorAdjuster.adjustSceneTileModel(model, colorMap, TILE_FLAT_SHADING, OVERRIDE_TILE_COLOR_ENABLED, OVERRIDE_TILE_COLOR);
			tile.setSceneTileModel(model);
		}

		long end = System.nanoTime();
		return end - start;
	}

	@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.CognitiveComplexity", "PMD.NPathComplexity", "PMD.AvoidLiteralsInIfCondition"})
	private LightnessRange calculateDynamicLightnessRange(Scene scene, RegionProfile profile) {
		int[] histogram = new int[128];
		int totalTiles = 0;
		Tile[][][] tiles = getSceneTiles(scene);

		for (Tile[][] zTiles : tiles) {
			for (Tile[] xTiles : zTiles) {
				for (Tile tile : xTiles) {
					if (tile == null) {
						continue;
					}

					WorldPoint worldPoint = getTileWorldPoint(scene, tile);
					if (shouldSkipTile(tile, worldPoint) || !isTileInProfile(worldPoint, tile, profile)) {
						continue;
					}

					int lightness = getAverageTileLightness(tile);
					if (lightness >= Colors.MIN_LIGHTNESS && lightness <= Colors.MAX_LIGHTNESS) {
						histogram[lightness]++;
						totalTiles++;
					}
				}
			}
		}

		if (totalTiles == 0) {
			return LightnessRange.EMPTY;
		}

		int minLightness = 0;
		int maxLightness = 127;

		int lowerOutlierThreshold = (int) (totalTiles * 0.02f);
		int upperOutlierThreshold = (int) (totalTiles * 0.98f);

		int currentCount = 0;
		boolean foundMin = false;

		for (int i = 0; i < histogram.length; i++) {
			currentCount += histogram[i];

			if (!foundMin && currentCount >= lowerOutlierThreshold) {
				minLightness = i;
				foundMin = true;
			}
			if (currentCount >= upperOutlierThreshold) {
				maxLightness = i;
				break;
			}
		}

		return new LightnessRange(minLightness, maxLightness);
	}

	private int calculateShadowThreshold(LightnessRange lightnessRange, int targetPercent) {
		if (targetPercent <= 0) {
			return -1;
		}
		if (targetPercent >= 100 || lightnessRange.isEmpty()) {
			return Colors.MAX_LIGHTNESS;
		}

		int dynamicThreshold = lightnessRange.min + (int) (lightnessRange.range() * (targetPercent / 100.0f));
		return Utils.clamp(dynamicThreshold, Colors.MIN_LIGHTNESS, Colors.MAX_LIGHTNESS);
	}

	private int calculateLightnessReductionThreshold(LightnessRange lightnessRange, int targetPercent) {
		if (targetPercent <= 0) {
			return Colors.MAX_LIGHTNESS + 1;
		}
		if (targetPercent >= 100 || lightnessRange.isEmpty()) {
			return Colors.MIN_LIGHTNESS;
		}

		int dynamicThreshold = lightnessRange.max - (int) (lightnessRange.range() * (targetPercent / 100.0f));
		return Utils.clamp(dynamicThreshold, Colors.MIN_LIGHTNESS, Colors.MAX_LIGHTNESS);
	}

	private int getAverageTileLightness(Tile tile) {
		TileHsl hsl = getRepresentativeTileHsl(tile);
		return hsl.isEmpty() ? -1 : hsl.lightness;
	}

	private TileHsl getRepresentativeTileHsl(Tile tile) {
		SceneTilePaint paint = tile.getSceneTilePaint();
		if (paint != null && paint.getTexture() == -1) {
			return getPaintHsl(paint);
		}

		SceneTileModel model = tile.getSceneTileModel();
		if (model != null) {
			return getModelHsl(model);
		}
		return TileHsl.EMPTY;
	}

	private TileHsl getPaintHsl(SceneTilePaint paint) {
		TileHslAccumulator accumulator = new TileHslAccumulator();
		accumulator.add(paint.getNwColor());
		accumulator.add(paint.getNeColor());
		accumulator.add(paint.getSwColor());
		accumulator.add(paint.getSeColor());
		return accumulator.average();
	}

	private TileHsl getModelHsl(SceneTileModel model) {
		int[] colorsA = model.getTriangleColorA();
		if (colorsA == null || colorsA.length == 0) {
			return TileHsl.EMPTY;
		}

		TileHslAccumulator accumulator = new TileHslAccumulator();
		int[] colorsB = model.getTriangleColorB();
		int[] colorsC = model.getTriangleColorC();
		int[] textures = model.getTriangleTextureId();
		for (int i = 0; i < colorsA.length; i++) {
			if (textures == null || textures.length <= i || textures[i] == -1) {
				accumulator.add(colorsA[i]);
				accumulator.add(colorsB, i);
				accumulator.add(colorsC, i);
			}
		}
		return accumulator.average();
	}

	private WorldPoint getTileWorldPoint(Scene scene, Tile tile) {
		return WorldPoint.fromLocalInstance(scene, tile.getLocalLocation(), tile.getPlane());
	}

	private RegionProfile getRegionProfile(WorldPoint worldPoint) {
		switch (getAreaToggle(worldPoint)) {
			case COX:
				return RegionProfile.COX;
			case COX_OLM:
				return RegionProfile.COX_OLM;
			case LIGHT_ONLY_OPEN_WORLD:
				return RegionProfile.LIGHT_ONLY_OPEN_WORLD;
			case TOA:
				return RegionProfile.TOA;
			case TOB:
				return RegionProfile.TOB;
			case NIGHTMARE:
				return RegionProfile.NIGHTMARE;
			case ROYAL_TITANS:
				return RegionProfile.ROYAL_TITANS;
			case FORTIS_COLOSSEUM:
				return RegionProfile.FORTIS_COLOSSEUM;
			case DOOM_OF_MOKHAIOTL:
				return RegionProfile.DOOM_OF_MOKHAIOTL;
			case POH:
				return RegionProfile.POH;
			case OPEN_WORLD:
				return RegionProfile.OPEN_WORLD;
			default:
				return RegionProfile.OPEN_WORLD;
		}
	}

	private RegionProfile getTileRegionProfile(WorldPoint worldPoint, Tile tile) {
		if (getAreaToggle(worldPoint) != AreaToggle.OPEN_WORLD) {
			return getRegionProfile(worldPoint);
		}
		return getOpenWorldTileProfile(worldPoint, tile);
	}

	private RegionProfile getOpenWorldTileProfile(WorldPoint worldPoint, Tile tile) {
		if (worldPoint == null) {
			return RegionProfile.OPEN_WORLD;
		}

		int key = tileKey(worldPoint);
		RegionProfile cachedProfile = openWorldTileProfiles.get(key);
		if (cachedProfile != null) {
			return cachedProfile;
		}

		TileHsl hsl = getRawOpenWorldTileHsl(worldPoint);
		if (hsl.isEmpty()) {
			hsl = getRepresentativeTileHsl(tile);
		}
		RegionProfile profile = getOpenWorldTileProfile(worldPoint, hsl);
		openWorldTileProfiles.put(key, profile);
		return profile;
	}

	private RegionProfile getOpenWorldTileProfile(WorldPoint worldPoint, TileHsl hsl) {
		int snowProfileLevel = getSnowProfileLevel(worldPoint, hsl);
		if (snowProfileLevel == 0) {
			return RegionProfile.OPEN_WORLD;
		}

		return getBrightOpenWorldProfile(snowProfileLevel);
	}

	private boolean isTileInProfile(WorldPoint worldPoint, Tile tile, RegionProfile profile) {
		if (profile.isOpenWorldProfile()) {
			return getAreaToggle(worldPoint) == AreaToggle.OPEN_WORLD
					&& getOpenWorldTileProfile(worldPoint, tile) == profile;
		}
		return getRegionProfile(worldPoint) == profile;
	}

	private void logOpenWorldBrightnessStats(BrightnessStats brightnessStats) {
		if (log.isDebugEnabled()) {
			log.debug(
					"Open world brightness profile={}, tiles={}, average={}, median={}, brightTiles={}%, snowTiles={}%",
					brightnessStats.profile,
					brightnessStats.totalTiles,
					brightnessStats.averageLightness,
					brightnessStats.medianLightness,
					formatPermillePercent(brightnessStats.brightTilePermille),
					formatPermillePercent(brightnessStats.snowTilePermille));
		}
	}

	@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.CognitiveComplexity", "PMD.NPathComplexity"})
	private BrightnessStats getOpenWorldBrightnessStats(Scene scene) {
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || localPlayer.getLocalLocation() == null) {
			return BrightnessStats.EMPTY;
		}

		LocalPoint playerLocalLocation = localPlayer.getLocalLocation();
		int[] histogram = new int[Colors.MAX_LIGHTNESS + 1];
		int totalTiles = 0;
		int lightnessSum = 0;
		int saturationSum = 0;
		int brightTiles = 0;
		int paleSnowTiles = 0;
		int coldSnowTiles = 0;
		int snowTiles = 0;
		Tile[][][] tiles = getSceneTiles(scene);

		for (Tile[][] zTiles : tiles) {
			for (Tile[] xTiles : zTiles) {
				for (Tile tile : xTiles) {
					if (tile == null) {
						continue;
					}
					if (isTileAbovePlayerPlane(tile) || !isTileNearPlayer(tile, playerLocalLocation)) {
						continue;
					}

					WorldPoint worldPoint = getTileWorldPoint(scene, tile);
					if (getAreaToggle(worldPoint) != AreaToggle.OPEN_WORLD || shouldSkipTile(tile, worldPoint)) {
						continue;
					}

					TileHsl hsl = getRawOpenWorldTileHsl(worldPoint);
					if (hsl.isEmpty()) {
						continue;
					}

					histogram[hsl.lightness]++;
					lightnessSum += hsl.lightness;
					saturationSum += hsl.saturation;
					totalTiles++;
					if (hsl.lightness >= BRIGHT_REGION_BRIGHT_LIGHTNESS) {
						brightTiles++;
					}
					if (isPaleSnowTile(hsl)) {
						paleSnowTiles++;
					}
					if (isColdSnowTile(hsl)) {
						coldSnowTiles++;
					}
					if (isSnowTileOrFilled(worldPoint, hsl)) {
						snowTiles++;
					}
				}
			}
		}

		if (totalTiles < BRIGHT_REGION_MIN_TILES) {
			return new BrightnessStats(RegionProfile.OPEN_WORLD, totalTiles, -1, -1, -1, 0, 0, 0, 0);
		}

		int averageLightness = lightnessSum / totalTiles;
		int averageSaturation = saturationSum / totalTiles;
		int medianLightness = getPercentileLightness(histogram, totalTiles, BRIGHT_REGION_MEDIAN_PERCENTILE);
		int brightTilePermille = brightTiles * BRIGHT_REGION_PERMILLE / totalTiles;
		int paleSnowTilePermille = paleSnowTiles * BRIGHT_REGION_PERMILLE / totalTiles;
		int coldSnowTilePermille = coldSnowTiles * BRIGHT_REGION_PERMILLE / totalTiles;
		int snowTilePermille = snowTiles * BRIGHT_REGION_PERMILLE / totalTiles;
		return new BrightnessStats(
				RegionProfile.OPEN_WORLD,
				totalTiles,
				averageLightness,
				averageSaturation,
				medianLightness,
				brightTilePermille,
				paleSnowTilePermille,
				coldSnowTilePermille,
				snowTilePermille);
	}

	private String formatOpenWorldBrightnessStats(BrightnessStats brightnessStats) {
		return "profile="
				+ brightnessStats.profile
				+ ", classification=perTileSnow"
				+ ", tiles="
				+ brightnessStats.totalTiles
				+ ", averageLightness="
				+ brightnessStats.averageLightness
				+ ", averageSaturation="
				+ brightnessStats.averageSaturation
				+ ", medianLightness="
				+ brightnessStats.medianLightness
				+ ", brightTilePercent="
				+ formatPermillePercent(brightnessStats.brightTilePermille)
				+ "%, paleSnowTilePercent="
				+ formatPermillePercent(brightnessStats.paleSnowTilePermille)
				+ "%, coldSnowTilePercent="
				+ formatPermillePercent(brightnessStats.coldSnowTilePermille)
				+ "%, snowTilePercent="
				+ formatPermillePercent(brightnessStats.snowTilePermille)
				+ "%, minTiles="
				+ BRIGHT_REGION_MIN_TILES
				+ ", sampleRadiusTiles="
				+ BRIGHT_REGION_SAMPLE_RADIUS_TILES
				+ ", skipHigherRenderLevels=true"
				+ ", paleSnowRule=saturation<="
				+ PALE_SNOW_MAX_SATURATION
				+ " lightness>="
				+ PALE_SNOW_MIN_LIGHTNESS
				+ ", coldSnowRule=hue="
				+ COLD_SNOW_MIN_HUE
				+ "-"
				+ COLD_SNOW_MAX_HUE
				+ " saturation<="
				+ COLD_SNOW_MAX_SATURATION
				+ " lightness>="
				+ COLD_SNOW_MIN_LIGHTNESS
				+ ", fillNeighbors="
				+ SNOW_FILL_MIN_DIRECT_NEIGHBORS
				+ "/"
				+ SNOW_FILL_MIN_SUPPORT_NEIGHBORS;
	}

	private String formatPermillePercent(int permille) {
		return permille / PERCENT_PERMILLE + "." + permille % PERCENT_PERMILLE;
	}

	private boolean isSnowTile(TileHsl hsl) {
		return isPaleSnowTile(hsl) || isColdSnowTile(hsl);
	}

	private boolean isSnowTileOrFilled(WorldPoint worldPoint, TileHsl hsl) {
		return getSnowProfileLevel(worldPoint, hsl) > 0;
	}

	private TileHsl getRawOpenWorldTileHsl(WorldPoint worldPoint) {
		if (worldPoint == null) {
			return TileHsl.EMPTY;
		}
		return rawOpenWorldTileHsl.getOrDefault(tileKey(worldPoint), TileHsl.EMPTY);
	}

	private TileHsl getRawOpenWorldTileHsl(WorldPoint worldPoint, int xOffset, int yOffset) {
		if (worldPoint == null) {
			return TileHsl.EMPTY;
		}

		WorldPoint neighborPoint = new WorldPoint(
				worldPoint.getX() + xOffset,
				worldPoint.getY() + yOffset,
				worldPoint.getPlane());
		return getRawOpenWorldTileHsl(neighborPoint);
	}

	private boolean isTileNearPlayer(Tile tile, LocalPoint playerLocalLocation) {
		LocalPoint tileLocalLocation = tile.getLocalLocation();
		if (tileLocalLocation == null) {
			return false;
		}

		int radius = BRIGHT_REGION_SAMPLE_RADIUS_TILES * LOCAL_TILE_SIZE;
		return Math.abs(tileLocalLocation.getX() - playerLocalLocation.getX()) <= radius
				&& Math.abs(tileLocalLocation.getY() - playerLocalLocation.getY()) <= radius;
	}

	private boolean isPaleSnowTile(TileHsl hsl) {
		return hsl.saturation <= PALE_SNOW_MAX_SATURATION
				&& hsl.lightness >= PALE_SNOW_MIN_LIGHTNESS
				&& (hsl.saturation <= NEUTRAL_SNOW_MAX_SATURATION
				|| isColdSnowHue(hsl, COLD_SNOW_MIN_HUE, COLD_SNOW_MAX_HUE));
	}

	private boolean isColdSnowTile(TileHsl hsl) {
		return isColdSnowHue(hsl, COLD_SNOW_MIN_HUE, COLD_SNOW_MAX_HUE)
				&& hsl.saturation <= COLD_SNOW_MAX_SATURATION
				&& hsl.lightness >= COLD_SNOW_MIN_LIGHTNESS;
	}

	private boolean isSnowFillCandidate(TileHsl hsl) {
		return hsl.lightness >= SNOW_FILL_MIN_LIGHTNESS
				&& hsl.saturation <= SNOW_FILL_MAX_SATURATION
				&& isColdOrNeutralSnowHue(hsl, SNOW_FILL_MIN_HUE, SNOW_FILL_MAX_HUE);
	}

	private boolean isColdSnowHue(TileHsl hsl, int minHue, int maxHue) {
		return hsl.hue >= minHue && hsl.hue <= maxHue;
	}

	private boolean isColdOrNeutralSnowHue(TileHsl hsl, int minHue, int maxHue) {
		return hsl.saturation <= NEUTRAL_SNOW_MAX_SATURATION
				|| isColdSnowHue(hsl, minHue, maxHue);
	}

	private int getSnowProfileLevel(WorldPoint worldPoint, TileHsl hsl) {
		if (isSnowProfileExcludedRegion(worldPoint)) {
			return 0;
		}

		if (!isSnowTile(hsl) && !isSnowFillCandidate(hsl)) {
			return 0;
		}

		SnowNeighborhood neighborhood = getSnowNeighborhood(worldPoint);
		if (!neighborhood.hasSnowFillSupport()) {
			return 0;
		}
		return neighborhood.hasStrongSnowSupport() ? SNOW_PROFILE_LEVEL : SNOW_EDGE_PROFILE_LEVEL;
	}

	private boolean isSnowProfileExcludedRegion(WorldPoint worldPoint) {
		return worldPoint != null && SNOW_PROFILE_EXCLUDED_REGION_IDS.contains(worldPoint.getRegionID());
	}

	@SuppressWarnings("PMD.CognitiveComplexity")
	private SnowNeighborhood getSnowNeighborhood(WorldPoint worldPoint) {
		int directSnowNeighbors = 0;
		int supportNeighbors = 0;
		for (int xOffset = -SNOW_NEIGHBORHOOD_RADIUS; xOffset <= SNOW_NEIGHBORHOOD_RADIUS; xOffset++) {
			for (int yOffset = -SNOW_NEIGHBORHOOD_RADIUS; yOffset <= SNOW_NEIGHBORHOOD_RADIUS; yOffset++) {
				if (xOffset == 0 && yOffset == 0) {
					continue;
				}

				TileHsl neighborHsl = getRawOpenWorldTileHsl(worldPoint, xOffset, yOffset);
				if (neighborHsl.isEmpty()) {
					continue;
				}
				if (isSnowTile(neighborHsl)) {
					directSnowNeighbors++;
				}
				if (isSnowTile(neighborHsl) || isSnowFillCandidate(neighborHsl)) {
					supportNeighbors++;
				}
			}
		}
		return new SnowNeighborhood(directSnowNeighbors, supportNeighbors);
	}

	private RegionProfile getBrightOpenWorldProfile(int profileLevel) {
		switch (Utils.clamp(profileLevel, 0, BRIGHT_REGION_MAX_LEVEL)) {
			case 1:
				return RegionProfile.BRIGHT_REGION_MINUS_4;
			case 2:
				return RegionProfile.BRIGHT_REGION_MINUS_2;
			case 3:
				return RegionProfile.BRIGHT_REGION_0;
			case 4:
				return RegionProfile.BRIGHT_REGION_2;
			case 5:
				return RegionProfile.BRIGHT_REGION_4;
			case 6:
				return RegionProfile.BRIGHT_REGION_6;
			case 7:
				return RegionProfile.BRIGHT_REGION_8;
			case 8:
				return RegionProfile.BRIGHT_REGION_10;
			case 9:
				return RegionProfile.BRIGHT_REGION_12;
			case 10:
				return RegionProfile.BRIGHT_REGION_14;
			case 11:
				return RegionProfile.BRIGHT_REGION_16;
			case 12:
				return RegionProfile.BRIGHT_REGION_18;
			case 13:
				return RegionProfile.BRIGHT_REGION_20;
			default:
				return RegionProfile.OPEN_WORLD;
		}
	}

	private int getPercentileLightness(int[] histogram, int totalTiles, int percentile) {
		int targetCount = Math.max(1, totalTiles * percentile / 100);
		int currentCount = 0;
		for (int lightness = 0; lightness < histogram.length; lightness++) {
			currentCount += histogram[lightness];
			if (currentCount >= targetCount) {
				return lightness;
			}
		}
		return Colors.MAX_LIGHTNESS;
	}

	private boolean shouldSkipTile(Tile tile, WorldPoint worldPoint) {
		if (worldPoint != null
				&& (shouldSkipHigherPlaneTiles(worldPoint) && isTileAbovePlayerPlane(tile)
				|| isTileBlacklisted(worldPoint))) {
			return true;
		}
		return !isAreaEnabled(worldPoint);
	}

	private boolean shouldSkipHigherPlaneTiles(WorldPoint worldPoint) {
		return getAreaToggle(worldPoint) != AreaToggle.OPEN_WORLD;
	}

	private boolean isTileAbovePlayerPlane(Tile tile) {
		return tile.getRenderLevel() > client.getPlane();
	}

	private boolean isAreaEnabled(WorldPoint worldPoint) {
		switch (getAreaToggle(worldPoint)) {
			case COX:
			case COX_OLM:
				return config.isCoxEnabled();
			case TOA:
				return config.isToaEnabled();
			case TOB:
				return config.isTobEnabled();
			case NIGHTMARE:
				return config.isNightmareEnabled();
			case ROYAL_TITANS:
				return config.isRoyalTitansEnabled();
			case FORTIS_COLOSSEUM:
				return config.isFortisColosseumEnabled();
			case DOOM_OF_MOKHAIOTL:
				return config.isDoomOfMokhaiotlEnabled();
			case POH:
				return config.isPohEnabled();
			case OPEN_WORLD:
			default:
				return config.isOpenWorldEnabled();
		}
	}

	private AreaToggle getAreaToggle(WorldPoint worldPoint) {
		if (worldPoint == null) {
			return AreaToggle.OPEN_WORLD;
		}
		return REGION_AREA_TOGGLES.getOrDefault(worldPoint.getRegionID(), AreaToggle.OPEN_WORLD);
	}

	private static Map<Integer, AreaToggle> buildRegionAreaToggles() {
		Map<Integer, AreaToggle> areaToggles = new ConcurrentHashMap<>();
		addRegionAreaToggles(areaToggles, COX_REGION_IDS, AreaToggle.COX);
		addRegionAreaToggles(areaToggles, COX_OLM_REGION_IDS, AreaToggle.COX_OLM);
		addRegionAreaToggles(areaToggles, LIGHT_ONLY_OPEN_WORLD_REGION_IDS, AreaToggle.LIGHT_ONLY_OPEN_WORLD);
		addRegionAreaToggles(areaToggles, TOA_REGION_IDS, AreaToggle.TOA);
		addRegionAreaToggles(areaToggles, TOB_REGION_IDS, AreaToggle.TOB);
		addRegionAreaToggles(areaToggles, NIGHTMARE_REGION_IDS, AreaToggle.NIGHTMARE);
		addRegionAreaToggles(areaToggles, ROYAL_TITANS_REGION_IDS, AreaToggle.ROYAL_TITANS);
		addRegionAreaToggles(areaToggles, FORTIS_COLOSSEUM_REGION_IDS, AreaToggle.FORTIS_COLOSSEUM);
		addRegionAreaToggles(areaToggles, DOOM_OF_MOKHAIOTL_REGION_IDS, AreaToggle.DOOM_OF_MOKHAIOTL);
		addRegionAreaToggles(areaToggles, POH_REGION_IDS, AreaToggle.POH);
		return Collections.unmodifiableMap(areaToggles);
	}

	private static void addRegionAreaToggles(Map<Integer, AreaToggle> areaToggles, Set<Integer> regionIds, AreaToggle areaToggle) {
		for (int regionId : regionIds) {
			areaToggles.put(regionId, areaToggle);
		}
	}

	private static Set<Integer> buildTileRecolorBlacklist() {
		Set<Integer> tiles = new HashSet<>();
		for (int[] range : OLM_ROOM_TILE_RANGES) {
			addTileRange(tiles, OLM_REGION_ID, OLM_ROOM_PLANE, range[0], range[1], range[2]);
		}
		addTileRange(tiles, 13_136, 0, 55, 36, 39);
		tiles.add(tileKey(13_136, 61, 50, 0));
		tiles.add(tileKey(13_136, 35, 49, 0));
		tiles.add(tileKey(13_136, 59, 56, 0));
		tiles.add(tileKey(13_136, 40, 60, 0));
		tiles.add(tileKey(13_136, 44, 40, 0));
		tiles.add(tileKey(13_136, 54, 41, 0));
		return Collections.unmodifiableSet(tiles);
	}

	private static void addTileRange(Set<Integer> tiles, int regionId, int plane, int regionX, int startRegionY, int endRegionY) {
		int firstRegionY = Math.min(startRegionY, endRegionY);
		int lastRegionY = Math.max(startRegionY, endRegionY);
		for (int regionY = firstRegionY; regionY <= lastRegionY; regionY++) {
			tiles.add(tileKey(regionId, regionX, regionY, plane));
		}
	}

	private boolean isTileBlacklisted(WorldPoint worldPoint) {
		return worldPoint != null && TILE_RECOLOR_BLACKLIST.contains(tileKey(worldPoint));
	}

	private void copyToClipboard(String text) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
		log.info("Copied HDR tile key: {}", text);
	}

	private static int tileKey(WorldPoint worldPoint) {
		return tileKey(
				worldPoint.getRegionID(),
				worldPoint.getRegionX(),
				worldPoint.getRegionY(),
				worldPoint.getPlane());
	}

	private static int tileKey(int regionId, int regionX, int regionY, int plane) {
		return (regionId << 14) | (plane << 12) | (regionX << 6) | regionY;
	}

	private static String formatTileKey(WorldPoint worldPoint) {
		return worldPoint.getRegionID()
				+ ":"
				+ worldPoint.getRegionX()
				+ ":"
				+ worldPoint.getRegionY()
				+ ":"
				+ worldPoint.getPlane();
	}

	private static String formatTileKeyInitializer(WorldPoint worldPoint) {
		return "tileKey("
				+ worldPoint.getRegionID()
				+ ", "
				+ worldPoint.getRegionX()
				+ ", "
				+ worldPoint.getRegionY()
				+ ", "
				+ worldPoint.getPlane()
				+ ")";
	}

	private Tile[][][] getSceneTiles(Scene scene) {
		return scene.isInstance() ? scene.getTiles() : scene.getExtendedTiles();
	}

	private void updateColorMap(RegionProfile profile, int lightnessReductionThreshold, int shadowThreshold) {
		TargetSaturation targetSaturation = getTargetSaturation(profile);

		ColorMap.Settings settings = new ColorMap.Settings()
				.withBaseAdjustments(TILE_HUE_REDUCTION, TILE_SATURATION_REDUCTION)
				.withLightness(
						profile.lightnessReduction,
						lightnessReductionThreshold,
						profile.lightnessIncrease,
						shadowThreshold,
						getFinalLightnessAdjustment(profile))
				.withTargetSaturation(
						targetSaturation.adjustment,
						targetSaturation.hue,
						targetSaturation.hueRange);

		getColorMap(profile).updateColors(settings);
	}

	private TargetSaturation getTargetSaturation(RegionProfile profile) {
		if (profile.isBrightOpenWorldProfile()) {
			return new TargetSaturation(
					config.getTargetSaturationAdjustment(),
					config.getTargetSaturationColor(),
					config.getTargetSaturationHueRange());
		}

		switch (profile) {
			case COX:
			case COX_OLM:
				return new TargetSaturation(
						config.getCoxTargetSaturationAdjustment(),
						config.getCoxTargetSaturationColor(),
						config.getCoxTargetSaturationHueRange());
			case TOA:
				return new TargetSaturation(
						config.getToaTargetSaturationAdjustment(),
						config.getToaTargetSaturationColor(),
						config.getToaTargetSaturationHueRange());
			case TOB:
				return new TargetSaturation(
						config.getTobTargetSaturationAdjustment(),
						config.getTobTargetSaturationColor(),
						config.getTobTargetSaturationHueRange());
			case NIGHTMARE:
				return new TargetSaturation(
						config.getNightmareTargetSaturationAdjustment(),
						config.getNightmareTargetSaturationColor(),
						config.getNightmareTargetSaturationHueRange());
			case ROYAL_TITANS:
				return new TargetSaturation(
						config.getRoyalTitansTargetSaturationAdjustment(),
						config.getRoyalTitansTargetSaturationColor(),
						config.getRoyalTitansTargetSaturationHueRange());
			case FORTIS_COLOSSEUM:
				return new TargetSaturation(
						config.getFortisColosseumTargetSaturationAdjustment(),
						config.getFortisColosseumTargetSaturationColor(),
						config.getFortisColosseumTargetSaturationHueRange());
			case DOOM_OF_MOKHAIOTL:
				return new TargetSaturation(
						config.getDoomOfMokhaiotlTargetSaturationAdjustment(),
						config.getDoomOfMokhaiotlTargetSaturationColor(),
						config.getDoomOfMokhaiotlTargetSaturationHueRange());
			case POH:
				return new TargetSaturation(
						config.getPohTargetSaturationAdjustment(),
						config.getPohTargetSaturationColor(),
						config.getPohTargetSaturationHueRange());
			case OPEN_WORLD:
			default:
				return new TargetSaturation(
						config.getTargetSaturationAdjustment(),
						config.getTargetSaturationColor(),
						config.getTargetSaturationHueRange());
		}
	}

	private int getFinalLightnessAdjustment(RegionProfile profile) {
		if (profile.isBrightOpenWorldProfile()) {
			return profile.baseFinalLightnessAdjustment + SNOW_FINAL_LIGHTNESS_OFFSET + config.getFinalLightnessAdjustment();
		}

		switch (profile) {
			case COX:
			case COX_OLM:
				return profile.baseFinalLightnessAdjustment + config.getCoxFinalLightnessAdjustment();
			case TOA:
				return profile.baseFinalLightnessAdjustment + config.getToaFinalLightnessAdjustment();
			case TOB:
				return profile.baseFinalLightnessAdjustment + config.getTobFinalLightnessAdjustment();
			case NIGHTMARE:
				return profile.baseFinalLightnessAdjustment + config.getNightmareFinalLightnessAdjustment();
			case ROYAL_TITANS:
				return profile.baseFinalLightnessAdjustment + config.getRoyalTitansFinalLightnessAdjustment();
			case FORTIS_COLOSSEUM:
				return profile.baseFinalLightnessAdjustment + config.getFortisColosseumFinalLightnessAdjustment();
			case DOOM_OF_MOKHAIOTL:
				return profile.baseFinalLightnessAdjustment + config.getDoomOfMokhaiotlFinalLightnessAdjustment();
			case POH:
				return profile.baseFinalLightnessAdjustment + config.getPohFinalLightnessAdjustment();
			case OPEN_WORLD:
			default:
				return profile.baseFinalLightnessAdjustment + config.getFinalLightnessAdjustment();
		}
	}

	private ColorMap getColorMap(RegionProfile profile) {
		return colorMaps.get(profile);
	}

	@SuppressWarnings({"PMD.UseConcurrentHashMap", "PMD.AvoidInstantiatingObjectsInLoops"})
	private static Map<RegionProfile, ColorMap> buildColorMaps() {
		Map<RegionProfile, ColorMap> maps = new EnumMap<>(RegionProfile.class);
		for (RegionProfile profile : RegionProfile.values()) {
			maps.put(profile, new ColorMap());
		}
		return Collections.unmodifiableMap(maps);
	}

	private enum AreaToggle {
		OPEN_WORLD,
		LIGHT_ONLY_OPEN_WORLD,
		COX,
		COX_OLM,
		TOA,
		TOB,
		NIGHTMARE,
		ROYAL_TITANS,
		FORTIS_COLOSSEUM,
		DOOM_OF_MOKHAIOTL,
		POH
	}

	private enum RegionProfile {
		// Values are: lightness reduction, bright-tile target, lightness boost, shadow target, base final lightness.
		COX(0, 0, 50, 16, -2),
		COX_OLM(0, 0, 20, 75, 2), //30 - 80 instead?
		LIGHT_ONLY_OPEN_WORLD(0, 0, 50, 50, 0),
		TOA(70, 35, 50, 40, -6),
		TOB(70, 35, 50, 40, +1),
		NIGHTMARE(70, 35, 50, 40, -6),
		ROYAL_TITANS(70, 35, 50, 40, -11),
		FORTIS_COLOSSEUM(70, 35, 100, 60, +1),
		DOOM_OF_MOKHAIOTL(70, 35, 50, 100, +2),
		POH(70, 35, 25, 40, +1),
		BRIGHT_REGION_MINUS_4(70, 35, 50, 40, -4, true),
		BRIGHT_REGION_MINUS_2(70, 35, 50, 40, -2, true),
		BRIGHT_REGION_0(70, 35, 50, 40, 0, true),
		BRIGHT_REGION_2(70, 35, 50, 40, +2, true),
		BRIGHT_REGION_4(70, 35, 50, 40, +4, true),
		BRIGHT_REGION_6(70, 35, 50, 40, +6, true),
		BRIGHT_REGION_8(70, 35, 50, 40, +8, true),
		BRIGHT_REGION_10(70, 35, 50, 40, +10, true),
		BRIGHT_REGION_12(70, 35, 50, 40, +12, true),
		BRIGHT_REGION_14(70, 35, 50, 40, +14, true),
		BRIGHT_REGION_16(70, 35, 50, 40, +16, true),
		BRIGHT_REGION_18(70, 35, 50, 40, +18, true),
		BRIGHT_REGION_20(70, 35, 50, 40, +20, true),
		OPEN_WORLD(70, 35, 50, 40, -2);

		private final int lightnessReduction;
		private final int lightnessReductionTargetPercent;
		private final int lightnessIncrease;
		private final int shadowTargetPercent;
		private final int baseFinalLightnessAdjustment;
		private final boolean brightOpenWorldProfile;

		RegionProfile(
				int lightnessReduction,
				int lightnessReductionTargetPercent,
				int lightnessIncrease,
				int shadowTargetPercent,
				int baseFinalLightnessAdjustment) {
			this(
					lightnessReduction,
					lightnessReductionTargetPercent,
					lightnessIncrease,
					shadowTargetPercent,
					baseFinalLightnessAdjustment,
					false);
		}

		RegionProfile(
				int lightnessReduction,
				int lightnessReductionTargetPercent,
				int lightnessIncrease,
				int shadowTargetPercent,
				int baseFinalLightnessAdjustment,
				boolean brightOpenWorldProfile) {
			this.lightnessReduction = lightnessReduction;
			this.lightnessReductionTargetPercent = lightnessReductionTargetPercent;
			this.lightnessIncrease = lightnessIncrease;
			this.shadowTargetPercent = shadowTargetPercent;
			this.baseFinalLightnessAdjustment = baseFinalLightnessAdjustment;
			this.brightOpenWorldProfile = brightOpenWorldProfile;
		}

		private boolean isBrightOpenWorldProfile() {
			return brightOpenWorldProfile;
		}

		private boolean isOpenWorldProfile() {
			return this == OPEN_WORLD || brightOpenWorldProfile;
		}
	}

	private static final class TargetSaturation {
		private final int adjustment;
		private final int hue;
		private final int hueRange;

		private TargetSaturation(int adjustment, java.awt.Color color, int hueRange) {
			this.adjustment = adjustment;
			this.hue = Colors.unpackJagexHue(Colors.colorToJagexHsl(color));
			this.hueRange = hueRange;
		}
	}

	private static final class TileHsl {
		private static final TileHsl EMPTY = new TileHsl(-1, -1, -1);

		private final int hue;
		private final int saturation;
		private final int lightness;

		private TileHsl(int hue, int saturation, int lightness) {
			this.hue = hue;
			this.saturation = saturation;
			this.lightness = lightness;
		}

		private boolean isEmpty() {
			return lightness < 0;
		}
	}

	private static final class TileHslAccumulator {
		private int hueSum;
		private int saturationSum;
		private int lightnessSum;
		private int colors;

		private void add(int hsl) {
			if (hsl < 0) {
				return;
			}

			hueSum += Colors.unpackJagexHue(hsl);
			saturationSum += Colors.unpackJagexSaturation(hsl);
			lightnessSum += Colors.unpackJagexLightness(hsl);
			colors++;
		}

		private void add(int[] hslValues, int index) {
			if (hslValues != null && index < hslValues.length) {
				add(hslValues[index]);
			}
		}

		private TileHsl average() {
			if (colors == 0) {
				return TileHsl.EMPTY;
			}

			return new TileHsl(
					hueSum / colors,
					saturationSum / colors,
					lightnessSum / colors);
		}
	}

	private static final class SnowNeighborhood {
		private final int directSnowNeighbors;
		private final int supportNeighbors;

		private SnowNeighborhood(int directSnowNeighbors, int supportNeighbors) {
			this.directSnowNeighbors = directSnowNeighbors;
			this.supportNeighbors = supportNeighbors;
		}

		private boolean hasSnowFillSupport() {
			return directSnowNeighbors >= SNOW_FILL_MIN_DIRECT_NEIGHBORS
					&& supportNeighbors >= SNOW_FILL_MIN_SUPPORT_NEIGHBORS;
		}

		private boolean hasStrongSnowSupport() {
			return directSnowNeighbors >= SNOW_STRONG_MIN_DIRECT_NEIGHBORS
					&& supportNeighbors >= SNOW_STRONG_MIN_SUPPORT_NEIGHBORS;
		}
	}

	@SuppressWarnings("PMD.TooManyFields")
	private static final class BrightnessStats {
		private static final BrightnessStats EMPTY = new BrightnessStats(RegionProfile.OPEN_WORLD, 0, -1, -1, -1, 0, 0, 0, 0);

		private final RegionProfile profile;
		private final int totalTiles;
		private final int averageLightness;
		private final int averageSaturation;
		private final int medianLightness;
		private final int brightTilePermille;
		private final int paleSnowTilePermille;
		private final int coldSnowTilePermille;
		private final int snowTilePermille;

		private BrightnessStats(
				RegionProfile profile,
				int totalTiles,
				int averageLightness,
				int averageSaturation,
				int medianLightness,
				int brightTilePermille,
				int paleSnowTilePermille,
				int coldSnowTilePermille,
				int snowTilePermille) {
			this.profile = profile;
			this.totalTiles = totalTiles;
			this.averageLightness = averageLightness;
			this.averageSaturation = averageSaturation;
			this.medianLightness = medianLightness;
			this.brightTilePermille = brightTilePermille;
			this.paleSnowTilePermille = paleSnowTilePermille;
			this.coldSnowTilePermille = coldSnowTilePermille;
			this.snowTilePermille = snowTilePermille;
		}

	}

	private static final class LightnessRange {
		private static final LightnessRange EMPTY = new LightnessRange(-1, -1);

		private final int min;
		private final int max;

		private LightnessRange(int min, int max) {
			this.min = min;
			this.max = max;
		}

		private boolean isEmpty() {
			return min < 0 || max < 0;
		}

		private int range() {
			return max - min;
		}
	}
}