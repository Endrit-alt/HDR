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
@SuppressWarnings("PMD.CyclomaticComplexity")
public class HDRPlugin extends Plugin {
	public static final int NEXT_REFRESH_UNSET = -1;

	private static final int TILE_HUE_REDUCTION = 0;
	private static final int TILE_SATURATION_REDUCTION = 0;
	private static final boolean TILE_FLAT_SHADING = false;
	private static final boolean OVERRIDE_TILE_COLOR_ENABLED = false;
	private static final int OVERRIDE_TILE_COLOR = 0;

	private static final Set<Integer> COX_REGION_IDS = Set.of(
		13_136, 13_137, 13_393, 13_138, 13_394, 13_139, 13_395,
		13_140, 13_396, 13_141, 13_397, 13_145, 13_401, 12_889
	);
	private static final Set<Integer> LIGHT_ONLY_OPEN_WORLD_REGION_IDS = Set.of(7_316);

	private static final Set<Integer> TOA_LOBBY_REGION_IDS = Set.of(13_454);
	private static final Set<Integer> TOA_REGION_IDS = Set.of(
		15_700, 14_164, 14_676, 15_188, 15_184, 15_696
	);
	private static final Set<Integer> TOB_REGION_IDS = Set.of(
		12_613, 13_125, 13_122, 13_123, 13_379, 12_612, 12_611
	);
	private static final Set<Integer> NEX_REGION_IDS = Set.of(11_601);
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

	private final Map<RegionProfile, ColorMap> colorMaps = buildColorMaps();

	@Override
	protected void startUp() {
		reloadMap();
	}

	@Override
	protected void shutDown() {
		hasDetectedAreaToggle = false;
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

		if (previousAreaToggle == AreaToggle.OPEN_WORLD && currentAreaToggle != AreaToggle.OPEN_WORLD) {
			reloadMap();
		}
	}

