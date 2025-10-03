package gg.undefinedaquatic.asyncWorld.loader

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import gg.undefinedaquatic.asyncWorld.StorageInfo
import gg.undefinedaquatic.asyncWorld.driver.world.DriverWorld
import io.papermc.paper.world.PaperWorldLoader
import net.minecraft.core.GlobalPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.Main
import net.minecraft.server.WorldLoader.DataLoadContext
import net.minecraft.server.dedicated.DedicatedServerProperties.WorldDimensionData
import net.minecraft.server.level.ChunkLoadCounter
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.progress.LevelLoadListener
import net.minecraft.util.GsonHelper
import net.minecraft.util.datafix.DataFixers
import net.minecraft.world.Difficulty
import net.minecraft.world.entity.ai.village.VillageSiege
import net.minecraft.world.entity.npc.CatSpawner
import net.minecraft.world.entity.npc.WanderingTraderSpawner
import net.minecraft.world.level.*
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.PatrolSpawner
import net.minecraft.world.level.levelgen.PhantomSpawner
import net.minecraft.world.level.levelgen.WorldDimensions
import net.minecraft.world.level.levelgen.WorldOptions
import net.minecraft.world.level.storage.LevelStorageSource
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess
import net.minecraft.world.level.storage.PrimaryLevelData
import net.minecraft.world.level.validation.ContentValidationException
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.generator.CraftWorldInfo
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.generator.WorldInfo
import java.io.File
import java.io.IOException

object WorldUtil {

    fun WorldCreator.createWorldAsync(storageInfo: StorageInfo): World? {
        return (Bukkit.getServer() as CraftServer).createAsyncWorld(this, storageInfo)
    }

    fun CraftServer.createAsyncWorld(creator: WorldCreator, storageInfo: StorageInfo): World? {
        Preconditions.checkState(
            this.server.allLevels.iterator().hasNext(),
            "Cannot create additional worlds on STARTUP"
        )

        val name = creator.name()
        var chunkGenerator = creator.generator()
        var biomeProvider = creator.biomeProvider()
        val folder = File(this.worldContainer, name)
        val world: World? = this.getWorld(name)

        // Paper start
        val worldByKey: World? = this.getWorld(creator.key())
        if (world != null || worldByKey != null) {
            if (world === worldByKey) {
                return world
            }
            throw IllegalArgumentException("Cannot create a world with key " + creator.key() + " and name " + name + " one (or both) already match a world that exists")
        }

        // Paper end
        if (folder.exists()) {
            Preconditions.checkArgument(folder.isDirectory(), "File (%s) exists and isn't a folder", name)
        }

        if (chunkGenerator == null) {
            chunkGenerator = this.getGenerator(name)
        }

        if (biomeProvider == null) {
            biomeProvider = this.getBiomeProvider(name)
        }

        val actualDimension = when (creator.environment()) {
            World.Environment.NORMAL -> LevelStem.OVERWORLD
            World.Environment.NETHER -> LevelStem.NETHER
            World.Environment.THE_END -> LevelStem.END
            else -> throw IllegalArgumentException("Illegal dimension (" + creator.environment() + ")")
        }

        val levelStorageAccess: LevelStorageAccess?
        try {
            levelStorageAccess = LevelStorageSource.createDefault(this.worldContainer.toPath())
                .validateAndCreateAccess(name, actualDimension)
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        } catch (ex: ContentValidationException) {
            throw RuntimeException(ex)
        }

        val hardcore = creator.hardcore()

        val primaryLevelData: PrimaryLevelData?
        val context: DataLoadContext = this.server.worldLoaderContext
        var registryAccess = context.datapackDimensions()
        var contextLevelStemRegistry = registryAccess.lookupOrThrow<LevelStem?>(Registries.LEVEL_STEM)
        val dataTag = PaperWorldLoader.getLevelData(levelStorageAccess).dataTag()
        if (dataTag != null) {
            val levelDataAndDimensions = LevelStorageSource.getLevelDataAndDimensions(
                dataTag, context.dataConfiguration(), contextLevelStemRegistry, context.datapackWorldgen()
            )
            primaryLevelData = levelDataAndDimensions.worldData() as PrimaryLevelData
            registryAccess = levelDataAndDimensions.dimensions().dimensionsRegistryAccess()
        } else {
            val worldOptions = WorldOptions(creator.seed(), creator.generateStructures(), creator.bonusChest())
            val worldDimensions: WorldDimensions?

            val properties = WorldDimensionData(
                GsonHelper.parse(
                    creator.generatorSettings().ifEmpty { "{}" }
                ), creator.type().name.lowercase()
            )
            val levelSettings = LevelSettings(
                name,
                GameType.byId(this.defaultGameMode.getValue()),
                hardcore, Difficulty.EASY,
                false,
                GameRules(context.dataConfiguration().enabledFeatures()),
                context.dataConfiguration()
            )
            worldDimensions = properties.create(context.datapackWorldgen())

            val complete = worldDimensions.bake(contextLevelStemRegistry)
            val lifecycle = complete.lifecycle().add(context.datapackWorldgen().allRegistriesLifecycle())

            primaryLevelData = PrimaryLevelData(levelSettings, worldOptions, complete.specialWorldProperty(), lifecycle)
            registryAccess = complete.dimensionsRegistryAccess()
        }

        contextLevelStemRegistry = registryAccess.lookupOrThrow<LevelStem?>(Registries.LEVEL_STEM)
        primaryLevelData.customDimensions = contextLevelStemRegistry
        primaryLevelData.checkName(name)
        primaryLevelData.setModdedInfo(
            this.server.serverModName,
            this.server.moddedStatus.shouldReportAsModified()
        )

        if (this.server.options.has("forceUpgrade")) {
            Main.forceUpgrade(
                levelStorageAccess,
                primaryLevelData,
                DataFixers.getDataFixer(),
                this.server.options.has("eraseCache"),
                { true },
                registryAccess,
                this.server.options.has("recreateRegionFiles")
            )
        }

        val i = BiomeManager.obfuscateSeed(primaryLevelData.worldGenOptions().seed())
        val list: MutableList<CustomSpawner?> = ImmutableList.of<CustomSpawner?>(
            PhantomSpawner(), PatrolSpawner(), CatSpawner(), VillageSiege(), WanderingTraderSpawner(primaryLevelData)
        )
        val customStem = contextLevelStemRegistry.getValue(actualDimension)

        val worldInfo: WorldInfo = CraftWorldInfo(
            primaryLevelData,
            levelStorageAccess,
            creator.environment(),
            customStem!!.type().value(),
            customStem.generator(),
            this.getHandle().getServer().registryAccess()
        ) // Paper - Expose vanilla BiomeProvider from WorldInfo
        if (biomeProvider == null && chunkGenerator != null) {
            biomeProvider = chunkGenerator.getDefaultBiomeProvider(worldInfo)
        }

        val dimensionKey: ResourceKey<Level?>?
        val levelName: String = this.server.properties.levelName
        dimensionKey = when (name) {
            levelName + "_nether" -> {
                Level.NETHER
            }
            levelName + "_the_end" -> {
                Level.END
            }
            else -> {
                ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(creator.key().namespace(), creator.key().value())
                )
            }
        }

