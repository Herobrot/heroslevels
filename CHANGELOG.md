# 1.1.1
# IMPORTANT
Starting with this update, I will no longer be adding extra features or compatibilities.
This is because Globox, the original modder, plans to update to NeoForge, but specifically for version 26.3. 
However, for this and other mods ported of mine, I will continue to provide support, though only in the form of maintenance
and fixing critical bugs.
## What's next?
I’m working on personal mods that are completely unrelated to Globox. At most, they might be inspired
by his mods but expect something entirely new from me. ^^
## Fixed:
- Fixed a crash when loading the mod on a dedicated server
---
# 1.1.0
## Added:
- Added Thirst Was Reclaimed compat
  - Added 'exhaustionThirstReduction' bonus
  - Added 'drinkIncreasion' bonus
- Added a dynamic bonus when loading other mods that are compatible. So it default applies the skill with compatible attribute/bonus
- Added a new config for mods compat
## Fixed:
- Fixed bad name render on scoreboard and player name
- Fixed merchantInmune not working
- Fixed bypassing kill commands damage on evadingDamageBonus
- Fixed crash with conflicting ids<->keys, should now warn an error when pointing to the wrong id/key to an already created skill
- Fixed a bug that prevented 'allowHigherSkillLevel' to work intended
- Fixed edge cases bugs for screen and networking
- Fixed bad default ids (gold tools, copper tools and some stones)
## Changed
- Changed healthRegen to boost natural regeneration and not always regenerating
- Changed the default skills 'replace:true' to 'false'
- Updated sprites
- Attempt to optimize the code
---
# 1.0.4
## Added:
- &nbsp;
## Fixed:
- Accessories bad range version. Making crash whenever having the Accessories mod.
## Changed
- &nbsp;
---
# 1.0.3
## Added:
- New methods and Javadoc explanation in the API from the mod. This is just an add-on for modders who want to take advantage of Hero's Levels.
## Fixed:
- A server-side bug when the restrictions can't be loaded for players
## Changed
- "damageReflection" & "damageReflectionChance" now can be applied in any skills and also be cumulative if they are registered in more than one Skill. Previously, both bonuses had to be part of a single skill; if they were added individually to another skill, they would cancel each other out.
- Bump HerosLib requirement (now 1.2.1+1.21.1).
---
# 1.0.2
## Added:
- Added click and drag for the scroll bar
- Added option to turn off the tabs
- Added compat for LegendaryTabs (this fix is from HerosLib side)
## Fixed:
- Tried to fix a Tooltip bug on Restriction Screens
## Changed
- &nbsp;
---
# 1.0.1
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