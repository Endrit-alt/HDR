package com.endrit.hdr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class HDRTileOverlay extends Overlay {
	private static final Color HIDDEN_TILE_FILL_COLOR = new Color(255, 255, 255, 55);
	private static final Color HIDDEN_TILE_BORDER_COLOR = new Color(255, 255, 255, 190);
	private static final Color TOOLS_LABEL_COLOR = Color.WHITE;
	private static final Color TOOLS_LABEL_BACKGROUND_COLOR = new Color(0, 0, 0, 110);
	private static final Stroke HIDDEN_TILE_STROKE = new BasicStroke(2);
	private static final String TOOLS_ENABLED_LABEL = "No Dark Tiles Plugin Tools Enabled";
	private static final String TOOLS_USAGE_HELP =
			"Use Ctrl + Right Click to disable tiles from the plugins effects";
	private static final String TOOLS_DISABLED_HELP = "Tools can be disabled in the Disable Tiles sub-menu";
	private static final Font TOOLS_ENABLED_LABEL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 11);
	private static final Font TOOLS_HELP_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 10);
	private static final int TOOLS_ENABLED_LABEL_OFFSET = 156;
	private static final int TOOLS_LABEL_LINE_GAP = 2;
	private static final int TOOLS_LABEL_BACKGROUND_PADDING = 4;
	private static final int TOOLS_LABEL_BACKGROUND_ARC = 5;

	private final Client client;
	private final HDRConfig config;
	private final HDRPlugin plugin;

	@Inject
	public HDRTileOverlay(Client client, HDRConfig config, HDRPlugin plugin) {
		super();
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		if (!config.isHideTileToolsEnabled()) {
			return null;
		}

		Scene scene = client.getScene();
		if (scene == null) {
			return null;
		}

		for (Tile[][] zTiles : plugin.getSceneTiles(scene)) {
			for (Tile[] xTiles : zTiles) {
				for (Tile tile : xTiles) {
					renderTile(graphics, scene, tile);
				}
			}
		}
		renderToolsEnabledLabel(graphics);
		return null;
	}

	private void renderTile(Graphics2D graphics, Scene scene, Tile tile) {
		if (tile == null || tile.getRenderLevel() > client.getPlane()) {
			return;
		}

		Tile bridgeTile = tile.getBridge();
		if (bridgeTile != null) {
			renderTile(graphics, scene, bridgeTile);
		}

		WorldPoint worldPoint = plugin.getTileWorldPoint(scene, tile);
		if (!plugin.isUserHiddenTile(worldPoint)) {
			return;
		}

		Polygon polygon = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());
		if (polygon == null) {
			return;
		}

		Stroke previousStroke = graphics.getStroke();
		graphics.setColor(HIDDEN_TILE_FILL_COLOR);
		graphics.fill(polygon);
		graphics.setStroke(HIDDEN_TILE_STROKE);
		graphics.setColor(HIDDEN_TILE_BORDER_COLOR);
		graphics.draw(polygon);
		graphics.setStroke(previousStroke);
	}

	private void renderToolsEnabledLabel(Graphics2D graphics) {
		Player player = client.getLocalPlayer();
		if (player == null) {
			return;
		}

		Polygon playerTile = Perspective.getCanvasTilePoly(client, player.getLocalLocation());
		if (playerTile == null) {
			return;
		}

		Font previousFont = graphics.getFont();
		Color previousColor = graphics.getColor();
		try {
			Rectangle bounds = playerTile.getBounds();
			int feetY = bounds.y + bounds.height / 2;
			int centerX = bounds.x + bounds.width / 2;

			graphics.setFont(TOOLS_ENABLED_LABEL_FONT);
			FontMetrics enabledMetrics = graphics.getFontMetrics();
			int enabledTextY = feetY + TOOLS_ENABLED_LABEL_OFFSET + enabledMetrics.getAscent();

			graphics.setFont(TOOLS_HELP_FONT);
			FontMetrics helpMetrics = graphics.getFontMetrics();
			int usageTextY = enabledTextY
					+ enabledMetrics.getDescent()
					+ TOOLS_LABEL_LINE_GAP
					+ helpMetrics.getAscent();
			int disabledTextY = usageTextY
					+ helpMetrics.getDescent()
					+ TOOLS_LABEL_LINE_GAP
					+ helpMetrics.getAscent();

			int widestText = Math.max(
					enabledMetrics.stringWidth(TOOLS_ENABLED_LABEL),
					Math.max(
							helpMetrics.stringWidth(TOOLS_USAGE_HELP),
							helpMetrics.stringWidth(TOOLS_DISABLED_HELP)));
			int backgroundX = centerX - widestText / 2 - TOOLS_LABEL_BACKGROUND_PADDING;
			int backgroundY = feetY + TOOLS_ENABLED_LABEL_OFFSET - TOOLS_LABEL_BACKGROUND_PADDING;
			int backgroundWidth = widestText + TOOLS_LABEL_BACKGROUND_PADDING * 2;
			int backgroundHeight = disabledTextY
					+ helpMetrics.getDescent()
					- backgroundY
					+ TOOLS_LABEL_BACKGROUND_PADDING;
			graphics.setColor(TOOLS_LABEL_BACKGROUND_COLOR);
			graphics.fillRoundRect(
					backgroundX,
					backgroundY,
					backgroundWidth,
					backgroundHeight,
					TOOLS_LABEL_BACKGROUND_ARC,
					TOOLS_LABEL_BACKGROUND_ARC);

			graphics.setFont(TOOLS_ENABLED_LABEL_FONT);
			renderCenteredText(graphics, centerX, enabledTextY, TOOLS_ENABLED_LABEL);
			graphics.setFont(TOOLS_HELP_FONT);
			renderCenteredText(graphics, centerX, usageTextY, TOOLS_USAGE_HELP);
			renderCenteredText(graphics, centerX, disabledTextY, TOOLS_DISABLED_HELP);
		} finally {
			graphics.setFont(previousFont);
			graphics.setColor(previousColor);
		}
	}

	private static void renderCenteredText(Graphics2D graphics, int centerX, int textY, String text) {
		FontMetrics metrics = graphics.getFontMetrics();
		int textX = centerX - metrics.stringWidth(text) / 2;
			OverlayUtil.renderTextLocation(
					graphics,
					new Point(textX, textY),
					text,
					TOOLS_LABEL_COLOR);
	}
}
