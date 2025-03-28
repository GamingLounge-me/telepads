[![](https://maven.gaminglounge.me/api/badge/latest/gaminglounge/me/gaminglounge/telepads?color=40c14a&name=Telepads&prefix=v&filter=none:SNAPSHOT)](https://maven.gaminglounge.me/#/gaminglounge/me/gaminglounge/telepads)

# Telepads

This plugin adds Telepads which are a 3x3 multiblockstructure. The multiblockstructure builds it self when the telepads item (`/telepad:giveBuildItem`) is placed.
The Telepad can also be obtained via Recepie, this recepie is given to all users when they join(so it will be in their secepie book) and needs:
- 1x Ender eye
- 1x Diamond Block
- 4x Glass
- 3x Obsidian Block

It provied an interface where it can be:

1. pickupped
2. upgraded (Level 1 and 2 are available at the time)
3. configured permissions (for usage, settings are owner only)
4. Name edited (minimessage for formatting)
5. set a custom block for the display

Also this plugins has an TPA command.

## More features

All the Telepads which are level 2+ can be seen with the command: `/pad` if you habe the permission to see it, from there you can favorite pads and teleport to them.
Also Telepads wich an level higher than 2 can be set public so everyone can see the Telepad.
For admins the command `/telepad:admin` can be used to vie _all_ telepads without minimessage(for more sight into user configuration) formatting, favorite management and are able to teleport to them.

## Config

In the Config the following thinsg can be configured:

1. Database (inc. driver path for both mariadb and mysql support)
2. The Cost of the Teleport and Upgrade
3. TPA-cooldown/move time/move tolerance

## language

The Language is provied via two language files and are used with my api (ConfigAPI)[https://github.com/GamingLounge-me/ConfigAPI], which will load the config and get the correct sting based on the players Language.

## Future plans

I plan to add support to other economy options which will be an configurable vanilla item at the start.
Also i plan to change the level system to an function by gui, when UpgradeCost(config) is 0 all function are on by default and can be disabled.
