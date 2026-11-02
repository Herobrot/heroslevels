![Heros-Levels-Neoforge-Text](https://cdn.modrinth.com/data/cached_images/7877a764162b6976c51f5a9e27317d97c3b40029_0.webp)

[![cloth-config-api](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/cloth-config-api_64h.png)](https://modrinth.com/mod/cloth-config)
[![Requires-Heros-Lib](https://cdn.modrinth.com/data/cached_images/3b6545deb075073916f42e8175145fa76de4002b.png)](https://modrinth.com/mod/heros-lib)
[![available-neoforge](https://cdn.modrinth.com/data/cached_images/0ca42fd3f745230049fd9f62d8520af33fef9dda.png)](https://neoforged.net/)
# IMPORTANT
This mod and other ports of mine is gonna be **maintenance only**. This is because the original modder, [Globox](https://modrinth.com/user/Globox1997), is working on porting his mods to NeoForge.

# Hero's Levels
Hero's Levels adds a system of levels and restrictions that are unlocked as the player improves their skills.

This mod is a port to NeoForge from the mod [LevelZ](https://github.com/Globox1997/LevelZ) and is published with the [author's](https://github.com/Globox1997) explicit permission.
Installation

## Installation
Hero's Levels is a mod built for [NeoForge Loader](https://neoforged.net/) and it requires [Cloth Config API](https://modrinth.com/mod/cloth-config) and my lib [Hero's Lib](https://modrinth.com/mod/heros-lib)

## Datapack
The mod is fully data-driven. You can create custom skills, bonuses, and restrictions using datapacks placed in the `data/heroslevels/` directory.

### Skills
A skill requires a unique id, a key (used for translations and icon), and a maximum level.

The key value is extremely important as it references the texture path used for the skill icon in the GUI.

The texture must be located at: `assets/heroslevels/textures/gui/sprites/<key>.png`

Additionally, the key is used to generate a translation key for the skill name: `skill.heroslevels.<key>`

```json
{
  "constitution": {  
    "replace": false,
    "id": 0,
    "key": "constitution",
    "level": 20,
    "attributes": [
      {
        "id": 0,
        "type": "generic.max_health",
        "base": 16,
        "operation": "ADD_VALUE",
        "value": 1
      }
    ],
    "bonus": [
      {
        "level": 1,
        "key": "exhaustionThirstReduction",
        "mod": "thirst"
      },
      {
        "level": 10,
        "key": "healthRegen"
      }  
    ]
  }
}
```
- `"replace": false`: It create the skill, if there's already skill created, then merge the attributes and/or bonus.
- `"replace": true`: It creates the skill, if there's already skill created, then overrides that skill and have full priority that object.
- `"id"`: The id and the order of that skill in the Level Screen. If you want to change the order from the default skills, consider to turn to `false` the config `defaultSkills`
- `"key"`: The name of the skill.
- `"level":` The max level of that skill.
- `"attributes":` An array of objects for each attribute. It could also be not add no one `[]`.
  - `"id"`: (Optional) Same logic like the id for skills, but for attributes.
  - `"type"`: The attribute type in Minecraft. You can check in-game what you have with the auto-completion command `/attribute @p <attribute>`
  - `"base"`: (Optional) The base value it will spawn the player with that attribute. Consider that if replacing another skill, if that adds a base to the attribute you want to change, you should replace the skill for not conflict.
  - `"operation"`: The operation for Minecraft modifying attributes. It accepts [`ADD_VALUE`/`ADD_MULTIPLIED_BASE`/`ADD_MULTIPLIED_TOTAL`].
  - `"value"`: The value it adds/multiplying for each new level.
- `"bonus":` An array of objects for each bonus. Same for the attributes `[]`.
  - `"level"`: The minimum level to get the bonus. Each new level after that boost the bonus (not for all, but the majority yes).
  - `"key"`: The id of the bonus to apply to the skill.
  - `"mod"`: (Optional) This is **exclusive** for this version of LevelZ port. It applies the bonus only if the id of the mod is loaded in the game. 
### Attributes
You can use any registered attribute in the game, as long as the type matches the correct attribute ID.
This includes attributes added by other mods.

Examples of valid attribute types:
```
generic.max_health
generic.armor
generic.movement_speed
```
### Bonuses
Bonuses are special effects that unlock when a player reaches a specific level in a skill.
- `bowDamage`: Each level grants +bowDamage on arrow damage
- `bowDoubleDamageChance`: Chance to double arrow damage with bow
- `crossbowDamage`: Each level grants +crossbowDamage on arrow damage
- `crossbowDoubleDamageChance`: Chance to double arrow damage with crossbow
- `itemDamageChance`: Each level grants +chance to not consume item damage on item usage
- `potionAmplifierChanceBonus`: Chance to increase effect amplifier by one
- `potionDurationBonus`: Chance to increase potion duration and percentage increase
- `lingeringCloudRadiusChanceBonus`: Chance to increase the radius of lingering
- `lingeringCloudRadiusBonus`: Each level increase the radius of lingering
- `lingeringCloudDurationBonus`: Each level increase the duration of lingering by seconds
- `breedTwinChance`: Chance to have twins on breeding
- `fallDamageReduction`: Each level grants +fallDamageReduction
- `deathGraceChance`: Chance to not die on critical damage intake
- `tntStrength`: Grants +tntStrength tnt strength
- `priceDiscount`: Each level grants %priceDiscount on trading
- `tradeXp`: Each level grants more %tradeXp
- `merchantImmune`: Grants immunity to reputation decrease and attack call on damaging merchant
- `miningDropChance`: Each level grants %chance to double ore drop
- `plantDropChance`: Each level grants %chance to double plant drop
- `anvilXpCap`: Grants xp cap on anvil usage
- `anvilXpDiscount`: Each level grants %discount on anvil usage
- `anvilXpChance`: Chance to not use xp on anvil usage
- `healthRegen`: Each level grants %health on regeneration
- `healthAbsorption`: Grants absorption on regeneration
- `exhaustionReduction`: Each level grants %exhaust reduction
- `meleeKockbackAttackChance`: Each level grants %chance to knockback
- `meleeCriticalAttackChance`: Each level grants %chance to critical hit
- `meleeCriticalAttackDamage`: Each level grants +critical melee damage on critical hit
- `meleeDoubleAttackDamageChance`: Chance to double melee damage
- `foodIncreasion`: Each level grants %food value when eating food
- `damageReflection`: Each level grants %damage reflection
- `damageReflectionChance`: Each level grants %chance to reflect damage
- `evadingDamageChance`: Chance to evade incoming damage
- **[Thirst was Reclaimed compat]**
  - `drinkIncreasion`: Each level grants %thirst value when drinking drinks
  - `exhaustionThirstReduction`: Each level grants %thirst exhaust reduction

### Restrictions
Restrictions prevent players from using items, breaking blocks, or crafting until they reach the required skill level.
A restriction requires a skills object mapping the skill key to the required level.

```json
{
  "template_XX": {
    "replace": false,
    "skills": {
      "yourskill": 5
    },
    "blocks": [],
    "crafting": [],
    "entities": [],
    "items": [],
    "brewing": [],
    "enchantments": {}
  }
}
```

## Resourcepack
You can add descriptive text to your skills and bonuses in the skill info screen by adding translation keys to your language file.
- For Skill Info, use the key format: `skill.heroslevels.yourskill.0`, `skill.heroslevels.yourskill.1`, etc.
- For Bonus Info, use the key format: `bonus.heroslevels.id_de_bonus.0`, `bonus.heroslevels.id_de_bonus.1`, etc.

## Commands
Hero's Levels provides admin commands to manage player progression and automatically generate restriction datapacks.
### Level Command
Usage: `/heroslevels level <targets> <add|remove|set|get> <skillKey> [amount]`
- `<targets>`: The target player(s) (e.g., @a, @p, or a username).
     add|remove|set|get: The operation to perform.
- `<skillKey>`: The progression category to modify. Accepts:
         experience: Modifies the player's overall XP.
         points: Modifies available skill points.
         level: Modifies the player's overall level.
         all: Applies the operation to all registered skills.
         [skill_key]: The specific skill key (e.g., constitution).

### Restrict Command
Usage: `/heroslevels restrict <skill> <level> <type>`
This command automatically generates restriction entries. It looks at the block you are facing, 
the entity you are looking at, or the item you are holding, and adds it to a generated JSON file.
- `<skill>`: The skill key (e.g., miner).
- `<level>`: The required skill level (minimum 1).
- `<type>`: The category of the restriction. Accepts items, blocks, crafting, entities, mining, brewing, or enchantments.

**Note:** For the enchantments type, the command will read **all enchantments** from the book or item you are currently holding.

The generated restrictions are automatically saved to:
`\minecraft\config\heroslevels\generated\generated_restrictions.json`
