# Debug Utils 
[![](http://cf.way2muchnoise.eu/full_783008_Forge_%20.svg)![](http://cf.way2muchnoise.eu/versions/783008.svg)](https://www.curseforge.com/minecraft/mc-mods/debug-utils-forge) [![](http://cf.way2muchnoise.eu/full_783010_Fabric_%20.svg)![](http://cf.way2muchnoise.eu/versions/783010.svg)](https://www.curseforge.com/minecraft/mc-mods/debug-utils-fabric) [![Discord](https://img.shields.io/discord/790631506313478155?color=0a48c4&label=discord)](https://discord.gg/8Cx26tfWNs)

Mojang has various debugging tools implemented in minecraft that are removed for the release jar.
This mod adds them back which might be useful for various cases. You will need this mod on both the client and the server.

Use the command /debugutils <feature> to turn a debugging feature on/off

### Included debugging features are:

**POI debugging**: Logs adding/removing of POI blocks.

**Block Updates**: Displays blocks affected by a change in block states

**Structure Generations**: Displays the bounding box of newly generated structures

**Entity Pathing**: Will show the pathings of entities

**Entity Goals**: Will show various information about an entities ai. Active ones are highlighted.

**Raids**: Will highlight the center of a raid

**Entity Brains**: Similar to goals will display brain activities of entities.

**Bees**: Displaying information about bees. E.g. if it has a hive or not

**Game Event + Listener**: Highlights game info events and the adding of listeners for it. 
Listeners are e.g. sculk/warden/allays
And game infos are events which triggers them.

**Bee Hives**: Shows information about a bee hive

**Water**: Shows water level of nearby water blocks

**Heightmap**: Displays the heightmap

**Collision**: Shows nearby block collisions

**Light**: Shows the sky light value of blocks. Places with direct sky light are not displayed.

**Solid Faces**: Shows the solid faces of nearby blocks. A face is solid if it fills the whole plane.

**Chunk**: Shows nearby chunk data. E.g. if the chunk is entity ticking or not

**Spawn Chunks**: Shows entity ticking and lazy spawn chunks

### Devs

If you want to use the mod in your dev environment just add the following snippet to your build.gradle

```gradle
repositories {
    maven {
        url "https://gitlab.com/api/v4/projects/21830712/packages/maven"
    }
}

dependencies {    
    //Fabric==========    
    modRuntime("io.github.flemmli97:debugutils:${minecraft_version}-${mod_version}-fabric")
    
    //Forge==========    
    runtimeOnly fg.deobf("io.github.flemmli97:debugutils:${minecraft_version}-${mod_version}-forge")
}
```
