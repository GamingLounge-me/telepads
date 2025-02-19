[![](https://maven.gaminglounge.me/api/badge/latest/gaminglounge/me/gaminglounge/telepads?color=40c14a&name=Telepads&prefix=v&filter=none:SNAPSHOT)](https://maven.gaminglounge.me/#/gaminglounge/me/gaminglounge/telepads)

# Telepads

This plugin adds Telepads which are a 3x3 multiblockstructure. The multiblockstructure builds it self when the telepads item (`/telepad:giveBuildItem`) is placed.
It provied an interface where it can be:

1. pickupped
2. upgraded (2 Level are available at the time)
3. configured permissions (for usage, settings are owner only)
4. Name edited (minimessage for formatting)
5. set a custom block for the display

## More features

All the Telepads which are level 2+ can be seen with the command: `/pad` if you habe the permission to see it, from there you can favorite pads and teleport to them.
For admins the command `/telepad:admin` can be used to vie _all_ telepads without minimessage(for more sight into user configuration) formatting, favorite management and are able to teleport to them.

## Config

In the Config the following thinsg can be configured:

1. Database (inc. driver path for both mariadb and mysql support)

More config is coming soon at the time a teleport costs 2 coins and is used via vault, names and messages are also hard coded at the time which will change in the future.

## language

At the moment the messages are hardcoded and onyl are in german, the default language will continue to be german but the messages will be configurable in the future.
