package gg.undefinedaquatic.asyncWorld.driver

import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.RandomSequences
import net.minecraft.world.level.CustomSpawner
import net.minecraft.world.level.Level
import net.minecraft.world.level.chunk.storage.ChunkStorage
import net.minecraft.world.level.chunk.storage.RegionStorageInfo
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.storage.LevelStorageSource
import net.minecraft.world.level.storage.PrimaryLevelData
import org.bukkit.World
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.ChunkGenerator
import java.util.concurrent.Executor

class DriverWorld(
    server: MinecraftServer,
    dispatcher: Executor,
    storageSource: LevelStorageSource.LevelStorageAccess,
    levelData: PrimaryLevelData,
    dimension: ResourceKey<Level>,
    levelStem: LevelStem,
    isDebug: Boolean,
    biomeZoomSeed: Long,
    customSpawners: List<CustomSpawner>,
    tickTime: Boolean,
    randomSequences: RandomSequences?,
    env: World.Environment,
    gen: ChunkGenerator?,
    biomeProvider: BiomeProvider?
) : ServerLevel(
    server,
    dispatcher,
    storageSource,
    levelData,
    dimension,
    levelStem,
    isDebug,
    biomeZoomSeed,
    customSpawners,
    tickTime,
    randomSequences,
    env,
    gen,
    biomeProvider
) {

     init {

         val regionStorageInfo = RegionStorageInfo(
             storageSource.levelId, dimension, "chunk"
         )
         ChunkStorage::class.java.getDeclaredField("storage").apply {
             isAccessible = true
         }.set(chunkSource.chunkMap, DriverRegionFileStorage(regionStorageInfo, storageSource.getDimensionPath(level.dimension()).resolve("region"), false))

     }



}