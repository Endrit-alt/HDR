# HDR

HDR is a RuneLite plugin that adjusts OSRS terrain tile lighting and color at map-load time.

The plugin focuses on terrain tiles: flat `SceneTilePaint` tiles and shaped `SceneTileModel` tiles. It works with RuneLite's packed Jagex HSL colors, so changes are applied directly to the game's tile color values rather than as a screen overlay.

## What It Does

HDR can:

* Brighten shadowed terrain using a dynamic shadow target.
* Reduce bright terrain using a separate lightness-reduction target.
* Apply a final lightness adjustment after the main profile changes.
* Adjust saturation only around a chosen target hue.
* Use separate settings for different areas instead of one global profile.
* Skip blacklisted tiles by hardcoded region/local tile coordinates.
* Skip higher render-level tiles in named/special areas to avoid recoloring ceiling or roof-like surfaces.

## Area Profiles

The plugin has hardcoded area detection for:

* Open World
* Chambers of Xeric
* Tombs of Amascut lobby
* Tombs of Amascut encounters
* Theatre of Blood
* Nex
* Nightmare
* Royal Titans
* Fortis Colosseum
* Doom of Mokhaiotl
* Player-owned house (POH)
* Special light-only dungeon regions, such as region `7316`

Each area can have its own config section with:

* Enabled
* Final lightness adjustment
* Saturation adjustment
* Target color
* Hue range

POH is disabled by default because those regions were previously excluded.

## CoX Tile Blacklist

HDR includes a hardcoded coordinate blacklist for specific Chambers of Xeric tiles, including Olm room tiles. These tiles are left untouched so special floor pieces and problem areas can keep their original appearance.

Hold `CTRL` and open the right-click menu on a tile to copy an HDR tile key for adding more blacklist entries in code.

## Reload Behavior

Tile colors are changed during `PreMapLoad`, so a scene reload is required before changes appear. HDR reloads the map after config changes and also auto-reloads once when moving from Open World into a named/special area such as CoX, ToA, ToB, Fortis, Doom of Mokhaiotl, POH, or the `7316` profile.

## Limitations

HDR does not recolor:

* Walls and static scenery objects
* Ground objects
* NPCs, players, projectiles, or items
* Textures such as water or lava
* Animated scene objects
* Minimap tile colors

Some OSRS scenes use unusual plane, render-level, or model behavior. Those cases may need hardcoded region handling or tile blacklists rather than a universal rule.
