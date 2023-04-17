<div align="center">

<img alt="Server Announce Icon" src="src/main/resources/assets/serverannounce/megaphone_by_Andrew6rant.png" width="128">

# Server Announce

Simple, Configurable Server Announcements

<!-- todo: replace 494721 with your CurseForge project id -->
[![Release](https://img.shields.io/github/v/release/John-Paul-R/server-announce?style=for-the-badge&include_prereleases&sort=semver)][releases]
[![Available For](https://img.shields.io/badge/dynamic/json?label=Available%20For&style=for-the-badge&color=34aa2f&query=$[:]&url=https%3A%2F%2Fwww.jpcode.dev%2Fserverannounce%2Fsupported_mc_versions.json)][modrinth:files]

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/server-announce?color=00AF5C&label=modrinth&style=for-the-badge&logo=modrinth)][modrinth:files]
[![GitHub Downloads (all releases)](https://img.shields.io/github/downloads/John-Paul-R/server-announce/total?style=for-the-badge&amp;label=GitHub&amp;prefix=downloads%20&amp;color=4078c0&amp;logo=github)][releases]

</div>

Server Announce is a serverside **Fabric** mod to send chat messages when
certain Minecraft server events occur.

It is built on the [Fabric][fabric] mod loader and is available for modern
versions of [Minecraft][minecraft] Java Edition.

## Overview

Server Announce allows you to schedule messages in the future (optionally recurring!)

Messages Text can be provided in the same format as Minecraft's `/tellraw`, so `"Quoting text like this works"`, as does `{"text":"Minecraft's built-in JSON format","color":"gold"}`.

This means that tellraw generators like [MinecraftJson](https://www.minecraftjson.com/) will work with Server Announce.

### Message Types

- `SingleMessage` - A message that executes once, after the specified duration has elapsed. Runs that long after the command is executed (and after that duration has elapsed from server start)
- `PeriodicSingleMessage` - A message that executes repeatedly, in intervals of the specified duration.
- `PeriodicMessageGroup` - A collection of messages that will by cycled through repeatedly, one message per interval of the specified duration.

## New Commands

Note: All commands start with `/serverannounce` and require OP level `4`.

For reference, it takes `20` Minecraft "tick"s to make a real-life second.

### `create`

subcommands:

- `periodic_message_group <message_group_name> <period_ticks>`
- `periodic_single_message <message_name> <period_ticks> <message_text>`
- `single_message <message_name> <duration_ticks> <message_text>`

### `edit`

subcommands:

- `periodic_message_group <message_group_name> addMessage <message_name> <message_text>`

### `executeAll`

This one's pretty simple. Immediately sends out all scheduled messages, and resets their timers, starting at the current time. Primarily intended to aid with testing newly-added messages.

## Contributing

Thank you for considering contributing to Server Announce! Please see the
[Contribution Guidelines][contributing].

## Licence

Server Announce is open-sourced software licenced under the [MIT licence][licence].

## Icon credit!

Icon graciously provided by the fancy fella, [@Andrew6rant](https://github.com/Andrew6rant)

[contributing]: .github/CONTRIBUTING.md
[curseforge]: https://curseforge.com/minecraft/mc-mods/serverannounce
[curseforge:files]: https://curseforge.com/minecraft/mc-mods/serverannounce/files
[modrinth]: https://modrinth.com/mod/server-announce
[modrinth:files]: https://modrinth.com/mod/server-announce/versions
[fabric]: https://fabricmc.net/
[licence]: LICENCE
[minecraft]: https://minecraft.net/
[releases]: https://github.com/John-Paul-R/server-announce/releases
[security]: .github/SECURITY.md