	@Subscribe
	@SuppressWarnings("unused")
	public void onMenuOpened(MenuOpened event) {
		if (!client.isKeyPressed(KeyCode.KC_CONTROL)) {
			return;
		}

		Scene scene = client.getScene();
		Tile tile = client.getSelectedSceneTile();
		if (scene == null || tile == null) {
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

		RegionProfile profile = getRegionProfile(worldPoint);
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
					if (shouldSkipTile(tile, worldPoint) || getRegionProfile(worldPoint) != profile) {
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

	@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.CognitiveComplexity", "PMD.NPathComplexity"})
	private int getAverageTileLightness(Tile tile) {
		SceneTilePaint paint = tile.getSceneTilePaint();
		if (paint != null && paint.getTexture() == -1) {
			return Colors.unpackJagexLightness(paint.getNwColor());
		}

		SceneTileModel model = tile.getSceneTileModel();
		if (model != null) {
			return getModelLightness(model);
		}
		return -1;
	}

	private int getModelLightness(SceneTileModel model) {
		int[] colorsA = model.getTriangleColorA();
		if (colorsA == null || colorsA.length == 0) {
			return -1;
		}

		int[] textures = model.getTriangleTextureId();
		for (int i = 0; i < colorsA.length; i++) {
			if (textures == null || textures.length <= i || textures[i] == -1) {
				return Colors.unpackJagexLightness(colorsA[i]);
			}
		}
		return -1;
	}

	private WorldPoint getTileWorldPoint(Scene scene, Tile tile) {
		return WorldPoint.fromLocalInstance(scene, tile.getLocalLocation(), tile.getPlane());
	}

	private RegionProfile getRegionProfile(WorldPoint worldPoint) {
		switch (getAreaToggle(worldPoint)) {
			case COX:
				return RegionProfile.COX;
			case TOA_LOBBY:
				return RegionProfile.TOA_LOBBY;
			case LIGHT_ONLY_OPEN_WORLD:
				return RegionProfile.LIGHT_ONLY_OPEN_WORLD;
			case TOA:
				return RegionProfile.TOA;
			case TOB:
				return RegionProfile.TOB;
			case NEX:
				return RegionProfile.NEX;
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
			default:
				return RegionProfile.OPEN_WORLD;
		}
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
				return config.isCoxEnabled();
			case TOA_LOBBY:
				return config.isToaLobbyEnabled();
			case TOA:
				return config.isToaEnabled();
			case TOB:
				return config.isTobEnabled();
			case NEX:
				return config.isNexEnabled();
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
		addRegionAreaToggles(areaToggles, LIGHT_ONLY_OPEN_WORLD_REGION_IDS, AreaToggle.LIGHT_ONLY_OPEN_WORLD);
		addRegionAreaToggles(areaToggles, TOA_LOBBY_REGION_IDS, AreaToggle.TOA_LOBBY);
		addRegionAreaToggles(areaToggles, TOA_REGION_IDS, AreaToggle.TOA);
		addRegionAreaToggles(areaToggles, TOB_REGION_IDS, AreaToggle.TOB);
		addRegionAreaToggles(areaToggles, NEX_REGION_IDS, AreaToggle.NEX);
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
		switch (profile) {
			case COX:
				return new TargetSaturation(
					config.getCoxTargetSaturationAdjustment(),
					config.getCoxTargetSaturationColor(),
					config.getCoxTargetSaturationHueRange());
			case TOA_LOBBY:
				return new TargetSaturation(
					config.getToaLobbyTargetSaturationAdjustment(),
					config.getToaLobbyTargetSaturationColor(),
					config.getToaLobbyTargetSaturationHueRange());
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
			case NEX:
				return new TargetSaturation(
					config.getNexTargetSaturationAdjustment(),
					config.getNexTargetSaturationColor(),
					config.getNexTargetSaturationHueRange());
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
		switch (profile) {
			case COX:
				return profile.baseFinalLightnessAdjustment + config.getCoxFinalLightnessAdjustment();
			case TOA_LOBBY:
				return profile.baseFinalLightnessAdjustment + config.getToaLobbyFinalLightnessAdjustment();
			case TOA:
				return profile.baseFinalLightnessAdjustment + config.getToaFinalLightnessAdjustment();
			case TOB:
				return profile.baseFinalLightnessAdjustment + config.getTobFinalLightnessAdjustment();
			case NEX:
				return profile.baseFinalLightnessAdjustment + config.getNexFinalLightnessAdjustment();
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
		TOA_LOBBY,
		TOA,
		TOB,
		NEX,
		NIGHTMARE,
		ROYAL_TITANS,
		FORTIS_COLOSSEUM,
		DOOM_OF_MOKHAIOTL,
		POH
	}

	private enum RegionProfile {
		// Values are: lightness reduction, bright-tile target, lightness boost, shadow target, base final lightness.
		COX(0, 0, 50, 16, -2),
		TOA_LOBBY(0, 0, 35, 18, 0),
		LIGHT_ONLY_OPEN_WORLD(0, 0, 50, 50, 0),
		TOA(70, 35, 50, 40, -6),
		TOB(70, 35, 50, 40, +1),
		NEX(70, 35, 50, 40, +13),
		NIGHTMARE(70, 35, 50, 40, -6),
		ROYAL_TITANS(70, 35, 50, 40, -11),
		FORTIS_COLOSSEUM(70, 35, 100, 60, +1),
		DOOM_OF_MOKHAIOTL(70, 35, 50, 100, +2),
		POH(70, 35, 25, 40, +1),
		OPEN_WORLD(70, 35, 50, 40, -6);

		private final int lightnessReduction;
		private final int lightnessReductionTargetPercent;
		private final int lightnessIncrease;
		private final int shadowTargetPercent;
		private final int baseFinalLightnessAdjustment;

		RegionProfile(
			int lightnessReduction,
			int lightnessReductionTargetPercent,
			int lightnessIncrease,
			int shadowTargetPercent,
			int baseFinalLightnessAdjustment) {
			this.lightnessReduction = lightnessReduction;
			this.lightnessReductionTargetPercent = lightnessReductionTargetPercent;
			this.lightnessIncrease = lightnessIncrease;
			this.shadowTargetPercent = shadowTargetPercent;
			this.baseFinalLightnessAdjustment = baseFinalLightnessAdjustment;
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
