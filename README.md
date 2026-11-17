# Wandry — Fabric 1.21.11

Three Blockbench wands and their shared spell system.

## Wands
- **Magma Wand** — 1 damage point, ignites the target for 3 seconds.
- **Bone Wand** — 1 damage point, applies blindness for 1 second.
- **Celestial Wand** — 1 damage point, applies Glowing for 3 seconds.

## Casting and visuals
- Right-click and release to cast.
- The wand uses pull-back and thrust item poses while charging/releasing.
- Each cast launches a visible 3D projectile assembled from the supplied Blockbench Bone Bolt core and four shard models.
- The same geometry is recolored for magma (orange/red), bone (ivory), and celestial (light blue).
- Colored dust fragments orbit/drift independently around the projectile, with a launch flare and impact burst.
- The projectile model itself follows its flight direction.

## Build
Requires JDK 21 and Gradle compatible with Fabric Loom 1.17.13. Run `gradle build` from this folder. The mod jar will be in `build/libs/`.

## Included source assets
- Original wand `.bbmodel` files in `blockbench_sources/`.
- The user's projectile prototype is represented by its supplied core, four shard model JSONs, and ivory texture, combined into the three color variants.

**Build status:** source package only. It has not been compiled or play-tested in Minecraft in this environment.
