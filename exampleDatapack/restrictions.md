# Summary
A json is a file that contains "keys" (`"this_is_a_key":`) and "values" (`"I am a value"`/`20`/`[An array]`/`{An object}`)

You can create multiple files like `constitution_restrictions.json` and contains only a `"constitution_restrictions"` object.
Or make an only file that contains multiple skills.
## Default template
```json
{
  "template_XX": {
    "replace": false,
    "skills": {},
    "blocks": [],
    "crafting": [],
    "entities": [],
    "items": [],
    "brewing": [],
    "enchantments": {}
  }
}
```
## Constitution
Example Path: `data/heroslevels/restrictions/constitution_restrictions.json`.
```json
{
  "constitution_8_restrictions": {
    "replace": false,
    "skills": {
      "constitution": 8
    },
    "items": [
      "minecraft:brush"
    ]
  },
  "constitution_10_restrictions": {
    "replace": false,
    "skills": {
      "constitution": 10
    },
    "items": [
      "minecraft:spyglass"
    ]
  },
  "constitution_12_restrictions": {
    "replace": false,
    "skills": {
      "constitution": 12
    },
    "items": [
      "minecraft:goat_horn"
    ]
  },
  "constitution_16_restrictions": {
    "replace": false,
    "skills": {
      "constitution": 16
    },
    "blocks": [
      "minecraft:jukebox"
    ]
  }
}
```
Unlike creating Skills, it doesn't need a resourcepack, other than when creating a personalized skill.
If that, then in only requieres a lang file:
```
assets/heroslevels/
- lang/
  - en_us.json
```
## en_us.json
```json
{
  "restriction.heroslevels.constitution.tooltip": "Constitution %d"
}
```
- `restriction.**.<skill_key>.tooltip`: Adds the line and what level is restricted.
- `"%d"`: This is the value for "digit"