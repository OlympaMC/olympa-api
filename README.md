# OlympaAPI
## Olympa

Olympa was a Minecraft server. The full development took 2 years. The server closed in October 2021 and this is one of the archives of these many plugins. This repo is licensed under the GNU GPLv3.
More info about Olympa [here](https://github.com/OlympaMC/.github/blob/main/profile/README.md).


## Info
Internal API that contains the minimum for the project developers to interact with the environment. OlympaAPI needs OlympaCore to work properly in production. OlympaAPI contains code that runs on Spigot or Bungeecord or both.

The API contains, among others :
 - Complex Command
 - Gui
 - Global groups and server groups
 - Permission (custom and vanilla permission)
 - Report system
 - Chat (insult, link ...)
 - Complex Ban/Mute
 - Communication between several instances of OlympaAPI
 - Player custom information - cache in Redis
 - Save data to SQL DBMS
 - Captcha with png in Minecraft
 - Auctions
 - Afk
 - Custom image in game
 - Create Holograms
 - Scoreboard very optimized
 - Complex Regions
 - Vanish
 - Trades
 - Configs
 - Crack connect with password, premium without
 - Basic Monitoring Minecraft servers

The OlympaAPI JAR is used for experimental testing purposes locally.

## Dependencies

- [Gradle](https://github.com/gradle/gradle) (Compilator)
- [Java JDK 16](https://github.com/openjdk/jdk16)
- [Spigot](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse) 1.16
- [PaperSpigot](https://github.com/PaperMC/Paper) 1.16
- [Waterfall](https://github.com/PaperMC/Waterfall) 1.17
- [Jedis](https://github.com/redis/jedis) (Redis client for Java)