# Summary
A json is a file that contains "keys" (`"this_is_a_key":`) and "values" (`"I am a value"`/`20`/`[An array]`/`{An object}`)

You can create multiple files like `constitution.json` and contains only a `"constitution"` object. 
Or make an only file that contains multiple skills.
## Constitution
Example Path: `data/heroslevels/skills/constitution.json`.
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
Remember, it only adds the Skill and their logic, but if you want to add the icon, 
information for bonus and translation, you have to create a `resourcepack`:
```
assets/heroslevels/
- lang/
  - en_us.json
- textures/
  - gui/
    - sprites/
      - constitution.png
```
## en_us.json
```json
{
  "skill.heroslevels.constitution": "Constitution",
  "skill.heroslevels.constitution.0": "Constitution increases max health",
  
  "bonus.heroslevels.healthRegen.0": "Extra health regeneration %s",
  "bonus.heroslevels.exhaustionThirstReduction.0": "Reduction in exhaustion from thirst %s"
}
```
- `skill.**.<skill_key>.X`: Adds information for the skill per line.
- `bonus.**.<bonus_id>.X`: Adds information for the bonus on X skill. Is important to know that it will only add the line if the bonus is correctly added.
## constitution.png
![constitution_16x16_file](https://github.com/Herobrot/heroslevels/blob/1.21/src/main/resources/assets/heroslevels/textures/gui/sprites/constitution.png?raw=true)

The icon **it should be a 16x16** file in png.