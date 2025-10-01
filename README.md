# Debug Utils 
[![](http://cf.way2muchnoise.eu/full_783008_CurseForge_%20.svg)![](http://cf.way2muchnoise.eu/versions/783008.svg)](https://www.curseforge.com/minecraft/mc-mods/debug-utils)  
[![](https://img.shields.io/modrinth/dt/orux8o94?logo=modrinth&label=Modrinth)![](https://img.shields.io/modrinth/game-versions/orux8o94?logo=modrinth&label=Latest%20for)](https://modrinth.com/mod/debugutils)  
[![Discord](https://img.shields.io/discord/790631506313478155?color=0a48c4&label=discord)](https://discord.gg/8Cx26tfWNs)

With 1.21.9 debug features are no longer stripped in the release jar, but they require a jvmArg for them to be enabled.  
This mod now changes it so the features can be enabled via commands.  
The commands have been split into 
- `debugutils`: For features that require server data
- `debugutils_client` For feature that only need client data. These features do not require the mod on the server either

### Devs

If you want to use the mod in your dev environment just add the following snippet to your build.gradle

```gradle
repositories {
    maven {
        url "https://maven.blazing-coop.net/releases"
    }
}

dependencies {    
    //Fabric==========    
    modRuntime("io.github.flemmli97:debugutils:${minecraft_version}-${mod_version}-{mod_loader}")
    
    //NeoForge==========    
    runtimeOnly("io.github.flemmli97:debugutils:${minecraft_version}-${mod_version}-{mod_loader}")
}
```
