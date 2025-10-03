package gg.undefinedaquatic.asyncWorld.driver.world

import com.mojang.datafixers.DataFixer
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ChunkMap
import net.minecraft.util.thread.BlockableEventLoop
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.TicketStorage
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.chunk.LightChunkGetter
import net.minecraft.world.level.entity.ChunkStatusUpdateListener
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
import net.minecraft.world.level.storage.DimensionDataStorage
import net.minecraft.world.level.storage.LevelStorageSource
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Supplier

class DriverChunkMap(
    level: DriverWorld,
    storageSource: LevelStorageSource.LevelStorageAccess,
    fixerUpper: DataFixer,
    structureManager: StructureTemplateManager,
    val dispatcher: Executor,
    mainThreadExecutor: BlockableEventLoop<Runnable>,
    lightChunk: LightChunkGetter,
    generator: ChunkGenerator,
    chunkStatusListener: ChunkStatusUpdateListener?,
    overworldDataStorage: Supplier<DimensionDataStorage>,
    ticketStorage: TicketStorage,
    serverViewDistance: Int
) : ChunkMap(
    level,
    storageSource,
    fixerUpper,
    structureManager,
    dispatcher,
    mainThreadExecutor,
    lightChunk,
    generator,
    chunkStatusListener,
    overworldDataStorage,
    ticketStorage,
    serverViewDistance,
    false
) {

    override fun write(pos: ChunkPos, tag: Supplier<CompoundTag?>): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        dispatcher.execute {
            (level as DriverWorld).storageInfo.write(pos, tag.get())
            future.complete(null)
        }
        return future
    }

    override fun read(pos: ChunkPos): CompletableFuture<Optional<CompoundTag>> {
        return CompletableFuture.supplyAsync {
            val optional: Optional<CompoundTag> = Optional.ofNullable((level as DriverWorld).storageInfo.read(pos))
            return@supplyAsync optional
        }
    }
}