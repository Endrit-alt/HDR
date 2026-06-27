package com.endrit.hdr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class HDRTileOverlay extends Overlay {
	private static final Color HIDDEN_TILE_FILL_COLOR = new Color(0, 180, 255, 55);
	private static final Color HIDDEN_TILE_BORDER_COLOR = new Color(0, 220, 255, 190);
	private static final Stroke HIDDEN_TILE_STROKE = new BasicStroke(2);

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
}
