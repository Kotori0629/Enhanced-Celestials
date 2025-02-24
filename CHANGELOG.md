# 6.0.1.3
* Fix LunarForecast when requires clear skies is false.

# 6.0.1.2
* Fix issues with rain and lunar events.

# 6.0.1.1
* Fix crashes/errors related to the new Lunar Forecast system.

# 6.0.1.0
* Rewrite Lunar forecasting internal to address various bugs and desync issues.
* Add a new config option to enforce a max number of days between events.

# 6.0.0.0
* Port to 1.21

# 5.0.1.0
* Move to arch loom and fix project compiling
* Switch datapack path to `<modid>/enhancedcelestials/<path>` instead of `<modid>/<path>` on fabric.
* Fix tags on fabric.

# 5.0.0.4
* Fix Harvest moon crops missing in item tag.

# 5.0.0.3
* Fix a crash related to dynamic registries.

# 5.0.0.2
Fix Forge `MixinLootTableManager` injection signature, fixes #161

# 5.0.0.1
* Fix blood moon mob spawn rate.

# 5.0.0.0
* Port to 1.20.1

# End 1.19.4, Start 1.20.1
# 4.0.0.0
* Port to 1.19.4

# End 1.19.3, Start 1.19.4
# 3.0.0.0
* Port to 1.19.3

# End 1.19.2, Start 1.19.3
***
# 2.1.0.5
* Fix skylight colors.
* Lunar events will no longer apply their effects when raining if `requires_clear_skies` is set to true in LunarDimensionSettings json. Overworld sets this value to true by default.

# 2.1.0.4
* Add Japanese Translation.
* Changes command permission level from 3 to 2, allowing commands to be used by command blocks and data packs.
* Fix Super Moon rise/set notifications.

# 2.1.0.3
* Fix incorrect moon/skylight colors when an event ends.
* Remove blend strength fields in color settings. Set color values as if they were blended for sky & moon texture colors.
* Use a resource key to declare the default lunar event.
* LunarForecast blend initializes with a value of 1. Fixes moon growing/color blending when joining a server with an active event.
* Bump CorgiLib dependency version.
* Remove Jankson library. 

# 2.1.0.2
* Use CorgiLib's Conditions.
* Add CorgiLib dependency.

# 2.1.0.1
* Update codec keys to use persistent spelling/casing scheme

# 2.1.0.0
* Rewrite to use data packs.
* Abstraction/Refactors/More configurations.
* Add Super Moon.
* Add moon event tags.

# 2.0.0.0
* 1.19 port.