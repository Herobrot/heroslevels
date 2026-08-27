## Added:
- &nbsp;
## Fixed:
- &nbsp;
## Changed
- Updated the code for tabs for last version of HerosLib
---
# 1.0.0
## Added:
- Initial release. This notes are comparing the last LevelZ version.
- Added a red overlay to all the items you can't use in your inventory/hotbar
- A drop to ingredients in a Furnace/Brewing Stand whenever trying to fabricate a restricted item
- Brewing restrictions (you can drink potions, but not brew new potions)
- Compat with a few mods
- Locked tooltips adds an arrow whenever trying to fabricate a restricted item
- A restriction command to help modpackers
- Added bonus for potions (extra duration effect, lingering potions having more radius and duration)
- Added a configuration for how many XP mod drop when dying
  - Added a dynamic drop to XP so that, if losing levels/experience (hard mode), it drops a %percentage of the XP, preventing the absolute loss of the progress for the player
- Added compat for Jade, Curios/Accessories, Enchanting Infuser, Easy Anvils/Magic and Apotheosis (kinda this one)
## Changed:
- Now experience farms can work without spawning the mod experience
- Better configuration for colors on text screens
- The player in Skill Screen now is not a "statue"
- Changed the way enchantments is registered, making it compatible with Apotheosis and any mods that adds further vanilla level enchantments
- Using more NeoForge events and less mixin codes, preventing mixin conflicts with other mods
## Fixed:
- Fixed an exploit for leveling up when dying and pick up the experience
- Fixed a lot of garbageCollector and trying to optimize the code