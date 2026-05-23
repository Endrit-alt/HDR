# HDR

HDR is a RuneLite plugin that improves the lighting and color of OSRS terrain.

It changes the look of ground tiles in different areas of the game, making dark areas easier to see and bright areas less harsh.

## What It Does

HDR can:

- Brighten dark terrain
- Reduce overly bright terrain
- Adjust color saturation
- Use different settings for different areas
- Avoid changing certain special tiles that should keep their original look

## Supported Areas

HDR has separate settings for areas such as:

- Open World
- Chambers of Xeric
- Tombs of Amascut
- Theatre of Blood
- Nex
- Nightmare
- Royal Titans
- Fortis Colosseum
- Doom of Mokhaiotl
- Player-owned house

Each area can be enabled or adjusted separately.

## Chambers of Xeric Tile Handling

Some Chambers of Xeric tiles are ignored on purpose so important floor details and special tiles keep their original appearance.

You can hold **CTRL** and right-click a tile to copy its HDR tile key. This is mainly useful for reporting tiles that should be excluded.

## Reloading

HDR applies changes when the map loads.

After changing settings, the map may reload before the new colors appear. The plugin can also reload automatically when entering certain supported areas.

## Limitations

HDR only changes terrain tiles.

It does not recolor:

- Walls
- Objects
- NPCs or players
- Items
- Projectiles
- Water or lava textures
- The minimap

Some areas may need special handling because of how OSRS builds its scenes.