        val serverLevel = DriverWorld(
            storageInfo,
            this.server,
            this.server.executor,
            levelStorageAccess,
            primaryLevelData,
            dimensionKey,
            customStem,
            primaryLevelData.isDebugWorld,
            i,
            if (creator.environment() == World.Environment.NORMAL) list else ImmutableList.of<CustomSpawner?>(),
            true,
            this.server.overworld().randomSequences,
            creator.environment(),
            chunkGenerator, biomeProvider
        )

        this.server.addLevel(serverLevel) // Paper - Put world into worldlist before initing the world; move up
        this.initWorldAsync(serverLevel, primaryLevelData)

        //serverLevel.setSpawnSettings(true)

        this.prepareLevel(serverLevel)

        return serverLevel.world
    }

    fun CraftServer.prepareLevel(serverLevel: ServerLevel) {
        //this.forceTicks = true;
        // CraftBukkit end
        val chunkLoadCounter = ChunkLoadCounter()
        serverLevel.levelLoadListener.start(
            LevelLoadListener.Stage.LOAD_INITIAL_CHUNKS,
            chunkLoadCounter.totalChunks()
        ) // Paper - per world load listener

        do {
            serverLevel.levelLoadListener.update(
                LevelLoadListener.Stage.LOAD_INITIAL_CHUNKS,
                chunkLoadCounter.readyChunks(),
                chunkLoadCounter.totalChunks()
            ) // Paper - per world load listener
            //this.executeModerately(); // CraftBukkit
        } while (chunkLoadCounter.pendingChunks() > 0)

        serverLevel.levelLoadListener.finish(LevelLoadListener.Stage.LOAD_INITIAL_CHUNKS) // Paper - per world load listener
        serverLevel.setSpawnSettings(
            serverLevel.serverLevelData.difficulty != Difficulty.PEACEFUL && serverLevel.gameRules
                .getBoolean(GameRules.RULE_SPAWN_MONSTERS)
        ) // Paper - per level difficulty (from setDifficulty(ServerLevel, Difficulty, boolean))
        //this.updateEffectiveRespawnData();
        //this.forceTicks = false; // CraftBukkit
        //serverLevel.entityManager.tick(); // SPIGOT-6526: Load pending entities so they are available to the API // Paper - rewrite chunk system
        val loadTask = Runnable {
            WorldLoadEvent(serverLevel.world).callEvent() // Paper - call WorldLoadEvent
        }
        if (this.isPrimaryThread) {
            loadTask.run()
        } else {
            this.server.scheduleOnMain(loadTask)
        }
    }

    fun CraftServer.initWorldAsync(serverLevel: ServerLevel, serverLevelData: PrimaryLevelData) {
        if (serverLevel.generator != null) {
            serverLevel.world.populators
                .addAll(serverLevel.generator!!.getDefaultPopulators(serverLevel.world))
        }
        this.initWorldBorder(serverLevel)
        val initTask = Runnable {
            pluginManager.callEvent(WorldInitEvent(serverLevel.world))
        }
        if (this.isPrimaryThread) {
            initTask.run()
        } else {
            this.server.scheduleOnMain(initTask)
        }
        // Paper end - rework world loading process
        if (!serverLevelData.isInitialized) {
            serverLevelData.isInitialized = true
        }

        val globalPos: GlobalPos = this.server.worldData.overworldData().respawnData.globalPos()
        serverLevel.levelLoadListener.updateFocus(
            globalPos.dimension(),
            ChunkPos(globalPos.pos())
        ) // Paper - per world load listener
    }

    private fun CraftServer.initWorldBorder(serverLevel: ServerLevel) {
        // Paper start - rework world loading process
        serverLevel.worldBorder.world = serverLevel
        serverLevel.worldBorder.setAbsoluteMaxSize(29999984)
        this.server.playerList.addWorldborderListener(serverLevel)
        // Paper end - rework world loading process
    }

}